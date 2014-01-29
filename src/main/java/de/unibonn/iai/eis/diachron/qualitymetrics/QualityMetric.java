package de.unibonn.iai.eis.diachron.qualitymetrics;

import java.util.List;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * @author jdebattist
 *
 */
public interface QualityMetric {

	/**
	 * This method should compute the metric. A
	 * @param The triple passed by the stream processor to the quality metric
	 */
	void compute(Quad quad);

	/**
	 * @return the name of the quality metric
	 */
	String getName();
	
	
	/**
	 * @return the value computed by the Quality Metric
	 */
	double metricValue();
	
	/**
	 * This method will return daQ triples which will be stored in the dataset QualityGraph.
	 * @return a list of daQ triples
	 */
	List<Triple> toDAQTriples();
}
