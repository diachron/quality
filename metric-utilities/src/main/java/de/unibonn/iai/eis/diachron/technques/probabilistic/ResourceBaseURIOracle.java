package de.unibonn.iai.eis.diachron.technques.probabilistic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.mapdb.HTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.net.InternetDomainName;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.DCTypes;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.datatypes.Pair;
import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;
import de.unibonn.iai.eis.diachron.semantics.knownvocabs.DCAT;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.CUBE;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.VOID;

/**
 * @author Santiago Londoño
 * 
 * Implements heuristics to determine the base URI of a resource based on the triples it contains.
 * Specifically, by means of the URIs of the subjects of each triple.
 *
 */
public class ResourceBaseURIOracle {
	
	final static Logger logger = LoggerFactory.getLogger(ResourceBaseURIOracle.class);
	
	/**
	 * If the resource's base URI is declared in the document (e.g. by means of the rdf:type or owl:Ontology properies), 
	 * it will be stored in this variable. Thus, it being not null indicates that one of the heuristics has already found 
	 * a solution.
	 */
	private String declaredResDatasetURI = null;
	
	/**
	 * If the resource's base URI has previously been guessed, it will be stored in this variable. 
	 */
	private String lastGuessedBaseURI = null;
	
	/**
	 * A table holding the set of URIs recognized as parent URIs of the subjects of all the processed triples.
	 * The parent URI is obtained by taking the substring behined the last appearance of "/" in the subject's URI. As values,
	 * the table contains the number of times the parent URI set as key has appeared as subject in the processed triples
	 */
	private ConcurrentHashMap<String, Integer> tblSubjectURIs = null;
	
	/**
	 * Maximum number of subject URIs that can be held in the table, a limit is imposed to prevent memory 
	 * exhaustion when processing very big resources
	 */
	private final int maxSubjectURIs = 20000;
	
	
	final static List<String> _datasetClass = new ArrayList<String>();
	static{
		_datasetClass.add(VOID.Dataset.getURI());
		_datasetClass.add(DCTypes.Dataset.getURI());
		_datasetClass.add(DCAT.Dataset.getURI());
		_datasetClass.add(CUBE.DataSet.getURI());
	}
	
	
//	private Map<String, HTreeMap<String, Pair<Integer, Integer>>> graphDatasetURIs = new ConcurrentHashMap<String, HTreeMap<String, Pair<Integer, Integer>>>();
	private HTreeMap<String, Pair<Integer, Integer>> builder = MapDbFactory.createFilesystemDB().createHashMap("resource-base-uri-map").make();
	
	/**
	 * Default constructor
	 */
	public ResourceBaseURIOracle() {
		this.tblSubjectURIs = new ConcurrentHashMap<String, Integer>();
	}
	
	/**
	 * Adds a new hint for the heuristics, which consists of a particular triple of the resource 
	 * (i.e. a statement). The information contained in the provided statement will be used to do the guessing 
	 * @param statement One of the triples of the resource whose base URI is to be determined
	 */
	public void addHint(Quad statement) {
		
		// Extract the subject of the statement and its URI
		Node subject = statement.getSubject();
		Node predicate = statement.getPredicate();
		Node object = statement.getObject();
				
		// ...try to apply the first heuristic, which extracts the base URI from <> a void:Dataset/owl:Ontology statements
		if (!(this.tryExtractDatasetDecl(subject, predicate, object))){
			// Get the parent URI of the subject described by the statement
			String parentURI = this.extractParentURI(subject);
			
			// Add the parent URI to the table of subjects or update the corresponding entry if it's already there, do not 
			// add new rows if the maximum table size has been reached, but make sure that the declaredBaseURI is added if found
			if(parentURI != null && ((this.tblSubjectURIs.size() < this.maxSubjectURIs) || parentURI.equals(this.declaredResDatasetURI) )) {
				
				// Check if the current parent URI has already an entry in the table, if no, add it
				Integer curParentURICount = this.tblSubjectURIs.get(parentURI);
				this.tblSubjectURIs.put(parentURI, ((curParentURICount != null)?(curParentURICount + 1):(1)));
			}
		}
	}
	
