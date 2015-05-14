/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.interlinking.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.unibonn.iai.eis.diachron.mapdb.MapDBGraph;
import eu.diachron.qualitymetrics.accessibility.interlinking.helper.PolynomialFitter.Polynomial;

/**
 * 
 * This class follows the Degree Measure description
 * according to Assessing Linked Data Mappings using 
 * Network Measures
 * 
 * http://jens-lehmann.org/files/2012/linked_mapping_qa.pdf
 * 
 */

public class DegreeMeasure {
	
	private MapDBGraph _graph;
	
	public DegreeMeasure(MapDBGraph _graph){
		this._graph = _graph;
	}

	// the lower the value i.e the lower the distance between the ideal and the actual, the better
	public double getIdealMeasure() {
		double ideal = 0.0;
		
		double min = Double.MAX_VALUE;
		double max = 0.0;

		//Create set of possible degrees
		Map<Integer,Integer> kdegree = new HashMap<Integer,Integer>();
		for(String vertex : _graph.getVertices()){
			int degree = _graph.degree(vertex);
			if (degree == 0) continue;
			if (kdegree.containsKey(degree)) {
				int times = kdegree.get(degree);
				times++;
				kdegree.put(degree, times);
			} else kdegree.put(degree, 1);
		}
		
		PolynomialFitter poly = new PolynomialFitter(0);
		
		//get the best value for k in c.k^-gamma
		for (Entry<Integer, Integer> entry : kdegree.entrySet()){
			poly.addPoint(Math.log(entry.getKey()), Math.log(entry.getValue())); //degree, no.of times
		}
		Polynomial p = poly.getBestFit();
		
		double totalVertices = _graph.getVertexCount() + 1;
//		double totalDegreeSet = kdegree.keySet().size();
		
		for (int degree : kdegree.keySet()){
			double ratio_DegSet_Ver = kdegree.get(degree) / totalVertices;
			double powerLawK = Math.exp(p.getY(Math.log(degree)));
			
			double d = Math.abs(ratio_DegSet_Ver - powerLawK);
			
			min = (degree < min) ? degree : min;
			max = (max >= degree) ? max : degree ;
			
			ideal += d;
		}
		if (kdegree.keySet().size() == 1) min = max;
		
		if (min-max != 0) 
			ideal = (ideal - min)/(max-min);
		else
			ideal = 0.5;
		
		return Math.abs(ideal);
	}
}
