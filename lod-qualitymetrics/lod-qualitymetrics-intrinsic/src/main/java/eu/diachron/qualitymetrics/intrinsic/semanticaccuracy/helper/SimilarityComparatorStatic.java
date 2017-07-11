/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.semanticaccuracy.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;

import slib.graph.model.graph.G;
import slib.graph.model.impl.graph.memory.GraphMemory;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.model.repo.URIFactory;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Topo;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.utils.SMConstants;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Ex_Critic;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import de.unibonn.iai.eis.diachron.datatypes.Pair;
import eu.diachron.qualitymetrics.utilities.VocabularyLoader;
import eu.diachron.qualitymetrics.utilities.exceptions.VocabularyUnreachableException;

/**
 * @author Jeremy Debattista
 * 
 */
public class SimilarityComparatorStatic {
    private static URIFactory FACTORY = URIFactoryMemory.getSingleton();
    private final G graph; 
    private SM_Engine engine;
    
    private ICconf icConf = new IC_Conf_Topo("Sanchez", SMConstants.FLAG_ICI_ZHOU_2008); // till eval 8 FLAG_ICI_SANCHEZ_2011
    private SMconf smConf = new SMconf("Lin", SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_MAZANDU_2012); // till eval 8 FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998
    
    private Map<Pair<URI, URI>, Double> cachedCalculated = new HashMap<Pair<URI,URI>, Double>();

    
	public SimilarityComparatorStatic() throws SLIB_Ex_Critic, VocabularyUnreachableException{
		graph = buildMemoryGraph("http://dbpedia.org/ontology/");
		engine = new SM_Engine(graph);
		smConf.setICconf(icConf);
	}
	
	public SimilarityComparatorStatic(String ici, String sim) throws SLIB_Ex_Critic, VocabularyUnreachableException{
		icConf = new IC_Conf_Topo("Sanchez", ici);
		smConf = new SMconf("Lin", sim);
		
		graph = buildMemoryGraph("http://dbpedia.org/ontology/");
		engine = new SM_Engine(graph);
		smConf.setICconf(icConf);
	}
	
    
	public double compute(Resource r1_type, Resource r2_type) throws SLIB_Ex_Critic, VocabularyUnreachableException{
		URI r1_uri = FACTORY.getURI(r1_type.getURI());
		URI r2_uri = FACTORY.getURI(r2_type.getURI());
		
		Pair<URI,URI> p = new Pair<URI,URI>(r1_uri,r2_uri);
		Pair<URI,URI> _p = new Pair<URI,URI>(r2_uri,r1_uri);

		if (cachedCalculated.containsKey(p)) return cachedCalculated.get(p);
		if (cachedCalculated.containsKey(_p)) return cachedCalculated.get(_p);

		
        double sim = engine.compare(smConf, r1_uri, r2_uri);
        
        cachedCalculated.put(p, sim);
        cachedCalculated.put(_p, sim);
        
        return sim;
	}
	
	
	
	// Full Graph
	private final G buildMemoryGraph(String namespace) throws VocabularyUnreachableException{
		
        URI graph_uri = FACTORY.getURI(namespace);

        G graph = new GraphMemory(graph_uri);

		Model m = VocabularyLoader.getInstance().getModelForVocabulary(namespace);
		if (m.size() == 0){
			throw new VocabularyUnreachableException("Vocabulary " + namespace + "could not be accessed");
		}
		for (Statement s : m.listStatements().toList()){
			if (s.getObject().isLiteral()) continue;
			if (s.getObject().isAnon()) continue;
			if (s.getSubject().isAnon()) continue;
			graph.addE(FACTORY.getURI(s.getSubject().getURI()), FACTORY.getURI(s.getPredicate().getURI()), FACTORY.getURI(s.getObject().asResource().getURI()));
		}
			
		return graph;
	}
	
	
    @SuppressWarnings("unused")
	private void addNodes(G graph, URI observed, String obsString, Set<RDFNode> ancDes, boolean isAnc){
        RDFNode term = ModelFactory.createDefaultModel().createResource(obsString);
    	ancDes.remove(term);

 	   for (RDFNode n : ancDes) {
            if (n.asResource().getURI().equals(obsString)) continue;

            URI node = FACTORY.getURI(n.toString());

            if (isAnc) graph.addE(observed, RDFS.SUBCLASSOF, node);
            else graph.addE(node, RDFS.SUBCLASSOF, observed);
        }
    }
    
    @SuppressWarnings("unused")
	private void addOtherNodes(G graph, Resource observed){
    	Model m = VocabularyLoader.getInstance().getClassModelNoLiterals(observed.asNode(), null);
    	
    	for(Statement stmt : m.listStatements().toList()){
    		if (stmt.getPredicate().equals(org.apache.jena.vocabulary.RDFS.subClassOf)) continue;
    		graph.addE(FACTORY.getURI(observed.getURI()), 
    				FACTORY.getURI(stmt.getPredicate().getURI()),
    				FACTORY.getURI(stmt.getObject().asResource().getURI()));
    	}
    }
}