	/**
	 * Provides the best guessing of the dataset URI that can be obtained, given the information provided up to now 
	 * (information provided through invocations to the addHint method.
	 * @return guess of the resource's dataset URI, null if no guess can be computed yet
	 */
	public String getEstimatedResourceDatasetURI() {
		
		// If a the base URI had been precisely determined, return it...
		if(this.declaredResDatasetURI != null) {
			logger.debug("Resource base URI defined in declaration: {}", this.declaredResDatasetURI);
			return this.declaredResDatasetURI;
		} else {
			
			// otherwise estimate the base URI according to the contents of the table of subject URIs
			int curMaxCount = -1;
			String curBestGuessURI = null;
			logger.debug("Estimating resource base URI...");
			
			for(Map.Entry<String, Integer> curHintEntry : this.tblSubjectURIs.entrySet()) {
				
				logger.debug("Checking subject URIs table entry: {} | {}", curHintEntry.getKey(), curHintEntry.getValue());

				// Keep track of the parent URI that appearead most frequently among the subjects
				if(curHintEntry.getValue() > curMaxCount) {
					curMaxCount = curHintEntry.getValue();
					curBestGuessURI = curHintEntry.getKey();
					logger.debug("Most frequent parent URI updated: {} count: {}", curBestGuessURI, curMaxCount);
				}
			}
			
			if (curBestGuessURI.equals("$builder")){
				String baseURI = EnvironmentProperties.getInstance().getBaseURI();
				int maxOcc = -1;
				Set<String> biggestTerms = new HashSet<String>();
				Iterator<String> set = this.builder.keySet().iterator();
				
				while(set.hasNext()){
					String s = set.next();
					if (maxOcc < this.builder.get(s).getSecondElement()){
						maxOcc = this.builder.get(s).getSecondElement();
						for(String st : biggestTerms){
							this.builder.remove(st);
						}
						biggestTerms = new HashSet<String>();
						biggestTerms.add(s);
					} else if (maxOcc == this.builder.get(s).getSecondElement()){
						biggestTerms.add(s);
					} else {
						this.builder.remove(s);
					}
				}
				
				String[] guessedURI = new String[this.builder.keySet().size()];
				for(String s : this.builder.keySet()){
					String term = s;
					if (s.endsWith("%")){
						int indexof = s.indexOf("%");
						term = s.substring(1,indexof - 1);
					}
					guessedURI[(this.builder.get(s).getFirstElement() - 1)] = term; 
				}
				
				curBestGuessURI = baseURI + "/" +Joiner.on("/").join(guessedURI);
			}

			this.lastGuessedBaseURI = curBestGuessURI;
			return curBestGuessURI;
		}
	}
	
	/**
	 * Returns the number of subjects found to be part of the URI specified as parameter
	 * @param subjectURI the parent URI for which the count of subjects belonging to it is to be returned
	 * @return number of subjects found to be part of the specified parent URI
	 */
	public int getBaseURICount() {
		
		String parentURI = ((this.declaredResDatasetURI != null)?(this.declaredResDatasetURI):(this.lastGuessedBaseURI));
		
		if(parentURI == null || parentURI.isEmpty()) {
			parentURI = this.getEstimatedResourceDatasetURI();
		}
		
		Integer subjectsCount = this.tblSubjectURIs.get(parentURI);
		
		return (subjectsCount != null)?(subjectsCount):(0);
	}

