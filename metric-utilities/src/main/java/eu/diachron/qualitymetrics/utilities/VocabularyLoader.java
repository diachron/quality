/**
 * 
 */
package eu.diachron.qualitymetrics.utilities;

 import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
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
			Model m = RDFDataMgr.loadModel("vocabs/" + knownDatasets.get(ns));
			dataset.addNamedModel(ns, m);
		} else {
			//download and store in cache
			if (dcm.existsInCache(DiachronCacheManager.VOCABULARY_CACHE, ns)){
				try{
					CachedVocabulary cv = (CachedVocabulary) dcm.getFromCache(DiachronCacheManager.VOCABULARY_CACHE, ns);
					StringReader reader = new StringReader(cv.getTextualContent());
					Model m = ModelFactory.createDefaultModel();
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
	
	private static void downloadAndLoadVocab(String ns) {
		try{
			Model m = RDFDataMgr.loadModel(ns,Lang.RDFXML);
			dataset.addNamedModel(ns, m);
	
			StringBuilderWriter writer = new StringBuilderWriter();
			m.write(writer, "TURTLE");
			
			CachedVocabulary cv = new CachedVocabulary();
			cv.setLanguage("TURTLE");
			cv.setNs(ns);
			cv.setTextualContent(writer.toString());
			
			dcm.addToCache(DiachronCacheManager.VOCABULARY_CACHE, ns, cv);
		} catch (RiotException | HttpException e){
			logger.error("Vocabulary {} could not be accessed.",ns);
//			throw new VocabularyUnreachableException("The vocabulary <"+ns+"> cannot be accessed. Error thrown: "+e.getMessage());
		}
	}
	
	private static Boolean termExists(String ns, Node term){
		Model m = dataset.getNamedModel(ns);
		
		if ((term.getNameSpace().startsWith(RDF.getURI())) && (term.getURI().matches(RDF.getURI()+"_[0-9]+"))){
			return true;
		}
		
		if (term.isURI()) {
			Resource r = m.createResource(term.getURI());
			return m.containsResource(r);
		}
		return null;
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
	
	public static boolean isProperty(Node term){
		String ns = term.getNameSpace();
		
		Model m = getModelForVocabulary(ns);
		
		return (m.contains(m.createResource(term.getURI()), RDF.type, RDF.Property) 
				|| m.contains(m.createResource(term.getURI()), RDF.type, OWL.DatatypeProperty)
				|| m.contains(m.createResource(term.getURI()), RDF.type, OWL.ObjectProperty));
	}
	
	public static boolean isClass(Node term){
		String ns = term.getNameSpace();
		
		Model m = getModelForVocabulary(ns);
		
		return (m.contains(m.createResource(term.getURI()), RDF.type, RDFS.Class) || m.contains(m.createResource(term.getURI()), RDF.type, OWL.Class)) ;
	}
	
	public static Boolean checkTerm(Node term, boolean useCache){
		if (useCache) return checkTerm(term);
		else{
			Model m = null;
			try {
				m = RDFDataMgr.loadModel(term.getURI());
			} catch (Exception e){
				System.out.println(term.getURI() + " " + e.getLocalizedMessage());
				return false;
			}
			
			if ((term.getNameSpace().startsWith(RDF.getURI())) && (term.getURI().matches(RDF.getURI()+"_[0-9]+"))){
				return true;
			}
			
			if (term.isURI()) {
				Resource r = m.createResource(term.getURI());
				return m.containsResource(r);
			}
			
			return null;
		}
	}
	
	public static boolean isProperty(Node term, boolean useCache){
		if (useCache) return isProperty(term);
		else{
			Model m = null;
			try {
				m = RDFDataMgr.loadModel(term.getURI());
			} catch (Exception e){
				System.out.println(term.getURI() + " " + e.getLocalizedMessage());
				return false;
			}
			
			return (m.contains(m.createResource(term.getURI()), RDF.type, RDF.Property) 
					|| m.contains(m.createResource(term.getURI()), RDF.type, OWL.DatatypeProperty)
					|| m.contains(m.createResource(term.getURI()), RDF.type, OWL.ObjectProperty));
		}
	}
	
	public static boolean isDeprecatedTerm(Node term){
		
		String ns = term.getNameSpace();
		
		if(!(dataset.containsNamedModel(ns))) loadNStoDataset(ns);
		Model m = dataset.getNamedModel(ns);
		
		Resource r = Commons.asRDFNode(term).asResource();
		
		Filter<RDFNode> filter = new Filter<RDFNode>() {
	            @Override
	            public boolean accept(RDFNode node) {
	            	return ((node.equals(OWL.DeprecatedClass)) || (node.equals(OWL.DeprecatedProperty)));
	            }
		};

		return m.listObjectsOfProperty(r, RDF.type).filterKeep(filter).hasNext();
	}

	public static Set<RDFNode> getPropertyDomain(Node term){
		String ns = term.getNameSpace();
		
		if(!(dataset.containsNamedModel(ns))) loadNStoDataset(ns);
		Model m = dataset.getNamedModel(ns);
		
		Resource s = Commons.asRDFNode(term).asResource();
		
		Set<RDFNode> set = m.listObjectsOfProperty(s, RDFS.domain).toSet();
//		set.addAll(inferParent(term,m,true));
		return set;
	}
	
	public static Set<RDFNode> getPropertyRange(Node term){
		String ns = term.getNameSpace();
		
		if(!(dataset.containsNamedModel(ns))) loadNStoDataset(ns);
		Model m = dataset.getNamedModel(ns);
		
		Resource s = Commons.asRDFNode(term).asResource();
		
		Set<RDFNode> set = m.listObjectsOfProperty(s, RDFS.range).toSet();
//		set.addAll(inferParent(term,m,false));
		
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
		}
		
		return set;
	}
	
	
	public static Set<RDFNode> inferParent(Node term, Model m, boolean isSuperClass){
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
		Set<RDFNode> set = new HashSet<RDFNode>();
		set.add(OWL.Thing);
		set.add(RDFS.Resource);
		while(rs.hasNext()) set.add(rs.next().get("super"));
		return set;
	}
}