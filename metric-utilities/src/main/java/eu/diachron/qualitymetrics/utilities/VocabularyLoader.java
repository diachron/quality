/**
 * 
 */
package eu.diachron.qualitymetrics.utilities;

import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedTriplesStream;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.Lock;
import org.apache.jena.util.iterator.Filter;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import de.unibonn.iai.eis.luzzu.semantics.utilities.Commons;
import eu.diachron.qualitymetrics.cache.CachedVocabulary;
import eu.diachron.qualitymetrics.cache.DiachronCacheManager;

/**
 * @author Jeremy Debattista
 * 
 * This helper class loads known vocabularies
 * into a Jena dataset.
 * 
 * In this package, we provide 53 non-propriatary
 * vocabularies that are used by at least 1% of 
 * the whole LOD Cloud. This list is compiled from
 * http://linkeddatacatalog.dws.informatik.uni-mannheim.de/state/#toc6
 * 
 */
public class VocabularyLoader {

	// --- Instance Variables --- //
	private static Logger logger = LoggerFactory.getLogger(VocabularyLoader.class);
	private static volatile VocabularyLoader instance = null;
	private static Object lock = new Object();


	// --- Vocabulary Storage and Cache --- //
	private DiachronCacheManager dcm = DiachronCacheManager.getInstance();
	private Dataset dataset = DatasetFactory.createGeneral();
	private ConcurrentMap<String, String> knownDatasets = new ConcurrentHashMap<String,String>();
	
	// --- LRU Caches --- //
    private ConcurrentMap<String, Boolean> termsExists = new ConcurrentLinkedHashMap.Builder<String, Boolean>().maximumWeightedCapacity(10000).build();
	private ConcurrentMap<String, Boolean> isPropertyMap = new ConcurrentLinkedHashMap.Builder<String, Boolean>().maximumWeightedCapacity(10000).build();
    private ConcurrentMap<String, Boolean> objectProperties = new ConcurrentLinkedHashMap.Builder<String, Boolean>().maximumWeightedCapacity(10000).build();
    private ConcurrentMap<String, Boolean> datatypeProperties = new ConcurrentLinkedHashMap.Builder<String, Boolean>().maximumWeightedCapacity(10000).build();
	private ConcurrentMap<String, Boolean> isClassMap = new ConcurrentLinkedHashMap.Builder<String, Boolean>().maximumWeightedCapacity(10000).build();
    private ConcurrentMap<String, Boolean> checkedDeprecatedTerm = new ConcurrentLinkedHashMap.Builder<String, Boolean>().maximumWeightedCapacity(10000).build();
    private ConcurrentMap<String, Set<RDFNode>> propertyDomains = new ConcurrentLinkedHashMap.Builder<String, Set<RDFNode>>().maximumWeightedCapacity(10000).build();
    private ConcurrentMap<String, Set<RDFNode>> propertyRanges = new ConcurrentLinkedHashMap.Builder<String, Set<RDFNode>>().maximumWeightedCapacity(10000).build();
    private ConcurrentMap<String, Set<RDFNode>> parentNodes = new ConcurrentLinkedHashMap.Builder<String, Set<RDFNode>>().maximumWeightedCapacity(10000).build();
    private ConcurrentMap<String, Set<RDFNode>> childNodes = new ConcurrentLinkedHashMap.Builder<String, Set<RDFNode>>().maximumWeightedCapacity(10000).build();
    private ConcurrentMap<String, Boolean> isIFPMap = new ConcurrentLinkedHashMap.Builder<String, Boolean>().maximumWeightedCapacity(10000).build();
    private ConcurrentMap<String, Set<RDFNode>> disjointWith = new ConcurrentLinkedHashMap.Builder<String, Set<RDFNode>>().maximumWeightedCapacity(10000).build();
    
    private ConcurrentMap<String, Boolean> failSafeMap = new ConcurrentLinkedHashMap.Builder<String, Boolean>().maximumWeightedCapacity(10000).build(); // A small fail-safe map that checks whether a domain retrieves vocabs.
    private ConcurrentMap<String, Integer> failSafeCounter = new ConcurrentLinkedHashMap.Builder<String, Integer>().maximumWeightedCapacity(10000).build(); // keeps a counter of the number of times an NS was accessed before putting the NS to the fail-safe map
    private final Integer NS_MAX_RETRIES = 3;

    

