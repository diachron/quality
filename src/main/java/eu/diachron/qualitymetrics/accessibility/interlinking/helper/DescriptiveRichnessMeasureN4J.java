/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.interlinking.helper;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.tooling.GlobalGraphOperations;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.vocabulary.OWL;


/**
 * @author Jeremy Debattista
 * 
 */
// This metric measures how much to the description of a resource is added through the use of sameAs edges. 
// If a sameAs edge is introduced, we can measure whether or not that edge adds to the description.

public class DescriptiveRichnessMeasureN4J {
	
	private GraphDatabaseService graph;
	
	public DescriptiveRichnessMeasureN4J(final GraphDatabaseService _graph){
		this.graph = _graph;
	}
	

	//the richer the resulting description, the lower the distance to our ideal.
	// for quality metrics since we need a value between 0.0 and 1.0, where 0 is worst and 1 is the best,
	// we normalised the value.
	public double getIdealMeasure(){
		double max = 0.0;
		double min = 0.0;// _graph.getVertexCount();
		
		double ideal = 0.0;
		
		Iterator<Node> it = GlobalGraphOperations.at(graph).getAllNodes().iterator();
		
		while(it.hasNext()){
			double d = this.getMeasure(it.next());
			ideal += (1 / (1 + d));
			min++;
		}

		return Math.abs((ideal - min)/(max-min));
	}
	
	
	//Measure The measure counts the number of new edges brought to a resource through the sameAs relation(s).
	//This initial set of edges is defined as Ai = {eij | l(eij) ̸= ”owl:sameAs”,j ∈ Ni+} the number of edges brought to 
	//by the neighbours connected through a sameAs relation defined as
	//Bi = {ejl | vl ∈ Nj+, l ̸= i, eij ∈ Ni+, l(eij) = ”owl:sameAs”} Finally, the gain is the difference between the two sets
	//mdescription = Bi \ Ai

	public double getMeasure(Node node){
		Set<Node> descriptionNode = new HashSet<Node>();
		Set<Node> sameAsNode = new HashSet<Node>();
		
		
		Iterable<Relationship> _inRel = node.getRelationships(Direction.OUTGOING);
		Iterator<Relationship> iterator = _inRel.iterator();
		
		while(iterator.hasNext()){
			Relationship rel = iterator.next();
			if (rel.getProperty("value").toString().equals(OWL.sameAs.getURI())) descriptionNode.add(rel.getEndNode());
			else sameAsNode.add(rel.getEndNode());
		}
		
		
		Set<Node> enrichedDescriptionNodes = new HashSet<Node>();
		for(Node n : sameAsNode){
			_inRel = n.getRelationships(Direction.OUTGOING);
			iterator = _inRel.iterator();
			
			while(iterator.hasNext()){
				enrichedDescriptionNodes.add(iterator.next().getEndNode());
			}
			//enrichedDescriptionNodes.addAll(this.getOnlineDataset(node)); <-- activate it for online services
		}
		enrichedDescriptionNodes.remove(node);
		
		enrichedDescriptionNodes.removeAll(descriptionNode);
		//if we have x sameAs y . y hasProp z. this will return z
		
		return enrichedDescriptionNodes.size();
	}
	
	//TODO: fix to make it more efficient, using http retreiver?
	private Set<String> getOnlineDataset(String sameAsURI){
        Model model = ModelFactory.createDefaultModel();
        Set<String> enriched = new HashSet<String>();
        try{
	        model.read(sameAsURI);
	        NodeIterator iter = model.listObjects();
	        while(iter.hasNext()){
	        	enriched.add(iter.next().toString());
	        }
        } catch (Exception e) {}
        return enriched;
	}
}