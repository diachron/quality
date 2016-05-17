package eu.diachron.qualitymetrics.accessibility.performance;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import eu.diachron.qualitymetrics.utilities.AbstractQualityMetric;
import eu.diachron.qualitymetrics.utilities.HTTPRetriever;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;

/**
 * @author Santiago Londono
 * Estimates the efficiency with which a system can bind to the dataset, by determining 
 * whether the time to answer a set of N requests (sent in parallel) divided by N, is not 
 * longer than the time it takes to answer one request
 */
public class DataSourceScalability extends AbstractQualityMetric {
	
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
	 * The computation of this metric is based on the difference between the response time of a single request and 
	 * the average response time of a set of NUM_HTTP_REQUESTS, issued in parallel. The DIFFERENCE_THRESHOLD is the 
	 * number of milliseconds above which the value of the metric will start getting a value of 0 (worst ranking). 
	 * For differences below this threshold, the value of the metric increases inverse linearly (view metricValue())
	 */
	private static final double DIFFERENCE_THRESHOLD = 10000.0;
		
	/**
	 * Holds the difference between the averaged response time calculated for N requests and the 
	 * response time calculated for a single request, as currently calculated by the compute method
	 */
	private long scalabilityDiff = -1;
	
	
	/**
	 * Dataset PLD
	 */
	private String datasetURI = null;

	/**
	 * Holds the metric value
	 */
	private Double metricValue = null;
	
	private List<Quad> _problemList = new ArrayList<Quad>();
	
	/**
	 * Processes a single quad making part of the dataset. Firstly, tries to figure out the URI of the dataset wherefrom the quads were obtained. 
	 * If so, the URI is extracted from the corresponding subject. A number NUM_HTTP_REQUESTS of HTTP GET requests are sent simultaneously to 
	 * the dataset's URI and the response times are averaged, in order to estimate the response time of a single request. Then an additional, 
	 * single request is sent and its response time measured, the result of substracting it from the average is set as "scalability differential factor" 
	 * @param quad Quad to be processed and examined to try to extract the dataset's URI
	 */
	public void compute(Quad quad) {
		logger.debug("Computing : {} ", quad.asTriple().toString());
	}

	/**
	 * The “scalability differential factor” or Sdf, is calculated as the difference between the average response time of a request, 
	 * estimated by sending N simultaneous requests and the response time of an independent, single request. Thus, its range is [0, +inf).
	 * This method computes the normalized value of the metric, which is in the range [0, 1], with 1 the top ranking. when the Sdf gets 
	 * a value bigger than or equals to Thres, the value of the metric will be 0. Therefore, for Sdf values above Thres, 
	 * the metric losses its comparative power.
	 * @return Current value of the Scalability of a Data Source metric, measured with respect to the dataset's URI
	 */
	public double metricValue() {
		this.datasetURI = EnvironmentProperties.getInstance().getBaseURI();
		
		if (this.metricValue == null){
			
			// Send parallel requests and accumulate their response times as the total delay
			logger.trace("Sending {} HTTP GET requests in parallel to {}...", NUM_HTTP_REQUESTS, datasetURI);
			long requestsSwarmDelay = HTTPRetriever.measureParallelReqsDelay(this.datasetURI, NUM_HTTP_REQUESTS, REQUEST_SET_IMEOUT);
			
			// Verify if the total delay was properly calculated (a delay of -1 indicates that one or more requests failed, which would spoil the avg. op.)
			if(requestsSwarmDelay >= 0) {
				// Estimate the response time of a single request as the average response time among the swarm of requests
				long avgRequestsSwarmDelay = requestsSwarmDelay / NUM_HTTP_REQUESTS;
	
				// Send a single request, directly obtain the time required to attend that very request
				long singleRequestDelay = HTTPRetriever.measureReqsBurstDelay(datasetURI, 1);
				
				// Calculate the scalability differential factor
				scalabilityDiff = avgRequestsSwarmDelay - singleRequestDelay;
				logger.trace("Total scalability differential factor for dataset {} was {}", datasetURI, scalabilityDiff);
			} else {
				logger.trace("Calculation of scalability differential factor failed for dataset {}", datasetURI);
				scalabilityDiff = ((long)DIFFERENCE_THRESHOLD); //return 0 when test fails
			}
			
			statsLogger.info("DataSourceScalability. Dataset: {}; Base URI: {} - Scalability Differential : {}; " +
					"Difference Threshold : {};", EnvironmentProperties.getInstance().getDatasetURI(), 
					EnvironmentProperties.getInstance().getBaseURI(), scalabilityDiff, DIFFERENCE_THRESHOLD);
			
			this.metricValue = Math.max(0.0, 1.0 - (1.0/DIFFERENCE_THRESHOLD) * Math.max(0.0, (double)scalabilityDiff));
		}
		return this.metricValue;
	}

	public Resource getMetricURI() {
		return METRIC_URI;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		ProblemList<Quad> pl = null;
		try {
			if(this._problemList != null && this._problemList.size() > 0) {
				pl = new ProblemList<Quad>(this._problemList);
			} else {
				pl = new ProblemList<Quad>();
			}
		} catch (ProblemListInitialisationException e) {
			logger.error("Error building problems list for metric Data Source Scalability", e);
		}
		return pl;
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