	// --- Constructor and Instance --- //
	private VocabularyLoader(){
		knownDatasets.put("http://dbpedia.org/ontology/","dbpedia.nt");
		knownDatasets.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#","rdf.rdf");
		knownDatasets.put("http://www.w3.org/2000/01/rdf-schema#","rdfs.rdf");
		knownDatasets.put("http://xmlns.com/foaf/0.1/","foaf.rdf");
		knownDatasets.put("http://purl.org/dc/terms/","dcterm.rdf");
		knownDatasets.put("http://www.w3.org/2002/07/owl#","owl.rdf");
		knownDatasets.put("http://www.w3.org/2003/01/geo/wgs84_pos#","pos.rdf");
		knownDatasets.put("http://rdfs.org/sioc/ns#","sioc.rdf");
//		knownDatasets.put("http://webns.net/mvcb/","admin.rdf");
		knownDatasets.put("http://www.w3.org/2004/02/skos/core#","skos.rdf");
		knownDatasets.put("http://rdfs.org/ns/void#","void.rdf"); //TODO update new namespace
		knownDatasets.put("http://purl.org/vocab/bio/0.1/","bio.rdf");
		knownDatasets.put("http://purl.org/linked-data/cube#","cube.ttl");
		knownDatasets.put("http://purl.org/rss/1.0/","rss.rdf");
		knownDatasets.put("http://www.w3.org/2000/10/swap/pim/contact#","w3con.rdf");
		knownDatasets.put("http://usefulinc.com/ns/doap#","doap.rdf");
		knownDatasets.put("http://purl.org/ontology/bibo/","bibo.rdf");
		knownDatasets.put("http://www.w3.org/ns/dcat#","dcat.rdf");
		knownDatasets.put("http://www.w3.org/ns/auth/cert#","cert.rdf");
		knownDatasets.put("http://purl.org/linked-data/sdmx/2009/dimension#","sdmxd.ttl");
		knownDatasets.put("http://www.daml.org/2001/10/html/airport-ont#","airport.rdf");
		knownDatasets.put("http://xmlns.com/wot/0.1/","wot.rdf");
//		knownDatasets.put("http://purl.org/rss/1.0/modules/content/","content.rdf");
		knownDatasets.put("http://creativecommons.org/ns#","cc.rdf");
		knownDatasets.put("http://purl.org/vocab/relationship/","ref.rdf");
//		knownDatasets.put("http://xmlns.com/wordnet/1.6/","wn.rdf");
		knownDatasets.put("http://rdfs.org/sioc/types#","tsioc.rdf");
		knownDatasets.put("http://www.w3.org/2006/vcard/ns#","vcard2006.rdf");
		knownDatasets.put("http://purl.org/linked-data/sdmx/2009/attribute#","sdmxa.ttl");
		knownDatasets.put("http://www.geonames.org/ontology#","gn.rdf");
		knownDatasets.put("http://data.semanticweb.org/ns/swc/ontology#","swc.rdf");
		knownDatasets.put("http://purl.org/dc/dcmitype/","dctypes.rdf");
		knownDatasets.put("http://purl.org/net/provenance/ns#","hartigprov.rdf");
		knownDatasets.put("http://www.w3.org/ns/sparql-service-description#","sd.rdf");
		knownDatasets.put("http://open.vocab.org/terms/","open.ttl");
		knownDatasets.put("http://www.w3.org/ns/prov#","prov.rdf");
		knownDatasets.put("http://purl.org/vocab/resourcelist/schema#","resource.rdf");
		knownDatasets.put("http://rdvocab.info/elements/","rda.rdf");
		knownDatasets.put("http://purl.org/net/provenance/types#","prvt.rdf");
		knownDatasets.put("http://purl.org/NET/c4dm/event.owl#","c4dm.rdf");
		knownDatasets.put("http://purl.org/goodrelations/v1#","gr.rdf");
		knownDatasets.put("http://www.w3.org/ns/auth/rsa#","rsa.rdf");
		knownDatasets.put("http://purl.org/vocab/aiiso/schema#","aiiso.rdf");
		knownDatasets.put("http://purl.org/net/pingback/","pingback.rdf");
		knownDatasets.put("http://www.w3.org/2006/time#","time.rdf");
		knownDatasets.put("http://www.w3.org/ns/org#","org.rdf");
		knownDatasets.put("http://www.w3.org/2007/05/powder-s#","wdrs.rdf");
		knownDatasets.put("http://www.w3.org/2003/06/sw-vocab-status/ns#","vs.rdf");
		knownDatasets.put("http://purl.org/vocab/vann/","vann.rdf");
		knownDatasets.put("http://www.w3.org/2002/12/cal/icaltzd#","icaltzd.rdf");
		knownDatasets.put("http://purl.org/vocab/frbr/core#","frbrcore.rdf");
		knownDatasets.put("http://www.w3.org/1999/xhtml/vocab#","xhv.rdf");
		knownDatasets.put("http://purl.org/vocab/lifecycle/schema#","lcy.rdf");
		knownDatasets.put("http://www.w3.org/2004/03/trix/rdfg-1/","rdfg.rdf");
		knownDatasets.put("http://schema.org/", "schema.rdf"); //added schema.org since it does not allow content negotiation
	}
	
	
	public static VocabularyLoader getInstance(){
		if (instance == null){
			synchronized(lock){
				logger.info("Creating Instance for Vocabulary Loader");
				instance = new VocabularyLoader();
			}
		}
		logger.info("Returning Instance for Vocabulary Loader");
		return instance;
	}
	
	// --- Vocabulary Loading Methods --- //
	public void loadVocabulary(String vocabURI){
		if(!(this.dataset.containsNamedModel(vocabURI))) 
			this.loadNStoDataset(vocabURI);
	}

