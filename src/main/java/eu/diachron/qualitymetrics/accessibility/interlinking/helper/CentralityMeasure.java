/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.interlinking.helper;

import edu.uci.ics.jung.graph.DirectedGraph;

/**
 * @author Jeremy Debattista
 * 
 */
public class CentralityMeasure {
	
	private DirectedGraph<String,RDFEdge> _graph;
	
	public CentralityMeasure(DirectedGraph<String,RDFEdge> _graph){
		this._graph = _graph;
	}
	
	//The lower the centrality measure, the better as having a high number of central points is prone to critical failure
	public double getIdealMeasure(){
		double min = Double.MAX_VALUE;
		double max = 0.0;
		
		for(String v : _graph.getVertices()){
			double d = this.getMeasure(v);
			max = (max >= d) ? max : d ;
			min = (d < min) ? d : min;
		}
		
		int totalVer = _graph.getVertexCount() - 1;
		double dCentrality = 0.0;
		for(String v : _graph.getVertices()){
			double d = this.getMeasure(v);
			dCentrality += ((max - d)/totalVer);
		}
		
		//normalise
		if (min-max != 0) 
			dCentrality = (dCentrality - min)/(max-min);
		else
			dCentrality = 0.5;
		
		return dCentrality;
	}

	public double getMeasure(String vertex){
		double in = 0;
		double out = 0;
		
		for(RDFEdge edge : _graph.getInEdges(vertex)){
			String v = _graph.getSource(edge);
			in += Math.max(1, _graph.getInEdges(v).size());
		}
		
		for(RDFEdge edge : _graph.getOutEdges(vertex)){
			String v = _graph.getDest(edge);
			out += Math.max(1, _graph.getOutEdges(v).size());
		}
		
		return (in > 0 ? out / in : 0);
	}
}
