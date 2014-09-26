/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.interlinking.helper;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.vocabulary.OWL;

import edu.uci.ics.jung.graph.DirectedGraph;

/**
 * @author Jeremy Debattista
 * 
 */
// This metric measures how much to the description of a resource is added through the use of sameAs edges. 
// If a sameAs edge is introduced, we can measure whether or not that edge adds to the description.

public class DescriptiveRichnessMeasure {
	
	private DirectedGraph<String,RDFEdge> _graph;
	
	public DescriptiveRichnessMeasure(DirectedGraph<String,RDFEdge> _graph){
		this._graph = _graph;
	}
	

	//the richer the resulting description, the lower the distance to our ideal.
	// for quality metrics since we need a value between 0.0 and 1.0, where 0 is worst and 1 is the best,
	// we normalised the value.
	public double getIdealMeasure(){
		double max = 0.0;
		double min = _graph.getVertexCount();
		
		double ideal = 0.0;
		
		for(String v : _graph.getVertices()){
			double d = this.getMeasure(v);
			ideal += (1 / (1 + d));
		}
		
		return Math.abs((ideal - min)/(max-min));
	}
	
	
	//Measure The measure counts the number of new edges brought to a resource through the sameAs relation(s).
	//This initial set of edges is defined as Ai = {eij | l(eij) ̸= ”owl:sameAs”,j ∈ Ni+} the number of edges brought to 
	//by the neighbours connected through a sameAs relation defined as
	//Bi = {ejl | vl ∈ Nj+, l ̸= i, eij ∈ Ni+, l(eij) = ”owl:sameAs”} Finally, the gain is the difference between the two sets
	//mdescription = Bi \ Ai

	public double getMeasure(String vertex){
		Set<String> descriptionNode = new HashSet<String>();
		Set<String> sameAsNode = new HashSet<String>();
		
		for(RDFEdge e : _graph.getOutEdges(vertex)){
			if (!(e.getRDFEdge().equals(OWL.sameAs.getURI()))) descriptionNode.add(_graph.getDest(e));
			else sameAsNode.add(_graph.getDest(e));
		}
		
		Set<String> enrichedDescriptionNodes = new HashSet<String>();
		for(String node : sameAsNode){
			for (RDFEdge e : _graph.getOutEdges(node)){
				enrichedDescriptionNodes.add(_graph.getDest(e));
			}
			enrichedDescriptionNodes.addAll(this.getOnlineDataset(node));
		}
		enrichedDescriptionNodes.remove(vertex);
		
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