	private synchronized void loadNStoDataset(String ns){
		if (this.knownDatasets.containsKey(ns)){
			Model m = RDFDataMgr.loadModel("vocabs/" + this.knownDatasets.get(ns));
			this.dataset.addNamedModel(ns, m);
		} else {
			//download and store in cache
			if (this.dcm.existsInCache(DiachronCacheManager.VOCABULARY_CACHE, ns)){
				try{
					CachedVocabulary cv = (CachedVocabulary) this.dcm.getFromCache(DiachronCacheManager.VOCABULARY_CACHE, ns);
					StringReader reader = new StringReader(cv.getTextualContent());
					Model m = ModelFactory.createOntologyModel();
					m.read(reader, ns, cv.getLanguage());
					this.dataset.addNamedModel(ns, m);
				}catch (ClassCastException cce){
					logger.error("Cannot cast {} " + ns);
				}
			} else {
				downloadAndLoadVocab(ns);
			}
		}
	}
	
	private synchronized void loadNStoDataset(String ns, Node term){
		if (this.knownDatasets.containsKey(ns)){
			Model m = RDFDataMgr.loadModel("vocabs/" + this.knownDatasets.get(ns));
			this.dataset.addNamedModel(ns, m);
		} else {
			//download and store in cache
			if (this.dcm.existsInCache(DiachronCacheManager.VOCABULARY_CACHE, ns)){
				try{
					CachedVocabulary cv = (CachedVocabulary) this.dcm.getFromCache(DiachronCacheManager.VOCABULARY_CACHE, ns);
					StringReader reader = new StringReader(cv.getTextualContent());
					Model m = ModelFactory.createOntologyModel();
					m.read(reader, ns, cv.getLanguage());
					this.dataset.addNamedModel(ns, m);
				}catch (ClassCastException cce){
					logger.error("Cannot cast {} " + ns);
				}
			} else {
				downloadAndLoadVocab(ns, term);
			}
		}
	}
	
	private synchronized void addToFailSafeDecision(String domainAuthority){
		if (this.failSafeCounter.containsKey(domainAuthority)){
			Integer current = this.failSafeCounter.get(domainAuthority) + 1;
			if (current == NS_MAX_RETRIES){
				this.failSafeMap.put(domainAuthority, true);
				this.failSafeCounter.put(domainAuthority, 0);
			} else {
				this.failSafeCounter.put(domainAuthority, current);
			}
		} else {
			this.failSafeCounter.putIfAbsent(domainAuthority, 0);

		}
	}
	
	private synchronized void updateFailSafeCache(String domainAuthority){
		if (this.failSafeCounter.containsKey(domainAuthority)){
				this.failSafeCounter.remove(domainAuthority);
		}	
	}
	
	
	
