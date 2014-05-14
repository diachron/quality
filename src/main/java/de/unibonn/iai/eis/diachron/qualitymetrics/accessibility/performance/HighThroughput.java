package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.performance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;

/**
 * @author Santiago Londono
 * Estimates the efficiency with which a system can bind to the dataset, by measuring the number of 
 * answered HTTP requests responsed by the source of the dataset, per second.
 */
public class HighThroughput extends AbstractQualityMetric {
	
	private final Resource METRIC_URI = DQM.HighThroughputMetric;
	
	private static Logger logger = LoggerFactory.getLogger(HighThroughput.class);
	
	/**
	 * Amount of HTTP requests that will be sent to the data source in order to estimate how many requests are served per second. 
	 */
	private static final int NUM_HTTP_REQUESTS = 3;
	
	/**
	 * Holds the total delay as currently calculated by the compute method
	 */
	private long totalDelay = -1;

	/**
	 * Processes a single quad making part of the dataset. Firstly, tries to figure out the URI of the dataset wherefrom the quads were obtained. 
	 * A burst HTTP requests is sent to the dataset's URI and the number of requests sent is divided by the total time required to serve them,  
	 * thus obtaining the estimated number of requests server per second
	 * @param quad Quad to be processed and examined to try to extract the dataset's URI
	 */
	@Override
	public void compute(Quad quad) {
		// Get all parts of the quad required for the computation of this metric
		String datasetURI = LowLatency.extractDatasetURI(quad);

		// The URI of the subject of such quad, should be the dataset's URL. 
		// Try to calculate the total delay associated to the current dataset
		if(datasetURI != null) {
			totalDelay = HttpPerformanceUtil.measureReqsBurstDelay(datasetURI, NUM_HTTP_REQUESTS);
			logger.trace("Total delay for dataset {} was {}", datasetURI, totalDelay);
		}
	}

	/**
	 * Returns the current value of the High Throughput Metric as served requests per second, computed as the ration between the total 
	 * number of requests sent to the dataset's endpoint and the total time required to obtain their responses
	 * @return Current value of the High Throughput metric, measured with respect to the dataset's URI
	 */
	@Override
	public double metricValue() {
		return ((double)NUM_HTTP_REQUESTS)/((double)totalDelay);
	}

	@Override
	public Resource getMetricURI() {
		return METRIC_URI;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		return null;
	}

}
