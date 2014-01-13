package de.unibonn.iai.eis.diachron.qualitymetrics;

public interface QualityMetric {

	/*
	 * Input - Triple Output - value of the computed quality metric
	 */

	double compute();

	/*
	 * Returns the name of the quality Metric
	 */
	String getName();

	void postprocessing();

}
