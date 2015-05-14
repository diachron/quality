/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.interlinking.helper;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;

import de.unibonn.iai.eis.diachron.mapdb.MapDBGraph;



/**
 * @author Jeremy Debattista
 * 
 */
// This metric measures how much to the description of a resource is added through the use of sameAs edges. 
// If a sameAs edge is introduced, we can measure whether or not that edge adds to the description.

public class DescriptiveRichnessMeasure {
	
	private MapDBGraph _graph;
	
	public DescriptiveRichnessMeasure(MapDBGraph _graph){
		this._graph = _graph;
	}
	

	//the richer the resulting description, the lower the distance to our ideal - where the ideal (according to gueret) is 1.
	// for quality metrics since we need a value between 0.0 and 1.0, where 0 is worst and 1 is the best,
	// we normalised the value.
	public double getIdealMeasure(){
		double ideal = 0.0;
		
		for(String vertex : _graph.getSameAs().keySet()){
			double d = this.getMeasure(vertex);
			ideal += (1 / (1 + d));
		}
		
		return ideal; //TODO: to fix eventually when we decide what the real value of this metric is. ATM 1.0 represent a high value
		//according to gueret et al. the ideal is 1. the richer the description the lower value, therefore we do not need to normalise for our metrics.
	}
	
	
	//Measure The measure counts the number of new edges brought to a resource through the sameAs relation(s).
	//This initial set of edges is defined as Ai = {eij | l(eij) ̸= ”owl:sameAs”,j ∈ Ni+} the number of edges brought to 
	//by the neighbours connected through a sameAs relation defined as
	//Bi = {ejl | vl ∈ Nj+, l ̸= i, eij ∈ Ni+, l(eij) = ”owl:sameAs”} Finally, the gain is the difference between the two sets
	//mdescription = Bi \ Ai

	public double getMeasure(String vertex){
		//get all nodes for vertex
		Set<String> s_vertex = new HashSet<String>(_graph.getOutEdgeNodes(vertex));
		Set<String> s_v_sameAs = new HashSet<String>(_graph.getSameAsNodes(vertex));
		
		s_vertex.removeAll(s_v_sameAs); //remove all sameAs links
		
		Set<String> enrich = new HashSet<String>();
		for(String sameAs_v : s_v_sameAs){
			//for all sameAs nodes attached to vertex
			enrich.addAll(_graph.getOutEdgeNodes(sameAs_v));
			
			//enable http retreiver?
		}
		enrich.remove(vertex);
		
		//Compute set difference
		enrich.removeAll(s_vertex);
		
		return (double)enrich.size();
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