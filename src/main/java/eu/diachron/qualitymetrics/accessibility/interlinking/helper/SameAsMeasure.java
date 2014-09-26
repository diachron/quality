/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.interlinking.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hp.hpl.jena.vocabulary.OWL;

import edu.uci.ics.jung.graph.DirectedGraph;

/**
 * @author Jeremy Debattista
 * 
 */

//The very common owl:sameAs property can be improperly asserted. One way to confirm a given sameAs relation 
//is correct is to find closed chains of sameAs relations between the linking resource and the resource linked. 
//This metric detects whether there are open sameAs chains in the network.
//
//Measure The metric counts the number of sameAs chains that are not closed. 
//Let pik = {eij1,...,ejyk} be a path of length y defined as a sequence of edges with the same label l(pik). 
//The number of open chains is defined as
//mpaths = ∥{pik | l(pik) = ”owl:sameAs”, k ̸= i}∥

public class SameAsMeasure {
	
	private DirectedGraph<String,RDFEdge> _graph;
	
	public SameAsMeasure(DirectedGraph<String,RDFEdge> _graph){
		this._graph = _graph;
	}
	

//	the closer to 0 the better
	public double getIdealMeasure(){
		double min = Double.MAX_VALUE;
		double max = 0.0;
		
		double ideal = 0.0;
		
		for(String v : _graph.getVertices()){
			double d = this.getMeasure(v);
			max = (max >= d) ? max : d ;
			min = (d < min) ? d : min;
			
			ideal += d;
		}
		
		if ((max-min) != 0) ideal = (ideal - min)/(max-min);
		
		return ideal;
	}
	
	public double getMeasure(String vertex){
		double measure = 0.0;
		
		List<String> path = this.getPath(vertex, Direction.BOTH); 

		if ((path.size() > 1) && (!path.get(path.size() - 1).equals(vertex))){
			measure++;
		}
		
		return measure;
	}
	
	
	private List<String> getPath(String vertex, Direction d){
		List<String> path = new ArrayList<String>();
		path.add(vertex);
		
		//check if there are any inEdges with owl:SameAs
		if ((d == Direction.IN) || (d == Direction.BOTH)){
			Collection<RDFEdge> inEdges = _graph.getInEdges(vertex);
		
			for(RDFEdge e : inEdges){
				if (e.getRDFEdge().equals(OWL.sameAs.getURI())){
					path.addAll(0, this.getPath(_graph.getSource(e), Direction.IN));
				}
			}
		}
		
		if ((d == Direction.OUT) || (d == Direction.BOTH)){
			Collection<RDFEdge> outEdges = _graph.getOutEdges(vertex);
		
			for(RDFEdge e : outEdges){
				if (e.getRDFEdge().equals(OWL.sameAs.getURI())){
					path.addAll((path.size() - 1), this.getPath(_graph.getDest(e), Direction.OUT));
				}
			}
		}
		
		return path;
	}
	
	
	private enum Direction{
		IN, OUT, BOTH
	}

}