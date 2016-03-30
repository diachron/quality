/**
 * 
 */
package eu.diachron.qualitymetrics.utilities;

import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

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

	private static Logger logger = LoggerFactory.getLogger(VocabularyLoader.class);

	private static DiachronCacheManager dcm = DiachronCacheManager.getInstance();
	private static Dataset dataset = DatasetFactory.createMem();
	private static ConcurrentMap<String, String> knownDatasets = new ConcurrentHashMap<String,String>();
	static {
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
	
//	private static DB mapDb = MapDbFactory.getMapDBAsyncTempFile();

	/**
	 * Checks if a term (Class or Property) exists in a vocabulary
	 * 
	 * @param term: Class or Property resource
	 */
	public static boolean checkTerm(Node term){
		String ns = term.getNameSpace();
		
		if(!(dataset.containsNamedModel(ns))) loadNStoDataset(ns);
		return termExists(ns, term);
	}
	
	public static void loadVocabulary(String vocabURI){
		if(!(dataset.containsNamedModel(vocabURI))) 
			loadNStoDataset(vocabURI);
	}
	
	private static void loadNStoDataset(String ns){
		if (knownDatasets.containsKey(ns)){
			//String filepath = VocabularyLoader.class.getClassLoader().getResource("vocabs/"+knownDatasets.get(ns)).getPath();
			Model m = ModelFactory.createOntologyModel().read("vocabs/" + knownDatasets.get(ns));
			//Model m = RDFDataMgr.loadModel("vocabs/" + knownDatasets.get(ns));
			dataset.addNamedModel(ns, m);
		} else {
			//download and store in cache
			if (dcm.existsInCache(DiachronCacheManager.VOCABULARY_CACHE, ns)){
				try{
					CachedVocabulary cv = (CachedVocabulary) dcm.getFromCache(DiachronCacheManager.VOCABULARY_CACHE, ns);
					StringReader reader = new StringReader(cv.getTextualContent());
					Model m = ModelFactory.createOntologyModel();
					m.read(reader, ns, cv.getLanguage());
					dataset.addNamedModel(ns, m);
				}catch (ClassCastException cce){
					logger.error("Cannot cast {} " + ns);
				}
			} else {
				downloadAndLoadVocab(ns);
			}
		}
	}
	
	private static void downloadAndLoadVocab(final String ns) {
		try{
			
			ExecutorService executor = Executors.newSingleThreadExecutor();
			Model m = null;
			
			final Future<Model> handler = executor.submit(new Callable<Model>() {
			    @Override
			    public Model call() throws Exception {
			    	logger.info("Loading {}", ns);
			    	Model m = ModelFactory.createOntologyModel().read(ns, Lang.RDFXML.getName());
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
			} catch (Exception e)  {
				logger.error("Vocabulary {} could not be accessed.",ns);
				handler.cancel(true);
			} 
		} catch (Exception e){
			logger.error("Vocabulary {} could not be accessed.",ns);
//			throw new VocabularyUnreachableException("The vocabulary <"+ns+"> cannot be accessed. Error thrown: "+e.getMessage());
		}
	}
	
	
    private static Map<String, Boolean> termsExists = new ConcurrentLinkedHashMap.Builder<String, Boolean>().maximumWeightedCapacity(10000).build();
    private static Boolean termExists(String ns, Node term){
    	if (termsExists.containsKey(term.getURI())){
    		return termsExists.get(term.getURI());
    	} else {
			Model m = dataset.getNamedModel(ns);
			
			if ((term.getNameSpace().startsWith(RDF.getURI())) && (term.getURI().matches(RDF.getURI()+"_[0-9]+"))){
				synchronized(termsExists) {
					termsExists.put(term.getURI(),true);
				}
				return true;
			}
			
			if (term.isURI()) {
				Resource r = m.createResource(term.getURI());
				synchronized(termsExists) {
					termsExists.put(term.getURI(), m.containsResource(r));
				}
				return termsExists.get(term.getURI());
			}
			return false;
    	}
	}
	
	public static void clearDataset(){
		dataset.close();
		dataset = DatasetFactory.createMem();
	}
	
	public static Boolean knownVocabulary(String uri){
		return (knownDatasets.containsKey(uri) || dataset.containsNamedModel(uri));
	}
	
	
	public static Model getModelForVocabulary(String ns){
		if(!(dataset.containsNamedModel(ns))) 
			loadNStoDataset(ns);
		
		return dataset.getNamedModel(ns);
	}
	
    private static Map<String, Boolean> isPropertyMap = new ConcurrentLinkedHashMap.Builder<String, Boolean>().maximumWeightedCapacity(10000).build();
	public static boolean isProperty(Node term){
		String ns = term.getNameSpace();
		
		
		if (!isPropertyMap.containsKey(term.getURI())){
			OntModel m = (getModelForVocabulary(ns).size() > 0) ? (OntModel) getModelForVocabulary(ns) : null;
			if (m == null) return false;
			synchronized(isPropertyMap) {
				isPropertyMap.put(term.getURI(),  
					((m.getDatatypeProperty(term.getURI()) != null) ||
					(m.getObjectProperty(term.getURI()) != null) ||
					(m.getOntProperty(term.getURI()) != null)));
			}
		}
		
		return isPropertyMap.get(term.getURI());
	}
	
    private static Map<String, Boolean> objectProperties = new ConcurrentLinkedHashMap.Builder<String, Boolean>().maximumWeightedCapacity(10000).build();
	public static boolean isObjectProperty(Node term){
		String ns = term.getNameSpace();
		
		if (!objectProperties.containsKey(term.getURI())){
			OntModel m = (getModelForVocabulary(ns).size() > 0) ? (OntModel) getModelForVocabulary(ns) : null;
			if (m == null) return false;
			synchronized(objectProperties) {
				objectProperties.put(term.getURI(), (m.getObjectProperty(term.getURI()) != null));
			}
		}
		
		return objectProperties.get(term.getURI());
	}
	
    private static Map<String, Boolean> datatypeProperties = new ConcurrentLinkedHashMap.Builder<String, Boolean>().maximumWeightedCapacity(10000).build();
	public static boolean isDatatypeProperty(Node term){
		String ns = term.getNameSpace();
		
		if (!datatypeProperties.containsKey(term.getURI())){
			OntModel m = (getModelForVocabulary(ns).size() > 0) ? (OntModel) getModelForVocabulary(ns) : null;
			if (m == null) return false;
			synchronized(datatypeProperties) {
				datatypeProperties.put(term.getURI(), (m.getDatatypeProperty(term.getURI()) != null));
			}
		}
		
		return datatypeProperties.get(term.getURI());
	}
	
	
    private static Map<String, Boolean> isClassMap =  new ConcurrentLinkedHashMap.Builder<String, Boolean>().maximumWeightedCapacity(10000).build();
    //(Map<String, Boolean>) Collections.synchronizedMap(new LRUMap<String, Boolean>(10000));
	public static boolean isClass(Node term){
		String ns = term.getNameSpace();
		
		if (!isClassMap.containsKey(term.getURI())){
			OntModel m = (getModelForVocabulary(ns).size() > 0) ? (OntModel) getModelForVocabulary(ns) : null;
			if (m == null) return false;
			synchronized(isClassMap) {
				isClassMap.put(term.getURI(),  (m.getOntClass(term.getURI()) != null));
			}
		}
		
		return isClassMap.get(term.getURI());
	}
	
	
	public static Filter<RDFNode> deprecatedfilter = new Filter<RDFNode>() {
        @Override
        public boolean accept(RDFNode node) {
        	return ((node.equals(OWL.DeprecatedClass)) || (node.equals(OWL.DeprecatedProperty)));
        }
	};
	
	//private static HTreeMap<String, Boolean> checkedDeprecatedTerm = MapDbFactory.createHashMap(mapDb, UUID.randomUUID().toString());
    private static Map<String, Boolean> checkedDeprecatedTerm = new ConcurrentLinkedHashMap.Builder<String, Boolean>().maximumWeightedCapacity(10000).build();
	public static boolean isDeprecatedTerm(Node term){
		if (checkedDeprecatedTerm.containsKey(term.getURI())) return checkedDeprecatedTerm.get(term.getURI());
		
		String ns = term.getNameSpace();
		
		if(!(dataset.containsNamedModel(ns))) loadNStoDataset(ns);
		
		OntModel m = (getModelForVocabulary(ns).size() > 0) ? (OntModel) getModelForVocabulary(ns) : null;
		if (m == null) return false;
		Resource r = Commons.asRDFNode(term).asResource();
		
		boolean isDeprecated = m.listObjectsOfProperty(r, RDF.type).filterKeep(deprecatedfilter).hasNext();
		synchronized(checkedDeprecatedTerm) {
			checkedDeprecatedTerm.put(term.getURI(), isDeprecated);
		}
		return isDeprecated;
	}

    private static Map<String, Set<RDFNode>> propertyDomains = new ConcurrentLinkedHashMap.Builder<String, Set<RDFNode>>().maximumWeightedCapacity(10000).build();
    
	public static Set<RDFNode> getPropertyDomain(Node term){
		if (propertyDomains.containsKey(term.getURI())) return propertyDomains.get(term.getURI());
		
		String ns = term.getNameSpace();
		
		Set<RDFNode> set = new HashSet<RDFNode>();

		if(!(dataset.containsNamedModel(ns))) loadNStoDataset(ns);
		OntModel m = (getModelForVocabulary(ns).size() > 0) ? (OntModel) getModelForVocabulary(ns) : null;
		if (m == null) return set;
		
		OntProperty op = ((OntModel) m).getOntProperty(term.getURI());
		if (op != null){
			List<? extends OntResource> domains = op.listDomain().toList();
			for (OntResource d : domains){
				if (d.asClass().isUnionClass()){
					UnionClass uc = d.asClass().asUnionClass();
					set.addAll(uc.getOperands().asJavaList());
				}
				else set.add(d.asResource());
			}
		}
		
		synchronized(propertyDomains) {
			propertyDomains.put(term.getURI(), set);
		}
		
		return set;
	}
	
    private static Map<String, Set<RDFNode>> propertyRanges = new ConcurrentLinkedHashMap.Builder<String, Set<RDFNode>>().maximumWeightedCapacity(10000).build();

	public static Set<RDFNode> getPropertyRange(Node term){
		if (propertyRanges.containsKey(term.getURI())) return propertyRanges.get(term.getURI());

		
		String ns = term.getNameSpace();
		
		Set<RDFNode> set = new HashSet<RDFNode>();

		if(!(dataset.containsNamedModel(ns))) loadNStoDataset(ns);
		OntModel m = (getModelForVocabulary(ns).size() > 0) ? (OntModel) getModelForVocabulary(ns) : null;
		if (m == null) return set;
		
		OntProperty op = ((OntModel) m).getOntProperty(term.getURI());
		
		if (op != null){
			List<? extends OntResource> domains = op.listRange().toList();
			for (OntResource d : domains){
				if (d.asClass().isUnionClass()){
					UnionClass uc = d.asClass().asUnionClass();
					set.addAll(uc.getOperands().asJavaList());
				}
				else set.add(d.asResource());
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
		
		synchronized(propertyRanges) {
			propertyRanges.put(term.getURI(), set);
		}
		
		return set;
	}
	
	
	private static Map<Node, Set<RDFNode>> infParent = new HashMap<Node,Set<RDFNode>>();
	@Deprecated
	public static Set<RDFNode> inferParent(Node term, Model m, boolean isSuperClass){
		
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
	
    private static Map<String, Set<RDFNode>> parentNodes = new ConcurrentLinkedHashMap.Builder<String, Set<RDFNode>>().maximumWeightedCapacity(10000).build();
	public static Set<RDFNode> inferParentClass(Node term){
		if (parentNodes.containsKey(term.getURI())){
			return parentNodes.get(term.getURI());
		} else {
			String ns = term.getNameSpace();

			
			Set<RDFNode> set = new LinkedHashSet<RDFNode>();
			if(!(dataset.containsNamedModel(ns))) loadNStoDataset(ns);
			
			OntModel m = (getModelForVocabulary(ns).size() > 0) ? (OntModel) getModelForVocabulary(ns) : null;
			if (m == null) return set;
			
			if (m != null){
				OntClass oc = m.getOntClass(term.getURI());
				if (oc != null)
					set.addAll(m.getOntClass(term.getURI()).listSuperClasses().toList());
			}
			
			set.add(OWL.Thing);
			
			synchronized(parentNodes) {
				parentNodes.put(term.getURI(), set);
			}
			
			return set;
		}
	}
	
	public static Set<RDFNode> inferParentProperty(Node term){
		if (parentNodes.containsKey(term.getURI())){
			return parentNodes.get(term.getURI());
		} else {
			String ns = term.getNameSpace();
	
			if(!(dataset.containsNamedModel(ns))) loadNStoDataset(ns);
			OntModel m = (getModelForVocabulary(ns).size() > 0) ? (OntModel) getModelForVocabulary(ns) : null;
			if (m == null) return new LinkedHashSet<RDFNode>();
			
			Set<RDFNode> set = new LinkedHashSet<RDFNode>(
					m.getOntProperty(term.getURI()).listSuperProperties().toList());
			
			synchronized(parentNodes) {
				parentNodes.put(term.getURI(), set);
			}

			return set;
		}
	}
	
    private static Map<String, Set<RDFNode>> childNodes = new ConcurrentLinkedHashMap.Builder<String, Set<RDFNode>>().maximumWeightedCapacity(10000).build();
	public static Set<RDFNode> inferChildClass(Node term){
		if (childNodes.containsKey(term.getURI())){
			return childNodes.get(term.getURI());
		} else {
			String ns = term.getNameSpace();

			if(!(dataset.containsNamedModel(ns))) loadNStoDataset(ns);
			OntModel m = (getModelForVocabulary(ns).size() > 0) ? (OntModel) getModelForVocabulary(ns) : null;
			if (m == null) return new LinkedHashSet<RDFNode>();
						
			
			Set<RDFNode> set = new LinkedHashSet<RDFNode>(
					m.getOntClass(term.getURI()).listSubClasses().toList());
			
			
			synchronized(childNodes) {
				childNodes.put(term.getURI(), set);
			}
			
			return set;	
		}
	}
	
	
	public static Set<RDFNode> inferChildProperty(Node term){
		if (childNodes.containsKey(term.getURI())){
			return childNodes.get(term.getURI());
		} else {
			String ns = term.getNameSpace();
	
			if(!(dataset.containsNamedModel(ns))) loadNStoDataset(ns);
			OntModel m = (getModelForVocabulary(ns).size() > 0) ? (OntModel) getModelForVocabulary(ns) : null;
			if (m == null) return new LinkedHashSet<RDFNode>();

			Set<RDFNode> set = new LinkedHashSet<RDFNode>(
					m.getOntProperty(term.getURI()).listSubProperties().toList());
			
			synchronized(childNodes) {
				childNodes.put(term.getURI(), set);
			}
			return set;
		}
	}
	
	@Deprecated
	public static Set<RDFNode> inferChildren(Node term, Model m, boolean isSuperClass){
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

    private static Map<String, Boolean> isIFPMap = new ConcurrentLinkedHashMap.Builder<String, Boolean>().maximumWeightedCapacity(10000).build();
	public static boolean isInverseFunctionalProperty(Node term){
		
		String ns = term.getNameSpace();
		
		if (!isIFPMap.containsKey(term.getURI())){
			OntModel m = (getModelForVocabulary(ns).size() > 0) ? (OntModel) getModelForVocabulary(ns) : null;
			if (m == null) return false;

			synchronized(isIFPMap) {
				isIFPMap.put(term.getURI(), (m.getInverseFunctionalProperty(term.getURI()) != null));
			}
		}
		
		return isIFPMap.get(term.getURI());
	}

	public static Model getClassModelNoLiterals(Node term, Model m){
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
	public static Model inferAncDec(Node term, Model m){
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
	
    private static Map<String, Set<RDFNode>> disjointWith = new ConcurrentLinkedHashMap.Builder<String, Set<RDFNode>>().maximumWeightedCapacity(10000).build();
	public static Set<RDFNode> getDisjointWith(Node term){
		if (disjointWith.containsKey(term.getURI())){
			return disjointWith.get(term.getURI());
		} else {
			String ns = term.getNameSpace();

			OntModel m = (getModelForVocabulary(ns).size() > 0) ? (OntModel) getModelForVocabulary(ns) : null;
			if (m == null) return new LinkedHashSet<RDFNode>();
			
			Set<RDFNode> set = new LinkedHashSet<RDFNode>(m.getOntClass(term.getURI()).listDisjointWith().toList());
			
			Set<RDFNode> parent = inferParentClass(term);
			parent.remove(OWL.Thing);
			for(RDFNode n : parent){
				if (n.isAnon()) continue;
				set.addAll(getDisjointWith(n.asNode()));
			}
			
			synchronized(disjointWith) {
				disjointWith.put(term.getURI(), set);
			}
			
			return set;	
		}
	}
}