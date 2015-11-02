/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.semanticaccuracy.helper;

import java.util.HashMap;
import java.util.HashSet;
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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import de.unibonn.iai.eis.diachron.datatypes.Pair;
import eu.diachron.qualitymetrics.utilities.VocabularyLoader;
import eu.diachron.qualitymetrics.utilities.exceptions.VocabularyUnreachableException;

/**
 * @author Jeremy Debattista
 * 
 * This class builds an in-memory partial graph to compute the similarity between two types.
 * Since a partial graph is being build, then the exact similarity may not be given as some
 * relationships given by the full graph might not be in this partial graph.
 * 
 * When building the partial graph, all ancestors and descendants of the passed resource types
 * are retrieved and added to the graph. Furthermore, the ancestors and descendants of the  
 * resource super classes, are also added to the graph.
 * 
 * 
 * Limitation: we can only work on lightweight ontologies mainly structured through the subclassof (isa) relationships
 * therefore our similarity engine cannot handle properties such as owl:equivalentClass. This might be done using
 * logic based semantic measures.
 * 
 * 
 */
@Deprecated
public class SimilarityComparator {

	private Resource r1_type,  r2_type;
    private static URIFactory FACTORY = URIFactoryMemory.getSingleton();
    private boolean isApproximate = false;
    
    @SuppressWarnings("unused")
	private Map<Pair<URI, URI>, Double> cachedCalculated = new HashMap<Pair<URI,URI>, Double>();

    
	/**
	 * Creates a Similarity Comparator for two types
	 * 
	 * @param r1_Type - Resource One Type
	 * @param r2_Type - Resource Two Type
	 */
	public SimilarityComparator(Resource r1_Type, Resource r2_Type, boolean isApproximate){
		this.r1_type = r1_Type;
		this.r2_type = r2_Type;
		this.isApproximate = isApproximate;
	}
	
    
	public double compute() throws SLIB_Ex_Critic, VocabularyUnreachableException{
		URI r1_uri = FACTORY.getURI(this.r1_type.getURI());
		URI r2_uri = FACTORY.getURI(this.r2_type.getURI());
		

		final G graph;
		if (isApproximate) graph = this.buildPartialMemoryGraph(r1_uri, r2_uri);
		else {
			Set<String> namespaces = new HashSet<String>();
			namespaces.add(this.r1_type.getNameSpace());
			namespaces.add(this.r2_type.getNameSpace());
			graph = this.buildMemoryGraph(namespaces);
		}
        SM_Engine engine = new SM_Engine(graph);

        ICconf icConf = new IC_Conf_Topo("Sanchez", SMConstants.FLAG_ICI_ZHOU_2008);

        SMconf smConf = new SMconf("Lin", SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_MAZANDU_2012); //Lin1998, Mazandu2012
        smConf.setICconf(icConf);
		
        double sim = engine.compare(smConf, r1_uri, r2_uri);
        
        return sim;
	}
	
	
	public static void main (String [] args) throws SLIB_Ex_Critic, VocabularyUnreachableException{
		Resource one = ModelFactory.createDefaultModel().createResource("http://dbpedia.org/ontology/Manga");
		Resource two = ModelFactory.createDefaultModel().createResource("http://dbpedia.org/ontology/WrittenWork");

		
		SimilarityComparator s = new SimilarityComparator(one, two, false);
		double d1 = s.compute();
		
		one = ModelFactory.createDefaultModel().createResource("http://dbpedia.org/ontology/ComicsCreator");
		two = ModelFactory.createDefaultModel().createResource("http://dbpedia.org/ontology/SoccerManager");
		s = new SimilarityComparator(one, two, false);
		double d2 = s.compute();
		
		System.out.println( 1 - ((d1 + d2) / 2) );
	}
	
	
	// Full Graph
	private final G buildMemoryGraph(Set<String> namespaces) throws VocabularyUnreachableException{
		
        URI graph_uri = FACTORY.getURI(this.r1_type.getNameSpace());

        G graph = new GraphMemory(graph_uri);

        
		for (String ns : namespaces){
			Model m = VocabularyLoader.getModelForVocabulary(ns);
			if (m.size() == 0){
				throw new VocabularyUnreachableException("Vocabulary " + ns + "could not be accessed");
			}
			for (Statement s : m.listStatements().toList()){
				if (s.getObject().isLiteral()) continue;
				if (s.getObject().isAnon()) continue;
				if (s.getSubject().isAnon()) continue;
				graph.addE(FACTORY.getURI(s.getSubject().getURI()), FACTORY.getURI(s.getPredicate().getURI()), FACTORY.getURI(s.getObject().asResource().getURI()));
			}
			
		}
		
		
		return graph;
	}
	
