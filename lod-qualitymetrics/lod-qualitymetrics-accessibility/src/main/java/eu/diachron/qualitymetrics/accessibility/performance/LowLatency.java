package eu.diachron.qualitymetrics.accessibility.performance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import eu.diachron.qualitymetrics.utilities.HTTPRetriever;

/**
 * @author Santiago Londono
 * Estimates the efficiency with which a system can bind to the dataset, by measuring the delay between 
 * the submission of a request for that very dataset and reception of the respective response (or part of it)
 */
public class LowLatency implements QualityMetric {
	
	private final Resource METRIC_URI = DQM.LowLatencyMetric;
	
	private static Logger logger = LoggerFactory.getLogger(LowLatency.class);
	
	/**
	 * Amount of HTTP requests that will be sent to the data source in order to determine its latency, the 
	 * resulting delays of all of these requests will be averaged to obtain the final latency measure
	 */
	private static final int NUM_HTTP_SAMPLES = 2;
	
	/**
	 * Holds the total delay as currently calculated by the compute method
	 */
	private long totalDelay = -1;
	
	
	/**
	 * Dataset PLD
	 */
	private String datasetURI = EnvironmentProperties.getInstance().getDatasetURI();;

	/**
	 * Holds the metric value
	 */
	private Double metricValue = null;
	
	/**
	 * Response time that is considered to be the ideal for a resource. In other words, its the amount of time in milliseconds below 
	 * which response times for resources will get a perfect score of 1.0. 
	 */
	private static final double NORM_TOTAL_RESPONSE_TIME = 750.0;

	/**
	 * Processes a single quad making part of the dataset. Firstly, tries to figure out the URI of the dataset wherefrom the quads were obtained. 
	 * This is done by checking whether the current quads corresponds to the rdf:type property stating that the resource is a void:Dataset, if so, 
	 * the URI is extracted from the corresponding subject. Some HTTP requests are sent to the dataset's URI and the response times are averaged to 
	 * obtain a measure of the latency 
	 * @param quad Quad to be processed and examined to try to extract the dataset's URI
	 */
	public void compute(Quad quad) {
		//nothing to compute
	}

	/**
	 * Returns the current value of the Low Latency Metric as a ranking in the range [0, 1], with 1.0 the top ranking. 
	 * It does so by computing the average of the time elapsed between the instant when a request is sent to the URI 
	 * of the dataset and the instant when any response is received. Then this average response time is normalized by dividing 
	 * NORM_TOTAL_RESPONSE_TIME, the ideal response time, by it
	 * @return Current value of the Low Latency metric, measured with respect to the dataset's URI
	 */
	public double metricValue() {
		if (this.metricValue == null){
			totalDelay = HTTPRetriever.measureReqsBurstDelay(datasetURI, NUM_HTTP_SAMPLES);
			logger.trace("Total delay for dataset {} was {}", datasetURI, totalDelay);

			double avgRespTime = ((double)totalDelay) / ((double)NUM_HTTP_SAMPLES);
			this.metricValue = Math.min(1.0, NORM_TOTAL_RESPONSE_TIME / avgRespTime);
		}
		return this.metricValue;
	}

	public Resource getMetricURI() {
		return METRIC_URI;
	}

	public ProblemList<?> getQualityProblems() {
		// Not implemented for this metric
		logger.debug("Quality problems not implemented for Low Latency metric");
		return null;
	}

	@Override
	public boolean isEstimate() {
		return false;
	}

	@Override
	public Resource getAgentURI() {
		return 	DQM.LuzzuProvenanceAgent;
	}
	
}
