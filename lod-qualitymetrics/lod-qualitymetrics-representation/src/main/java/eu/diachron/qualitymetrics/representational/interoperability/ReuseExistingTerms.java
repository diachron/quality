/**
 * 
 */
package eu.diachron.qualitymetrics.representational.interoperability;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.luzzu.assessment.ComplexQualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.AfterException;
import de.unibonn.iai.eis.luzzu.exceptions.BeforeException;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.utilities.LOVInterface;
import eu.diachron.qualitymetrics.utilities.SerialisableQuad;
import eu.diachron.qualitymetrics.utilities.VocabularyLoader;

/**
 * @author Jeremy Debattista
 * 
 * In this metric we assess if dataset reuse relevant terms 
 * for a particular domain. A dataset is deemed conformant to this metric
 * if it exhibits a higher overlap within the recommended vocabularies.
 * (Issue IX and X - Hogan et al. - An empirical survey of Linked Data Conformance)
 * 
 * This is a qualitative metric and thus require some extra input from
 * the user. This metric requires a trig file representing common vocabularies 
 * for a particular domain.
 * 
 * The value of this metric is the percentage of overlapping terms (i.e. the number
 * of overlapping terms / total number of terms)
 * 
 * For example:
 * <http://linkedgeodata.org> :hasDomain "Geographic" ;
 *  :hasVocabularies <http://www.w3.org/2003/01/geo/wgs84_pos#> , <http://www.geonames.org/ontology#> ;
 *  :getFromLOV "True" .
 */
public class ReuseExistingTerms implements ComplexQualityMetric {

	private Model categories = ModelFactory.createDefaultModel();
	private Set<String> topVocabs = new HashSet<String>();
	{
		// added the top 10 mostly used vocabs from 
		// linkeddatacatalog.dws.informatik.uni-mannheim.de/state/
		// and prefix.cc
		topVocabs.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		topVocabs.add("http://www.w3.org/2000/01/rdf-schema#");
		topVocabs.add("http://xmlns.com/foaf/0.1/");
		topVocabs.add("http://purl.org/dc/terms/");
		topVocabs.add("http://www.w3.org/2002/07/owl#");
		topVocabs.add("http://www.w3.org/2003/01/geo/wgs84_pos#");
		topVocabs.add("http://rdfs.org/sioc/ns#");
//		topVocabs.add("http://webns.net/mvcb/"); -- admin vocab does not work
		topVocabs.add("http://www.w3.org/2004/02/skos/core#");
		topVocabs.add("http://rdfs.org/ns/void#");
		topVocabs.add("http://www.w3.org/ns/dcat#"); // added dcat as it is becoming popular
	}
	private ConcurrentMap<String, Double> suggestedVocabs = new ConcurrentHashMap<String, Double>();
	
	private static Logger logger = LoggerFactory.getLogger(ReuseExistingTerms.class);
//	private SharedResources shared = SharedResources.getInstance();
	
	private Set<String> seenSet = MapDbFactory.getSingletonFileInstance(true).createHashSet(UUID.randomUUID().toString()).make();

	private Set<SerialisableQuad> _problemList = MapDbFactory.getSingletonFileInstance(true).createHashSet(UUID.randomUUID().toString()).make();


	private int overlapClasses = 0;
	private int overlapProperties = 0;
	
	private int totalClasses = 0;
	private int totalProperties = 0;
	
	@Override
	public void compute(Quad quad) {
		Node predicate = quad.getPredicate();
		
		if (predicate.hasURI(RDF.type.toString())){
			//it is a class
			Node object = quad.getObject();
			logger.info("checking class: {}", object.getURI());

			if (!(object.isBlank())){
				if (!(this.seenSet.contains(object.getURI()))){
					this.totalClasses++;
					if ((suggestedVocabs.containsKey(object.getNameSpace())) || (topVocabs.contains(object.getNameSpace()))){
						Boolean defined = VocabularyLoader.checkTerm(object);
						
						if (defined){
							this.overlapClasses++;
							double newVal = (suggestedVocabs.get(object.getNameSpace()) != null) ? suggestedVocabs.get(object.getNameSpace()) + 1.0 : 1.0;
							suggestedVocabs.put(object.getNameSpace(), newVal);
						}
					}
					this.seenSet.add(object.getURI());
				}
			}
		}
		
		if (!(this.seenSet.contains(predicate.getURI()))){
			this.totalProperties++;
			if ((suggestedVocabs.containsKey(predicate.getNameSpace())) || (topVocabs.contains(predicate.getNameSpace()))){
				// its a property from a suggested or top vocabulary
				logger.info("checking predicate: {}", predicate.getURI());
	
				Boolean defined = VocabularyLoader.checkTerm(predicate);
				
				if (defined){
					this.overlapProperties++;
					double newVal = (suggestedVocabs.get(predicate.getNameSpace()) != null) ? suggestedVocabs.get(predicate.getNameSpace()) + 1.0 : 1.0;
					suggestedVocabs.put(predicate.getNameSpace(), newVal);
				}
			}
			this.seenSet.add(predicate.getURI());
		}
	}