	private synchronized void downloadAndLoadVocab(final String ns) {
		String domAuth = "";
		try {
			URL domURL = new URL(ns);
			domAuth = domURL.getAuthority();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		
		if (!(this.failSafeMap.containsKey(domAuth))){
			try{
				ExecutorService executor = Executors.newSingleThreadExecutor();
				Model m = null;
				
				final Future<Model> handler = executor.submit(new Callable<Model>() {
				    @Override
				    public Model call() {
					    	logger.info("Loading {}", ns);
					    	Model m = null;
					    	try {
					    		m = RDFDataMgr.loadModel(ns);
					    	} catch (RiotException re) {
					    		logger.error(ns+" cannot be fetched using text/turtle. Trying to fetch data using application/rdf+xml");
					    		m = ModelFactory.createDefaultModel();
					    		StreamRDF dest = StreamRDFLib.graph(m.getGraph());
					    		RDFParser parser = RDFParser.source(ns).httpAccept("application/rdf+xml").build();
					    		parser.parse(dest);
					    	} catch (Exception e) {
					    		logger.error(ns+" cannot be fetched.");
					    	} 
					    	return m;
				    }
				});
				
				try {
					m = handler.get(5, TimeUnit.SECONDS);
					dataset.addNamedModel(ns, m);
					
					StringBuilderWriter writer = new StringBuilderWriter();
					m.write(writer, "TURTLE");
					
					CachedVocabulary cv = new CachedVocabulary();
					cv.setLanguage("TURTLE");
					cv.setNs(ns);
					cv.setTextualContent(writer.toString());
					
					dcm.addToCache(DiachronCacheManager.VOCABULARY_CACHE, ns, cv);
					updateFailSafeCache(domAuth); // if we manage to access a vocabulary, then we have to reset the cache counter
				} catch (Exception e)  {
					logger.error("Vocabulary {} could not be accessed.",ns);
					handler.cancel(true);
					addToFailSafeDecision(domAuth);
				} 
			} catch (Exception e){
				logger.error("Vocabulary {} could not be accessed.",ns);
				addToFailSafeDecision(domAuth);
	//			throw new VocabularyUnreachableException("The vocabulary <"+ns+"> cannot be accessed. Error thrown: "+e.getMessage());
			}
		} else {
			this.failSafeMap.get(domAuth);
		}
	}
	
	private synchronized void downloadAndLoadVocab(final String ns, final Node term) {
		String domAuth = "";
		try {
			URL domURL = new URL(ns);
			domAuth = domURL.getAuthority();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		
		if (!(this.failSafeMap.containsKey(domAuth))){
			try{
				ExecutorService executor = Executors.newSingleThreadExecutor();
				Model m = null;
				
				final Future<Model> handler = executor.submit(new Callable<Model>() {
				    @Override
				    public Model call() throws Exception {
				    	logger.info("Loading {}", ns);
				    	Model m = null;
				    	try{ m = RDFDataMgr.loadModel(term.getURI(), Lang.RDFXML); } catch (Exception e)
				    	{
				    		try{ m = RDFDataMgr.loadModel(term.getURI(), Lang.TURTLE); } catch (Exception e2)
				    		{
				    			try { m = RDFDataMgr.loadModel(term.getURI(), Lang.NTRIPLES); }
				    			catch (Exception e3){
				    				logger.error("Vocabulary {} could not be accessed after 3 attempts. " +  ns);
					    		}
				    		} 
				    	}
				    	return m;
				    }
				});
				
				try {
					m = handler.get(5, TimeUnit.SECONDS);
					dataset.addNamedModel(ns, m);
					
					StringBuilderWriter writer = new StringBuilderWriter();
					m.write(writer, "TURTLE");
					
					CachedVocabulary cv = new CachedVocabulary();
					cv.setLanguage("TURTLE");
					cv.setNs(ns);
					cv.setTextualContent(writer.toString());
					
					dcm.addToCache(DiachronCacheManager.VOCABULARY_CACHE, ns, cv);
					updateFailSafeCache(domAuth);
				} catch (Exception e)  {
					logger.error("Vocabulary {} could not be accessed.",ns);
					handler.cancel(true);
					addToFailSafeDecision(domAuth);

				} 
			} catch (Exception e){
				logger.error("Vocabulary {} could not be accessed.",ns);
//				throw new VocabularyUnreachableException("The vocabulary <"+ns+"> cannot be accessed. Error thrown: "+e.getMessage());
				addToFailSafeDecision(domAuth);
			}
		} else {
			this.failSafeMap.get(domAuth);
		}
	}
	
	public void clearDataset(){
		this.dataset.close();
		this.dataset = DatasetFactory.createGeneral();
	}
	
	// --- Vocabulary Helper Methods --- //
	
	public Boolean checkTerm(Node term){
		String ns = term.getNameSpace();
		
		if(!(dataset.containsNamedModel(ns))) loadNStoDataset(ns);
		return termExists(ns, term);
	}
	
    private Boolean termExists(String ns, Node term){
    	if (termsExists.containsKey(term.getURI())){
    		return termsExists.get(term.getURI());
    	} else {
			Model m = (getModelForVocabulary(term).size() > 0) ? getModelForVocabulary(term) : null;
			if (m == null) return false;
			
			m.enterCriticalSection(Lock.READ);
			try{
				if ((term.getNameSpace().startsWith(RDF.getURI())) && (term.getURI().matches(RDF.getURI()+"_[0-9]+"))){
					termsExists.putIfAbsent(term.getURI(),true);
				} else if (term.isURI()) {
					termsExists.putIfAbsent(term.getURI(), m.containsResource(Commons.asRDFNode(term)));
				}
			} finally {
				m.leaveCriticalSection();
			}
			return (termsExists.get(term.getURI()) == null) ? false : termsExists.get(term.getURI());
    	}
	}

	public Boolean knownVocabulary(String uri){
		return (knownDatasets.containsKey(uri) || dataset.containsNamedModel(uri));
	}
	
	public Model getModelForVocabulary(String ns){
		if(!(dataset.containsNamedModel(ns))) 
			loadNStoDataset(ns);
		
		return dataset.getNamedModel(ns);
	}
	
	public Model getModelForVocabulary(Node term){
		String ns = term.getURI();
		if (term.getURI().contains("#")){
			//it is a hash URI then we can use the namespace to download the vocabulary
			ns = term.getNameSpace();
		}
		
		if(!(dataset.containsNamedModel(ns))) 
			if (ns.contains("#")){
				//if it is a hash URI then we just need to download the schema in the namespace
				loadNStoDataset(ns);
			} else {
				loadNStoDataset(ns, term);
			}
		
		return dataset.getNamedModel(ns);
	}
	
	
	public boolean isProperty(Node term, boolean first){
//		String ns = term.getNameSpace();

		if (!isPropertyMap.containsKey(term.getURI())){
			Model m = (getModelForVocabulary(term).size() > 0) ? getModelForVocabulary(term) : null;
			if (m == null) return false;
			
			m.enterCriticalSection(Lock.READ);
			try{
				boolean isProperty = (m.contains(Commons.asRDFNode(term).asResource(), RDF.type, RDF.Property) ||
						m.contains(Commons.asRDFNode(term).asResource(), RDF.type, OWL.DatatypeProperty) ||
						m.contains(Commons.asRDFNode(term).asResource(), RDF.type, OWL.OntologyProperty) ||
						m.contains(Commons.asRDFNode(term).asResource(), RDF.type, OWL.AnnotationProperty) ||
						m.contains(Commons.asRDFNode(term).asResource(), RDF.type, OWL.ObjectProperty));
				
				if (!isProperty){
					//try inferring
					try{
						if (first){
							Node inferred = m.listObjectsOfProperty(Commons.asRDFNode(term).asResource(), RDF.type).next().asNode();
							
							if ((inferred.getURI().equals(OWL.Class.getURI())) || (inferred.getURI().equals(RDFS.Class.getURI())))
								isProperty = false;
							else
								isProperty = isProperty(inferred, false);
						}
					} catch (Exception e){}
				}
				
				isPropertyMap.putIfAbsent(term.getURI(), isProperty);
			} finally {
				m.leaveCriticalSection();
			}
		}
		
		return isPropertyMap.get(term.getURI());
	}
	
	public boolean isProperty(Node term){
		return isProperty(term, true);
	}
	
	public Boolean isObjectProperty(Node term, Boolean first){
//		String ns = term.getNameSpace();
		if (!objectProperties.containsKey(term.getURI())){
			Model m = (getModelForVocabulary(term).size() > 0) ? getModelForVocabulary(term) : null;
			if (m == null) return false;
						
			m.enterCriticalSection(Lock.READ);
			try{
				boolean isProperty = m.contains(Commons.asRDFNode(term).asResource(),  RDF.type, OWL.ObjectProperty);
				
				if (!isProperty){
					try{
						if (first){
							logger.debug("Trying to infer class");
							Node inferred = m.listObjectsOfProperty(Commons.asRDFNode(term).asResource(), RDF.type).next().asNode();
							
							if ((inferred.getURI().equals(OWL.Class.getURI())) || (inferred.getURI().equals(RDFS.Class.getURI())))
								isProperty = false;
							else
								isProperty = isObjectProperty(inferred,false);
						}
					} catch (Exception e){}
				}
				objectProperties.putIfAbsent(term.getURI(), isProperty);
			} finally {
				m.leaveCriticalSection();
			}
		}
		
		return objectProperties.get(term.getURI());
	}
	
	public Boolean isObjectProperty(Node term){
		return isObjectProperty(term,true);
	}
	
	public Boolean isDatatypeProperty(Node term, boolean first){
//		String ns = term.getNameSpace();
		if (!datatypeProperties.containsKey(term.getURI())){
			Model m = (getModelForVocabulary(term).size() > 0) ? getModelForVocabulary(term) : null;
			if (m == null) return false;
			
			m.enterCriticalSection(Lock.READ);
			try{
				boolean isProperty = m.contains(Commons.asRDFNode(term).asResource(),  RDF.type, OWL.DatatypeProperty);
				
				if (!isProperty){
					try{
						if (first){
							logger.debug("Trying to infer class");
							Node inferred = m.listObjectsOfProperty(Commons.asRDFNode(term).asResource(), RDF.type).next().asNode();
							
							if ((inferred.getURI().equals(OWL.Class.getURI())) || (inferred.getURI().equals(RDFS.Class.getURI())))
								isProperty = false;
							else
								isProperty = isDatatypeProperty(inferred,false);
						}
					} catch (Exception e){}
				}
				
				datatypeProperties.putIfAbsent(term.getURI(), isProperty);
			} finally {
				m.leaveCriticalSection();
			}
		}
		return datatypeProperties.get(term.getURI());
	}
	
	public Boolean isDatatypeProperty(Node term){
		return isDatatypeProperty(term,true);
	}
	
	public Boolean isClass(Node term, boolean first){
//		String ns = term.getNameSpace();
		if (!isClassMap.containsKey(term.getURI())){
			Model m = (getModelForVocabulary(term).size() > 0) ? getModelForVocabulary(term) : null;
			if (m == null) return false;
			
			m.enterCriticalSection(Lock.READ);
			try{
				boolean isClass = (m.contains(Commons.asRDFNode(term).asResource(), RDF.type,  OWL.Class) || m.contains(Commons.asRDFNode(term).asResource(), RDF.type,  RDFS.Class));
				
				if (!isClass){
					//try inferring
					try{
						if (first){
							logger.debug("Trying to infer class");
							Node inferred = m.listObjectsOfProperty(Commons.asRDFNode(term).asResource(), RDF.type).next().asNode();
							
							if ( (inferred.getURI().equals(RDF.Property.getURI())) ||
							(inferred.getURI().equals(OWL.DatatypeProperty.getURI())) ||
							(inferred.getURI().equals(OWL.OntologyProperty.getURI())) ||
							(inferred.getURI().equals(OWL.AnnotationProperty.getURI())) ||
							(inferred.getURI().equals(OWL.ObjectProperty.getURI())) ||
							(inferred.getURI().equals(OWL.FunctionalProperty.getURI()))
									) {
								// if the inferred class is one of the properties, then it is a property
								isClass = false;
							} else {
								isClass = isClass(inferred,true);
							}
						}
					} catch (Exception e){}
				}
				
				isClassMap.putIfAbsent(term.getURI(), isClass);
			} finally {
				m.leaveCriticalSection();
			}
		}
		return isClassMap.get(term.getURI());
	}
	
	public Boolean isClass(Node term){
		return isClass(term,true);
	}
	
	private Filter<RDFNode> deprecatedfilter = new Filter<RDFNode>() {
        @Override
        public boolean accept(RDFNode node) {
        	return ((node.equals(OWL.DeprecatedClass)) || (node.equals(OWL.DeprecatedProperty)));
        }
	};
	
	public boolean isDeprecatedTerm(Node term){
		if (checkedDeprecatedTerm.containsKey(term.getURI())) return checkedDeprecatedTerm.get(term.getURI());
		
//		String ns = term.getNameSpace();
		
		Model m = (getModelForVocabulary(term).size() > 0) ? getModelForVocabulary(term) : null;
		if (m == null) return false;
		
		m.enterCriticalSection(Lock.READ);
		try{ 
			Resource r = Commons.asRDFNode(term).asResource();
			boolean isDeprecated = m.listObjectsOfProperty(r, RDF.type).filterKeep(deprecatedfilter).hasNext();			
			//boolean isDeprecated = m.listObjectsOfProperty(r, RDF.type).filterKeep(o -> o.equals(OWL.DeprecatedClass) || o.equals(OWL.DeprecatedProperty)).hasNext();
			// CHECK: https://www.mkyong.com/java8/java-8-streams-filter-examples/
			checkedDeprecatedTerm.putIfAbsent(term.getURI(), isDeprecated);
		} finally {
			m.leaveCriticalSection();
		}
		return checkedDeprecatedTerm.get(term.getURI());
	}

	public Set<RDFNode> getPropertyDomain(Node term){
		if (propertyDomains.containsKey(term.getURI())) return propertyDomains.get(term.getURI());
		
//		String ns = term.getNameSpace();
		
		Set<RDFNode> set = new HashSet<RDFNode>();

		Model m = (getModelForVocabulary(term).size() > 0) ? getModelForVocabulary(term) : null;
		if (m == null) return set;
		
		m.enterCriticalSection(Lock.READ);
		try{ 
			Set<RDFNode> _tmp = m.listObjectsOfProperty(Commons.asRDFNode(term).asResource(), RDFS.domain).toSet();
			for (RDFNode node : _tmp){
				if (m.contains(node.asResource(), OWL.unionOf)){
					set.addAll(m.listObjectsOfProperty(node.asResource(), OWL.unionOf).toSet());
				} else {
					set.add(node);
				}
			}
			propertyDomains.putIfAbsent(term.getURI(), set);
		} finally {
			m.leaveCriticalSection();
		}
		
		return propertyDomains.get(term.getURI());
	}
	
	public Set<RDFNode> getPropertyRange(Node term){
		if (propertyRanges.containsKey(term.getURI())) return propertyRanges.get(term.getURI());
//		String ns = term.getNameSpace();
		
		Set<RDFNode> set = new HashSet<RDFNode>();

		Model m = (getModelForVocabulary(term).size() > 0) ? getModelForVocabulary(term) : null;
		if (m == null) return set;
		
		m.enterCriticalSection(Lock.READ);
		try{ 
			Set<RDFNode> _tmp = m.listObjectsOfProperty(Commons.asRDFNode(term).asResource(), RDFS.range).toSet();
			for (RDFNode node : _tmp){
				if (m.contains(node.asResource(), OWL.unionOf)){
					set.addAll(m.listObjectsOfProperty(node.asResource(), OWL.unionOf).toSet());
				} else {
					set.add(node);
				}
			}
			
			if (set.contains(RDFS.Literal)){
				set.add(XSD.xfloat);
				set.add(XSD.xdouble);
				set.add(XSD.xint);
				set.add(XSD.xlong);
				set.add(XSD.xshort);
				set.add(XSD.xbyte);
				set.add(XSD.xboolean);
				set.add(XSD.xstring);
				set.add(XSD.unsignedByte);
				set.add(XSD.unsignedShort);
				set.add(XSD.unsignedInt);
				set.add(XSD.unsignedLong);
				set.add(XSD.decimal);
				set.add(XSD.integer);
				set.add(XSD.nonPositiveInteger);
				set.add(XSD.nonNegativeInteger);
				set.add(XSD.positiveInteger);
				set.add(XSD.negativeInteger);
				set.add(XSD.normalizedString);
				set.add(XSD.date);
				set.add(XSD.dateTime);
				set.add(XSD.gDay);
				set.add(XSD.gMonth);
				set.add(XSD.gYear);
				set.add(XSD.gMonthDay);
				set.add(XSD.gYearMonth);
				set.add(XSD.hexBinary);
				set.add(XSD.language);
				set.add(XSD.time);
			}
			
			propertyRanges.putIfAbsent(term.getURI(), set);
		} finally {
			m.leaveCriticalSection();
		}

		return propertyRanges.get(term.getURI());
	}
	
	public Set<RDFNode> inferParentClass(Node term){
		if (parentNodes.containsKey(term.getURI())){
			return parentNodes.get(term.getURI());
		} else {
//			String ns = term.getNameSpace();

			Set<RDFNode> set = new LinkedHashSet<RDFNode>();
			
			Model m = (getModelForVocabulary(term).size() > 0) ? getModelForVocabulary(term) : null;
			if (m == null) return set;
			
			m.enterCriticalSection(Lock.READ);
			try{ 
				if (m != null){
					String query = "SELECT ?super { <"+term.getURI()+"> <"+RDFS.subClassOf.getURI()+">* ?super }";
					
					QueryExecution q = QueryExecutionFactory.create(query,m);
					ResultSet rs = q.execSelect();
					while(rs.hasNext()) set.add(rs.next().get("super"));
				}
				
				set.add(OWL.Thing);
				set.remove(Commons.asRDFNode(term));
				
				parentNodes.putIfAbsent(term.getURI(), set);
			} finally {
				m.leaveCriticalSection();
			}
			
			return parentNodes.get(term.getURI());
		}
	}
	
	public Set<RDFNode> inferParentProperty(Node term){
		if (parentNodes.containsKey(term.getURI())){
			return parentNodes.get(term.getURI());
		} else {
//			String ns = term.getNameSpace();
			Set<RDFNode> set = new LinkedHashSet<RDFNode>();

	
			Model m = (getModelForVocabulary(term).size() > 0) ?  getModelForVocabulary(term) : null;
			if (m == null) return set;
			
			m.enterCriticalSection(Lock.READ);
			try{ 
				if (m != null){
					String query = "SELECT ?super { <"+term.getURI()+"> <"+RDFS.subPropertyOf.getURI()+">* ?super }";

					QueryExecution q = QueryExecutionFactory.create(query,m);
					ResultSet rs = q.execSelect();
					while(rs.hasNext()) set.add(rs.next().get("super"));
				}
				
				parentNodes.putIfAbsent(term.getURI(), set);
			} finally {
				m.leaveCriticalSection();
			}
			
			return parentNodes.get(term.getURI());
		}
	}
	
	public Set<RDFNode> inferChildClass(Node term){
		if (childNodes.containsKey(term.getURI())){
			return childNodes.get(term.getURI());
		} else {
//			String ns = term.getNameSpace();
			Set<RDFNode> set = new LinkedHashSet<RDFNode>();

			Model m = (getModelForVocabulary(term).size() > 0) ? getModelForVocabulary(term) : null;
			if (m == null) return new LinkedHashSet<RDFNode>();
						
			m.enterCriticalSection(Lock.READ);
			try{ 
				String query = "SELECT ?child { ?child <"+RDFS.subClassOf.getURI()+">* <"+term.getURI()+"> }";
				
				QueryExecution q = QueryExecutionFactory.create(query,m);
				ResultSet rs = q.execSelect();
				while(rs.hasNext()) set.add(rs.next().get("child"));

				childNodes.putIfAbsent(term.getURI(), set);
			} finally {
				m.leaveCriticalSection();
			}
			return childNodes.get(term.getURI());
		}
	}
	
	public Set<RDFNode> inferChildProperty(Node term){
		if (childNodes.containsKey(term.getURI())){
			return childNodes.get(term.getURI());
		} else {
//			String ns = term.getNameSpace();
			Set<RDFNode> set = new LinkedHashSet<RDFNode>();

			Model m = (getModelForVocabulary(term).size() > 0) ?  getModelForVocabulary(term) : null;
			if (m == null) return new LinkedHashSet<RDFNode>();

			m.enterCriticalSection(Lock.READ);
			try{ 
				String query = "SELECT ?child { ?child <"+RDFS.subPropertyOf.getURI()+">* <"+term.getURI()+"> }";
				
				QueryExecution q = QueryExecutionFactory.create(query,m);
				ResultSet rs = q.execSelect();
				while(rs.hasNext()) set.add(rs.next().get("child"));
				
				childNodes.putIfAbsent(term.getURI(), set);
			} finally {
				m.leaveCriticalSection();
			}
			return childNodes.get(term.getURI());
		}
	}
	
	public boolean isInverseFunctionalProperty(Node term){
		
//		String ns = term.getNameSpace();
		
		if (!isIFPMap.containsKey(term.getURI())){
			Model m = (getModelForVocabulary(term).size() > 0) ? getModelForVocabulary(term) : null;
			if (m == null) return false;
			m.enterCriticalSection(Lock.READ);
			try{ 
				isIFPMap.putIfAbsent(term.getURI(), (m.contains(Commons.asRDFNode(term).asResource(), RDF.type, OWL.InverseFunctionalProperty)));
			} finally {
				m.leaveCriticalSection();
			}
		}
		
		return isIFPMap.get(term.getURI());
	}
	
	public Set<RDFNode> getDisjointWith(Node term){
		if (disjointWith.containsKey(term.getURI())){
			return disjointWith.get(term.getURI());
		} else {
//			String ns = term.getNameSpace();
			Model m = (getModelForVocabulary(term).size() > 0) ? getModelForVocabulary(term) : null;
			if (m == null) return new LinkedHashSet<RDFNode>();
			m.enterCriticalSection(Lock.READ);
			try{ 
				Set<RDFNode> set = new LinkedHashSet<RDFNode>(m.listObjectsOfProperty(Commons.asRDFNode(term).asResource(), OWL.disjointWith).toSet());
				
				Set<RDFNode> parent = new LinkedHashSet<RDFNode>(inferParentClass(term));
				parent.remove(OWL.Thing);
				for(RDFNode n : parent){
					if (n.isAnon()) continue;
					set.addAll(getDisjointWith(n.asNode()));
				}
				
				disjointWith.putIfAbsent(term.getURI(), set);
			} finally {
				m.leaveCriticalSection();
			}
			return disjointWith.get(term.getURI());
		}
	}
	
	// --- Deprecated Methods --- //
	private Map<Node, Set<RDFNode>> infParent = new HashMap<Node,Set<RDFNode>>();
	@Deprecated
	public Set<RDFNode> inferParent(Node term, Model m, boolean isSuperClass){
		
		if (infParent.containsKey(term)) return infParent.get(term);
		
		String query;
		Model _mdl = m;
		
		if (_mdl == null){
			String ns = term.getNameSpace();

			if(!(dataset.containsNamedModel(ns))) loadNStoDataset(ns);
			_mdl = dataset.getNamedModel(ns);
		}
		
		if (isSuperClass)
			query = "SELECT ?super { <"+term.getURI()+"> <"+RDFS.subClassOf.getURI()+">* ?super }";
		else
			query = "SELECT ?super { <"+term.getURI()+"> <"+RDFS.subPropertyOf.getURI()+">* ?super }";
		
		
		QueryExecution q = QueryExecutionFactory.create(query,_mdl);
		ResultSet rs = q.execSelect();
		Set<RDFNode> set = new LinkedHashSet<RDFNode>();
		while(rs.hasNext()) set.add(rs.next().get("super"));
		set.add(OWL.Thing);
				
		infParent.put(term, set);
		
		return set;
	}
	
	@Deprecated
	public Set<RDFNode> inferChildren(Node term, Model m, boolean isSuperClass){
		String query;
		Model _mdl = m;
		
		if (_mdl == null){
			String ns = term.getNameSpace();

			if(!(dataset.containsNamedModel(ns))) loadNStoDataset(ns);
			_mdl = dataset.getNamedModel(ns);
		}
		
		if (isSuperClass)
			query = "SELECT ?child { ?child <"+RDFS.subClassOf.getURI()+">* <"+term.getURI()+"> }";
		else
			query = "SELECT ?child { ?child <"+RDFS.subPropertyOf.getURI()+">* <"+term.getURI()+"> }";
		
		QueryExecution q = QueryExecutionFactory.create(query,_mdl);
		ResultSet rs = q.execSelect();
		Set<RDFNode> set = new HashSet<RDFNode>();
		while(rs.hasNext()) set.add(rs.next().get("child"));
		return set;
	}

	public Model getClassModelNoLiterals(Node term, Model m){
		String query  = "SELECT * { <"+term.getURI()+"> ?p ?o }";
		Model _mdl = m;
		Model _ret = ModelFactory.createDefaultModel();
		
		if (_mdl == null){
			String ns = term.getNameSpace();

			if(!(dataset.containsNamedModel(ns))) loadNStoDataset(ns);
			_mdl = dataset.getNamedModel(ns);
		}
		 
		
		QueryExecution q = QueryExecutionFactory.create(query,_mdl);
		ResultSet rs = q.execSelect();
		while(rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.get("o").isLiteral()) continue;
			else {
				Resource prop = qs.get("p").asResource();
				Resource obj = qs.getResource("o");
				_ret.add(Commons.asRDFNode(term).asResource(), _ret.createProperty(prop.getURI()), obj);
			}
			
		}
		
		return _ret;
	}
	
	@Deprecated
	public Model inferAncDec(Node term, Model m){
		Model _mdl = m;
		Model _ret = ModelFactory.createDefaultModel();
		
		if (_mdl == null){
			String ns = term.getNameSpace();

			if(!(dataset.containsNamedModel(ns))) loadNStoDataset(ns);
			_mdl = dataset.getNamedModel(ns);
		}
		
		String query = "SELECT ?super ?type { <"+term.getURI()+"> <"+RDFS.subClassOf.getURI()+"> ?super . ?super a ?type .}";
		QueryExecution q = QueryExecutionFactory.create(query,_mdl);
		ResultSet rs = q.execSelect();
		while(rs.hasNext()) {
			QuerySolution sol = rs.next();
			_ret.add(Commons.asRDFNode(term).asResource(), RDFS.subClassOf, sol.get("super"));
			_ret.add(sol.get("super").asResource(), RDF.type, sol.get("type"));
		}
		
		query = "SELECT ?child ?type { ?child <"+RDFS.subClassOf.getURI()+"> <"+term.getURI()+">  . ?child a ?type . }";
		q = QueryExecutionFactory.create(query,_mdl);
		rs = q.execSelect();
		while(rs.hasNext()) {
			QuerySolution sol = rs.next();
			_ret.add(sol.get("child").asResource(), RDFS.subClassOf,Commons.asRDFNode(term).asResource());
			_ret.add(sol.get("child").asResource(), RDF.type, sol.get("type"));

		}
		
		return _ret;
	}
	
	public static void main (String [] args){
//		Node n = ModelFactory.createDefaultModel().createResource("http://dbtropes.org/resource/Main/TheImp").asNode();
		Node n = ModelFactory.createDefaultModel().createResource("http://purl.org/eis/vocab/daq#computedOn").asNode();

		System.out.println(VocabularyLoader.getInstance().isProperty(n));
	}
}