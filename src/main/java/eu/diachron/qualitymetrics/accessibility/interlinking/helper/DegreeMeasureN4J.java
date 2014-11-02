/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.interlinking.helper;

import java.util.Iterator;
import java.util.Map.Entry;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.tooling.GlobalGraphOperations;

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

public class DegreeMeasureN4J {
	
	private GraphDatabaseService graph;
	
	private DB mapDB = DBMaker.newTempFileDB().closeOnJvmShutdown().deleteFilesAfterClose().make();
	HTreeMap<Integer, Integer> kdegree = mapDB.createHashMap("clustering-coefficient-map").make();
	
	public DegreeMeasureN4J(final GraphDatabaseService _graph){
		this.graph = _graph;
	}

	// the lower the value i.e the lower the distance between the ideal and the actual, the better
	public double getIdealMeasure() {
		double ideal = 0.0;
		
		double min = Double.MAX_VALUE;
		double max = 0.0;
		
		//Create set of possible degrees
		double totalVertices = 1.0;
		
		Iterator<Node> it = GlobalGraphOperations.at(graph).getAllNodes().iterator();
		
		while(it.hasNext()){
			totalVertices++;
			int degree = it.next().getDegree();
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
	
	@Override
	protected void finalize() throws Throwable {
		// Destroy persistent HashMap and the corresponding database
		try {
			if(kdegree != null) {
				kdegree.close();
			}
			if(mapDB != null && !mapDB.isClosed()) {
				mapDB.close();
			}
		} catch(Throwable ex) {
		} finally {
			super.finalize();
		}
	}
}