	@SuppressWarnings("unused")
	private final G buildMemoryGraph(String namespace) throws VocabularyUnreachableException{
		
        URI graph_uri = FACTORY.getURI(namespace);

        G graph = new GraphMemory(graph_uri);

        
		Model m = VocabularyLoader.getModelForVocabulary(namespace);
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
	
	
	// Partial Graph
	private final G buildPartialMemoryGraph(URI r1_uri, URI r2_uri) {

        Resource j_r1 = ModelFactory.createDefaultModel().createResource(this.r1_type.getURI());
        Resource j_r2 = ModelFactory.createDefaultModel().createResource(this.r2_type.getURI());

        URI graph_uri = FACTORY.getURI(this.r1_type.getNameSpace());

        G graph = new GraphMemory(graph_uri);

        graph.addV(r1_uri);
        graph.addV(r2_uri);

        
        addNodes(graph, r1_uri, this.r1_type.getURI(), VocabularyLoader.inferParent(j_r1.asNode(), null, true), true); // ancestors
        addNodes(graph, r2_uri, this.r2_type.getURI(), VocabularyLoader.inferParent(j_r2.asNode(), null, true), true); // ancestors
        addNodes(graph, r1_uri, this.r1_type.getURI(), VocabularyLoader.inferChildren(j_r1.asNode(), null, true), false); // descendants
        addNodes(graph, r2_uri, this.r2_type.getURI(), VocabularyLoader.inferChildren(j_r2.asNode(), null, true), false); // descendants
        
        addOtherNodes(graph, this.r1_type);
        addOtherNodes(graph, this.r2_type);
        
        Set<RDFNode> ancs = VocabularyLoader.inferParent(j_r1.asNode(), null, true);
        ancs.addAll(VocabularyLoader.inferParent(j_r2.asNode(), null, true));
        
        for(RDFNode n : ancs) {
        	addNodes(graph, FACTORY.getURI(n.toString()), n.toString(), VocabularyLoader.inferParent(n.asNode(), null, true), true);
        	addOtherNodes(graph, n.asResource());
        }

        Set<RDFNode> decs = VocabularyLoader.inferChildren(j_r1.asNode(), null, true);
        decs.addAll(VocabularyLoader.inferChildren(j_r2.asNode(), null, true));
        
        for(RDFNode n : decs){
        	addNodes(graph, FACTORY.getURI(n.toString()), n.toString(), VocabularyLoader.inferChildren(n.asNode(), null, true), false);
        	addOtherNodes(graph, n.asResource());
        }

        return graph;
    }
	
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
    
    private void addOtherNodes(G graph, Resource observed){
    	Model m = VocabularyLoader.getClassModelNoLiterals(observed.asNode(), null);
    	
    	for(Statement stmt : m.listStatements().toList()){
    		if (stmt.getPredicate().equals(com.hp.hpl.jena.vocabulary.RDFS.subClassOf)) continue;
    		graph.addE(FACTORY.getURI(observed.getURI()), 
    				FACTORY.getURI(stmt.getPredicate().getURI()),
    				FACTORY.getURI(stmt.getObject().asResource().getURI()));
    	}
    }
}
