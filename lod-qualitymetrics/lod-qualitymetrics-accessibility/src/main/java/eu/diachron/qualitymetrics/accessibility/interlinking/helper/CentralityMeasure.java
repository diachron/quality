/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.interlinking.helper;

import de.unibonn.iai.eis.diachron.commons.graphs.MapDBGraph;

/**
 * @author Jeremy Debattista
 * 
 */
public class CentralityMeasure {
	private MapDBGraph _graph;
	
	public CentralityMeasure(MapDBGraph _graph){
		this._graph = _graph;
	}
	
	//The lower the centrality measure, the better as having a high number of central points is prone to critical failure
	public double getIdealMeasure(){
		double min = Double.MAX_VALUE;
		double max = 0.0;
	
		// get the highest centrality measure found	
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
		
		//normalise our value between the minimum centrality and the maximum centrality found
		if (min-max != 0) 
			dCentrality = (dCentrality - min)/(max-min);
		else
			dCentrality = 0.5;
		
		//TODO: for problem report -> if dCentrality is closer to max, then it is a problem, maybe in the quality metadata we should show low/mid/high... 
		//...and show which of those nodes are very close to the max, therefore causing the data to have high centrality measure
		
		return dCentrality;
	}

	public double getMeasure(String vertex){
		double in = 0;
		double out = 0;
		
		for(String edge : _graph.getInEdgeNodes(vertex)){
			in += Math.max(1, _graph.getInEdgeNodes(edge).size());
		}
		
		for(String edge : _graph.getOutEdgeNodes(vertex)){
			out += Math.max(1, _graph.getOutEdgeNodes(edge).size());
		}
		
		return (in > 0 ? out / in : 0);
	}
}
