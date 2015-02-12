package eu.diachron.qualitymetrics.accessibility.performance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ResourceBaseURIOracle;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import eu.diachron.qualitymetrics.utilities.HTTPRetriever;

/**
 * @author Santiago Londono
 * Estimates the efficiency with which a system can bind to the dataset, by measuring the number of 
 * answered HTTP requests responsed by the source of the dataset, per second.
 */
public class HighThroughput implements QualityMetric {
	
	private final Resource METRIC_URI = DQM.HighThroughputMetric;
	
	private static Logger logger = LoggerFactory.getLogger(HighThroughput.class);
	
	/**
	 * Amount of HTTP requests that will be sent to the data source in order to estimate how many requests are served per second. 
	 */
	private static final int NUM_HTTP_REQUESTS = 3;
	
	/**
	 * Number of requests per second that ideally, should be served by a data source. In other words, its the amount of served requests 
	 * per second above of which a resource will get a perfect score of 1.0. 
	 */
	private static final double NORM_SERVED_REQS_PER_MILLISEC = 1.0;
	
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
	 * Processes a single quad making part of the dataset. Firstly, tries to figure out the URI of the dataset wherefrom the quads were obtained. 
	 * A burst HTTP requests is sent to the dataset's URI and the number of requests sent is divided by the total time required to serve them,  
	 * thus obtaining the estimated number of requests server per second
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
			totalDelay = HTTPRetriever.measureReqsBurstDelay(datasetURI, NUM_HTTP_REQUESTS);
			logger.trace("Total delay for dataset {} was {}", datasetURI, totalDelay);
			
			// Metric has been computed, prevent it from being re-computed for every quad in the dataset
			this.hasBeenComputed = true;
		}
	}

	/**
	 * Returns the current value of the High Throughput Metric as a ranking in the range [0, 1], with 1.0 the top ranking. 
	 * First estimates the number of served requests per second, computed as the ration between the total number of requests 
	 * sent to the dataset's endpoint and the sum of their response times. Then this estimate is normalized by dividing it 
	 * by NORM_SERVED_REQS_PER_SEC, the ideal amount of requests a resource is expected to serve per second, to get a raking of 1.0
	 * @return Current value of the High Throughput metric, measured with respect to the dataset's URI
	 */
	public double metricValue() {

		double servedReqsPerMilliSec = ((double)NUM_HTTP_REQUESTS)/((double)totalDelay);
		return Math.min(1.0, servedReqsPerMilliSec / NORM_SERVED_REQS_PER_MILLISEC);
	}

	public Resource getMetricURI() {
		return METRIC_URI;
	}

	public ProblemList<?> getQualityProblems() {
		return null;
	}

}
