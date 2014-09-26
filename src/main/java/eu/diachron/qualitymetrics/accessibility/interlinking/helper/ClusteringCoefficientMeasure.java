/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.interlinking.helper;

import java.util.Map;

import edu.uci.ics.jung.algorithms.metrics.Metrics;
import edu.uci.ics.jung.graph.DirectedGraph;

/**
 * @author Jeremy Debattista
 * 
 */
public class ClusteringCoefficientMeasure {
	
	private DirectedGraph<String,RDFEdge> _graph;
	private Map<String, Double> clusterCoefficientMap;
	
	public ClusteringCoefficientMeasure(DirectedGraph<String,RDFEdge> _graph){
		this._graph = _graph;
		clusterCoefficientMap = Metrics.clusteringCoefficients(_graph);
	}
	
	// We do not want to have resources having clustering coefficient value of 1.. 
	// having all resources having a value of 1 means that every resource is linked together.
	// if ideal is 0, then it means that we have a lot of cohesion, whilst 1 means the exact opposite.
	// In this case, 0 doesn't mean that it is bad, and 1 doesn't mean it is good
	public double getIdealMeasure(){
		double ideal = 0.0;
		double summationCC = 0.0;
		for(String vertex : _graph.getVertices()){
			summationCC += this.getMeasure(vertex);
		}
		ideal = 1.0 - ((1.0 / _graph.getVertexCount()) * summationCC);
		return ideal;
	}

	public double getMeasure(String vertex){
		
//		Collection<String> neighbours = _graph.getNeighbors(vertex);
//		double c = 0;
//		if (neighbours.size() > 1) {
//			for (String neighbourA : neighbours)
//				for (String neighbourB : neighbours)
//					if (!neighbourA.equals(neighbourB) && _graph.containsEdge(_graph.findEdge(neighbourA, neighbourB)))
//						c++;
//			c = c / (neighbours.size() * (neighbours.size() - 1.0d));
//		}
//		return c;
		return this.clusterCoefficientMap.get(vertex);
	}
	
	// using jung, the clustering coeeficient is divided by 2 as in (aps.arxiv.org/abs/cond-mat/0303516) 
	// whilst gueret (http://jens-lehmann.org/files/2012/linked_mapping_qa.pdf0 does not
}
