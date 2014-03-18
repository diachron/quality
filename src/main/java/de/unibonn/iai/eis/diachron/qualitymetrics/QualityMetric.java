package de.unibonn.iai.eis.diachron.qualitymetrics;

import java.util.List;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * @author jdebattist
 * 
 */
public interface QualityMetric {

	/**
	 * This method should compute the metric.
	 * 
	 * @param The
	 *            Quad <s,p,o,c> passed by the stream processor to the quality
	 *            metric
	 */
	void compute(Quad quad);

	/**
	 * @return the value computed by the Quality Metric
	 */
	double metricValue();

	/**
	 * This method will return daQ triples which will be stored in the dataset
	 * QualityGraph.
	 * 
	 * @return a list of daQ triples
	 */
	List<Triple> toDAQTriples();

	/**
	 * @return returns the daQ URI of the Quality Metric
	 */
	Resource getMetricURI();
}