	/**
	 * First heuristic: if the dataset contains a statement declaring a URI to be of type void:Dataset or owl:Ontology, it is 
	 * known that this URI is indeed, the resource's base URI
	 * @param subject URI the statement talks about
	 * @param predicate the property of the subject being declared by the statement
	 * @param object the attribute assigned by the statement to the subject
	 * @return true if the heuristic succeeded in determining the base URI upon this invocation. False otherwise
	 */
	private boolean tryExtractDatasetDecl(Node subject, Node predicate, Node object) {
		// First level validation: all parts of the triple will be required
		if(subject != null && predicate != null && object != null) {
			
			// Second level validation: all parts of the triple must be URIs
			if(subject.isURI() && predicate.isURI() && object.isURI()) {
				// Check that the current quad corresponds to the dataset declaration, from which the dataset URI will be extracted...
				if(predicate.getURI().equals(RDF.type.getURI()) &&  _datasetClass.contains(object.getURI()) || object.getURI().equals(OWL.Ontology.getURI())) {
					// The URI of the subject of such quad, should be the resource's base URI. 
					this.declaredResDatasetURI = subject.getURI();					
//					if (!(this.dcmgr.existsInCache(DiachronCacheManager.DATASET_CACHE, subject.getURI()))){
//						CachedDatasetStatistics cds = new CachedDatasetStatistics(subject.getURI());
//						dcmgr.addToCache(DiachronCacheManager.DATASET_CACHE, subject.getURI(), cds);
//					}
					logger.debug("Resource base URI declared in triple: {} {} {}", subject, predicate, object);
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Extracts the parent part of a complete URI, such as the URI identifying the subject of a triple. That is, extracts 
	 * the last segment of the path of the subjec'ts URI and returns the remainder
	 * @param subject with a complete URI from which the parent URI will be extracted
	 * @return substring of the URI of the provided subject, corresponding to its parent URI
	 */
	public String extractParentURI(Node subject) {
		String ns = "";
		if (subject.isBlank()) return null;
		if (subject.isURI()){
			subject.getNameSpace();
		
			if ((ns.equals("")) || (subject.equals(ns))){
				//build ns
				String baseURI = EnvironmentProperties.getInstance().getBaseURI();
				String extractedURI = subject.getURI().replace(baseURI, "");
				String split[] = extractedURI.split("/");
				int counter = 1;
				for (String s : split){
					if (s.equals("")) continue;
					if (builder.containsKey(s)){
						Pair<Integer, Integer> p = builder.get(s);
						if (p.getFirstElement() == counter) {
							p.setSecondElement(p.getSecondElement() + 1);
							builder.put(s, p);
						} else {
							Pair<Integer, Integer> p2 = new Pair<Integer,Integer>(counter , 1);
							String marker = s+"%"+UUID.randomUUID()+"%";
							builder.putIfAbsent(marker,p2);
						}
					} else {
						Pair<Integer, Integer> p = new Pair<Integer,Integer>(counter , 1);
						builder.putIfAbsent(s,p);
					}
					counter++;
				}
				return "$builder";
			} else {
				return ns;
			}
		}
		return null;
		
	}
	
	/**
	 * Note: utilitarian method, useful for several metrics
	 * Tries to figure out the URI of the dataset wherefrom the quads were obtained. This is done by checking whether the 
	 * current quads corresponds to the rdf:type property stating that the resource is a void:Dataset, if so, the URI is extracted 
	 * from the corresponding subject and returned 
	 * @param quad Quad to be processed and examined to try to extract the dataset's URI
	 * @return URI of the dataset wherefrom the quad originated, null if the quad does not contain such information
	 */
	public static String extractDatasetURI(Quad quad) {
		// Get all parts of the quad required to analyze the quad
		Node subject = quad.getSubject();
		Node predicate = quad.getPredicate();
		Node object = quad.getObject();

		// First level validation: all parts of the triple will be required
		if(subject != null && predicate != null && object != null) {			
			// Second level validation: all parts of the triple must be URIs
			if(subject.isURI() && predicate.isURI() && object.isURI()) {
				// Check that the current quad corresponds to the dataset declaration, from which the dataset URI will be extracted...
				if(predicate.getURI().equals(RDF.type.getURI()) && _datasetClass.contains(object.getURI())) {
					// The URI of the subject of such quad, should be the dataset's URL. 
					// Try to calculate the latency associated to the current dataset
//					if (!(DiachronCacheManager.getInstance().existsInCache(DiachronCacheManager.DATASET_CACHE, subject.getURI()))){
//						CachedDatasetStatistics cds = new CachedDatasetStatistics(subject.getURI());
//						DiachronCacheManager.getInstance().addToCache(DiachronCacheManager.DATASET_CACHE, subject.getURI(), cds);
//					}
					return subject.getURI();
				}
			}
		}
		return null;
	}
	
	/**
	 * Extract the pay-level domain (also known simply as domain, for example http://bbc.co.uk) of the URI provided as parameter. 
	 * About URIs: The hierarchical part of the URI is intended to hold identification information hierarchical in nature. 
	 * If this part begins with a double forward slash ("//"), it is followed by an authority part and a path. 
	 * If it doesn't it contains only a path and thus it doesn't have a PLD (e.g. urns).
	 * @param resourceURI
	 * @return
	 */
	public static String extractPayLevelDomainURI(String resourceURI) {
		// Argument validation. Fail fast
		if(resourceURI == null) {
			return null;
		}
		
		int extract = 0;
		if (resourceURI.startsWith("http://")) extract = resourceURI.indexOf("/", 7);
		else if (resourceURI.startsWith("https://")) extract = resourceURI.indexOf("/", 8);
		
		if(extract == -1) extract = resourceURI.length();
		
		String matched = (resourceURI.startsWith("http://")) ? resourceURI.substring(7, extract) : resourceURI.substring(8, extract);
		
		return InternetDomainName.from(matched).topPrivateDomain().toString();
	}

}
