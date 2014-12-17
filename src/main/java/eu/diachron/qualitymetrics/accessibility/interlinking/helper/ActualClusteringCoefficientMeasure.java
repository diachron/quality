/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.interlinking.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.unibonn.iai.eis.diachron.commons.graphs.MapDBGraph;

/**
 * @author Jeremy Debattista
 * 
 */
public class ActualClusteringCoefficientMeasure {
	private MapDBGraph _graph;
    private Map<String,Double> coefficients = new HashMap<String,Double>();

	
	
	public ActualClusteringCoefficientMeasure(MapDBGraph _graph){
		this._graph = _graph;
		this.calculateCoefficiency();
	}
	
	public double getIdealMeasure(){
		double ideal = 0.0;
		double summationCC = 0.0;
		for(String vertex : _graph.getVertices()){
			summationCC += this.coefficients.get(vertex);
		}
		ideal = 1.0 - ((1.0 / _graph.getVertexCount()) * summationCC);
		return ideal;
	}

	private void calculateCoefficiency(){
        for (String v : _graph.getVertices())
        {
        	Set<String> v_neighbours = _graph.getNeighbors(v);
            int n = v_neighbours.size();
            if (n < 2)
                coefficients.put(v, new Double(0));
            else
            {
                // how many of v's neighbors are connected to each other?
                ArrayList<String> neighbors = new ArrayList<String>(v_neighbours);
                double edge_count = 0;
                for (int i = 0; i < n; i++)
                {
                    String w = neighbors.get(i);
                    Set<String> w_neighbours = _graph.getNeighbors(w);
                    for (int j = i+1; j < n; j++ )
                    {
                        String x = neighbors.get(j);
                        edge_count += w_neighbours.contains(x) ? 1 : 0;
                    }
                }
                double possible_edges = (n * (n - 1))/2.0;
                coefficients.put(v, new Double(edge_count / possible_edges));
            }
        }
	}
}
