package de.unibonn.iai.eis.diachron.qualitymetrics;

import com.hp.hpl.jena.graph.Triple;

/**
 * @author jdebattist
 *
 */
public interface QualityMetric {

	/**
	 * This method should compute the metric. A
	 * @param The triple passed by the stream processor to the quality metric
	 */
	void compute(Triple triple);

	/**
	 * @return the name of the quality metric
	 */
	String getName();
	
	
	/**
	 * @return the value computed by the Quality Metric
	 */
	double metricValue();
}
