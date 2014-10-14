package eu.diachron.qualitymetrics.accessibility.performance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import eu.diachron.qualitymetrics.accessibility.availability.ResourceBaseURIOracle;
import eu.diachron.qualitymetrics.utilities.HTTPRetriever;
import eu.diachron.semantics.vocabulary.DQM;

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
	 * Flag stating whether the metric has been computed. This metric should be computed once, for the dataset's URI,
	 * but the compute method is run for every quad in the dataset. This flag prevents the metric from being computed per quad
	 */
	private boolean hasBeenComputed = false;
	
	/**
	 * Response time that is considered to be the ideal for a resource. In other words, its the amount of time in milliseconds below 
	 * which response times for resources will get a perfect score of 1.0. 
	 */
	private static final double NORM_TOTAL_RESPONSE_TIME = 1.0;

	/**
	 * Processes a single quad making part of the dataset. Firstly, tries to figure out the URI of the dataset wherefrom the quads were obtained. 
	 * This is done by checking whether the current quads corresponds to the rdf:type property stating that the resource is a void:Dataset, if so, 
	 * the URI is extracted from the corresponding subject. Some HTTP requests are sent to the dataset's URI and the response times are averaged to 
	 * obtain a measure of the latency 
	 * @param quad Quad to be processed and examined to try to extract the dataset's URI
	 */
	public void compute(Quad quad) {
		
		// Check if the metric has already been computed
		if(this.hasBeenComputed) {
			return;
		}
		
		// Get all parts of the quad required for the computation of this metric
		String datasetURI = null; 
		
		try {
			datasetURI = EnvironmentProperties.getInstance().getDatasetURI();
		} catch(Exception ex) {
			logger.error("Error retrieven dataset URI, processor not initialised yet", ex);
			// Try to get the dataset URI from the VOID property, as last resource
			datasetURI = ResourceBaseURIOracle.extractDatasetURI(quad);
		}

		// The URI of the subject of such quad, should be the dataset's URL. 
		// Try to calculate the total delay associated to the current dataset
		if(datasetURI != null) {
			totalDelay = HTTPRetriever.measureReqsBurstDelay(datasetURI, NUM_HTTP_SAMPLES);
			logger.trace("Total delay for dataset {} was {}", datasetURI, totalDelay);
			
			// Metric has been computed, prevent it from being re-computed for every quad in the dataset
			this.hasBeenComputed = true;
		}
	}

	/**
	 * Returns the current value of the Low Latency Metric as a ranking in the range [0, 1], with 1.0 the top ranking. 
	 * It does so by computing the average of the time elapsed between the instant when a request is sent to the URI 
	 * of the dataset and the instant when any response is received. Then this average response time is normalized by dividing 
	 * NORM_TOTAL_RESPONSE_TIME, the ideal response time, by it
	 * @return Current value of the Low Latency metric, measured with respect to the dataset's URI
	 */
	public double metricValue() {

		double avgRespTime = ((double)totalDelay) / ((double)NUM_HTTP_SAMPLES);
		return Math.min(1.0, NORM_TOTAL_RESPONSE_TIME / avgRespTime);
	}

	public Resource getMetricURI() {
		return METRIC_URI;
	}

	public ProblemList<?> getQualityProblems() {
		// TODO Auto-generated method stub
		return null;
	}

}