	@Override
	public double metricValue() {
		// calculating the overlapping terms of used suggested vocabularies
		double olt = ((double) this.overlapClasses + (double) this.overlapProperties) / ((double) this.totalClasses + (double) this.totalProperties) ;
	
		statsLogger.info("Reuse Existing Terms. Dataset: {} - Total # Overlap Classes : {}; # Overlap Properties : {}; # Total Classes : {}; # Total Properties : {}", 
				EnvironmentProperties.getInstance().getDatasetURI(), this.overlapClasses, this.overlapProperties, this.totalClasses, this.totalProperties);

		for(String s : suggestedVocabs.keySet()){
			if (suggestedVocabs.get(s) == 0.0){
				Quad q = new Quad(null, ModelFactory.createDefaultModel().createResource(s).asNode(), QPRO.exceptionDescription.asNode(), DQM.UnusedSuggestedVocabulary.asNode());
				this._problemList.add(new SerialisableQuad(q));
			}
		}
		return olt;
	}

	@Override
	public Resource getMetricURI() {
		return DQM.ReuseExistingTermsMetric;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		ProblemList<SerialisableQuad> pl = null;
		try {
			if(this._problemList != null && this._problemList.size() > 0) {
				pl = new ProblemList<SerialisableQuad>(this._problemList);
			} else {
				pl = new ProblemList<SerialisableQuad>();
			}
		} catch (ProblemListInitialisationException e) {
			logger.error(e.getMessage());
		}
		return pl;
	}

	@Override
	public boolean isEstimate() {
		return false;
	}

	@Override
	public Resource getAgentURI() {
		return 	DQM.LuzzuProvenanceAgent;
	}

	/** 
	 * The before method in this method should accept only
	 * a string with the trig filename 
	 */
	@Override
	public void before(Object... args) throws BeforeException {
		if (args == null || args.length <= 0 || args[0] == null){
			logger.error("Argument in the Reuse Existing Vocabulary metric should not be null.");
			throw new BeforeException("Argument should not be a string with a filename.");
		}
		String fileName = (String) args[0];
		
		try{
			categories = RDFDataMgr.loadModel(fileName);
		} catch (RiotException e){
			logger.error("Error loading configuration file: {}", fileName);
			throw new BeforeException("Invalid configuration file passed to the Reuse Existing Vocabulary metric. The configuration file should be RDF " + 
					"(or any other RDF serialisation format), having this following format: <http://linkedgeodata.org> :hasDomain \"geographic\"^^xsd:string ; " + 
					" :hasVocabularies <http://www.w3.org/2003/01/geo/wgs84_pos#> , <http://www.geonames.org/ontology#> .  ");
		}
		
		//Get dataset/base URI 
		String baseURI = EnvironmentProperties.getInstance().getBaseURI();
		if (baseURI == null){
			//Try using the dataset URI
			baseURI = EnvironmentProperties.getInstance().getBaseURI();
			if (baseURI == null){
				logger.error("Unknown Dataset URI");
				throw new BeforeException("Dataset URI is not known. This should be set in Luzzu.");
			}
		}
		
		//Load Vocabularies
		logger.info("Getting vocabularies from user configuration file for {}",baseURI);
		List<RDFNode> lst = categories.listObjectsOfProperty(categories.createResource(baseURI), categories.createProperty(":hasVocabularies")).toList();
		logger.info("Found {} vocabularies", lst.size());
		for(RDFNode n : lst){
			logger.info("Loading {}", n.toString());
			VocabularyLoader.loadVocabulary(n.toString());
			suggestedVocabs.put(n.toString(),0.0);
		}
		
		List<RDFNode> lov = categories.listObjectsOfProperty(categories.createResource(baseURI), categories.createProperty(":getFromLOV")).toList();
		boolean getFromLov = false;
		if (lov.size() > 0) getFromLov = lov.get(0).asLiteral().getBoolean();
		
		if (getFromLov){
			List<RDFNode> domainNode = categories.listObjectsOfProperty(categories.createResource(baseURI), categories.createProperty(":hasDomain")).toList();
			for(RDFNode n : domainNode){
				try {
					List<String> vocabs = LOVInterface.getKnownVocabsPerDomain(n.asLiteral().getString());
					for(String v : vocabs) suggestedVocabs.put(v, 0.0);
				} catch (IOException e) {
					logger.error("Could not load vocabularies from LOD. Error Message: {}", e.getMessage());
				}
			}
		}
	}

	@Override
	public void after(Object... args) throws AfterException {
		// nothing to do
	}

}
