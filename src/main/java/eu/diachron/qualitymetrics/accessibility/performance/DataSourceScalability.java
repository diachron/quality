package eu.diachron.qualitymetrics.accessibility.performance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.semantics.vocabulary.DQM;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;

/**
 * @author Santiago Londono
 * Estimates the efficiency with which a system can bind to the dataset, by determining 
 * whether the time to answer a set of N requests (sent in parallel) divided by N, is not 
 * longer than the time it takes to answer one request
 */
public class DataSourceScalability implements QualityMetric {
	
	private final Resource METRIC_URI = DQM.ScalabilityOfDataSourceMetric;
	
	private static Logger logger = LoggerFactory.getLogger(DataSourceScalability.class);
	
	/**
	 * Amount of HTTP requests that will be sent to the data source in order to prove its scalability
	 */
	private static final int NUM_HTTP_REQUESTS = 10;
	
	/**
	 * Maximum time in milliseconds to wait for the responses to all requests sent
	 */
	private static final int REQUEST_SET_IMEOUT = 30000;
	
	/**
	 * Holds the difference between the averaged response time calculated for N requests and the 
	 * response time calculated for a single request, as currently calculated by the compute method
	 */
	private long scalabilityDiff = -1;
	
	/**
	 * Flag stating whether the metric has been computed. This metric should be computed once, for the dataset's URI,
	 * but the compute method is run for every quad in the dataset. This flag prevents the metric from being computed per quad
	 */
	private boolean hasBeenComputed = false;

	/**
	 * Processes a single quad making part of the dataset. Firstly, tries to figure out the URI of the dataset wherefrom the quads were obtained. 
	 * If so, the URI is extracted from the corresponding subject. A number NUM_HTTP_REQUESTS of HTTP GET requests are sent simultaneously to 
	 * the dataset's URI and the response times are averaged, then the response time resulting of a single request is extracted from this value and 
	 * the result is regarded as the "scalability differential factor" 
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
			datasetURI = LowLatency.extractDatasetURI(quad);
		}

		// The URI of the subject of such quad, should be the dataset's URL. 
		// Try to calculate the scalability differential associated to the data source
		if(datasetURI != null) {
			// Send parallel requests and average the total delay to estimate the time required to attend a single request
			logger.trace("Sending {} HTTP GET requests in parallel to {}...", NUM_HTTP_REQUESTS, datasetURI);
			long requestsSwarmDelay = HttpPerformanceUtil.measureParallelReqsDelay(datasetURI, NUM_HTTP_REQUESTS, REQUEST_SET_IMEOUT);
			
			// Verify if delay estimate was properly calculated (a delay of -1 indicates that one or more requests failed)
			if(requestsSwarmDelay >= 0) {
				long avgRequestsSwarmDelay = requestsSwarmDelay / NUM_HTTP_REQUESTS;
	
				// Send a single request, directly obtain the time required to attend that very request
				long singleRequestDelay = HttpPerformanceUtil.measureReqsBurstDelay(datasetURI, 1);
				
				// Calculate the scalability differential factor
				scalabilityDiff = avgRequestsSwarmDelay - singleRequestDelay;
				logger.trace("Total scalability differential factor for dataset {} was {}", datasetURI, scalabilityDiff);
			} else {
				logger.trace("Calculation of scalability differential factor failed for dataset {}", datasetURI);
				scalabilityDiff = 0; //return 0 when test fails
			}
			
			// Metric has been computed, prevent it from being re-computed for every quad in the dataset
			this.hasBeenComputed = true;
		}
	}

	/**
	 * Returns the current value of the Scalability of a Data Source metric in milliseconds, computed as the difference 
	 * between the total time to serve N requests, divided N (average) and the time to serve a single request
	 * @return Current value of the Scalability of a Data Source metric, measured with respect to the dataset's URI
	 */
	public double metricValue() {
		return (double)scalabilityDiff;
	}

	public Resource getMetricURI() {
		return METRIC_URI;
	}

	public ProblemList<?> getQualityProblems() {
		return null;
	}

}
