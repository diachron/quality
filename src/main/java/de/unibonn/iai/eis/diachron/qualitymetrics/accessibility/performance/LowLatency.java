package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.performance;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;
import de.unibonn.iai.eis.diachron.vocabularies.VOID;

/**
 * @author Santiago Londono
 * Estimates the efficiency with which a system can bind to the dataset, by measuring the delay between 
 * the submission of a request for that very dataset and reception of the respective response (or part of it)
 */
public class LowLatency extends AbstractQualityMetric {
	
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
	 * Processes a single quad making part of the dataset. Firstly, tries to figure out the URI of the dataset wherefrom the quads were obtained. 
	 * This is done by checking whether the current quads corresponds to the rdf:type property stating that the resource is a void:Dataset, if so, 
	 * the URI is extracted from the corresponding subject. Some HTTP requests are sent to the dataset's URI and the response times are averaged to 
	 * obtain a measure of the latency 
	 * @param quad Quad to be processed and examined to try to extract the dataset's URI
	 */
	@Override
	public void compute(Quad quad) {
		// Get all parts of the quad required for the computation of this metric
		String datasetURI = extractDatasetURI(quad);

		// The URI of the subject of such quad, should be the dataset's URL. 
		// Try to calculate the total delay associated to the current dataset
		if(datasetURI != null) {
			totalDelay = measureReqsBurstDelay(datasetURI, NUM_HTTP_SAMPLES);
		}
	}
	
	/**
	 * TODO: Move this method to a common's class, since it could be useful for several metrics
	 * Tries to figure out the URI of the dataset wherefrom the quads were obtained. This is done by checking whether the 
	 * current quads corresponds to the rdf:type property stating that the resource is a void:Dataset, if so, the URI is extracted 
	 * from the corresponding subject and returned 
	 * @param quad Quad to be processed and examined to try to extract the dataset's URI
	 * @return URI of the dataset wherefrom the quad originated, null if the quad does not contain such information
	 */
	protected static String extractDatasetURI(Quad quad) {
		// Get all parts of the quad required to analyze the quad
		Node subject = quad.getSubject();
		Node predicate = quad.getPredicate();
		Node object = quad.getObject();

		// First level validation: all parts of the triple will be required
		if(subject != null && predicate != null && object != null) {			
			// Second level validation: all parts of the triple must be URIs
			if(subject.isURI() && predicate.isURI() && object.isURI()) {				
				// Check that the current quad corresponds to the dataset declaration, from which the dataset URI will be extracted...
				if(predicate.getURI().equals(RDF.type.getURI()) && object.getURI().equals(VOID.Dataset.getURI())) {
					// The URI of the subject of such quad, should be the dataset's URL. 
					// Try to calculate the latency associated to the current dataset
					return subject.getURI();
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Calculates the time required to obtain the response resulting of a request to the specified dataset URL. 
	 * The calculation is performed by executing several requests to the dataSetUrl and counting the time elapsed until a response is received.  
	 * Note that the contents nor the code of the responses are taken into account
	 * @param dataSetUrl URL to which the requests will be sent
	 * @param numRequests total requests to be sent in the burst
	 * @return Total delay between the sending of the requests and the reception of the corresponding responses
	 */
	protected static long measureReqsBurstDelay(String dataSetUrl, int numRequests) {
		HttpURLConnection httpConn = null;
		InputStream responseStream = null;
		long accumDelay = 0;
		
		try {
			URL targetUrl = new URL(dataSetUrl);
			long startTimeStamp = 0;
			
			for(int i = 0; i < numRequests; i++) {
				// Create a new HttpURLConnection object for each request, since each instance is intended to perform a single request (view Javadoc)
				// note that this call does not establish the actual network connection to the target resource and thus the timer is not initiated here
				httpConn = (HttpURLConnection)targetUrl.openConnection();
				
				// Getting the input-stream of the response actually connects to the target and retrieves contents, which won't be consumed in this case
				try {
					// Initiate the timer, as the call to getInputStream connects to the target resource and sends GET and HEADers
					startTimeStamp = System.currentTimeMillis();
					responseStream = httpConn.getInputStream();
					// Response received, calculate delay
					accumDelay += (System.currentTimeMillis() - startTimeStamp);
				} finally {
					// Make sure the stream is closed, thereby freeing network resources associated to this particular trial
					if(responseStream != null) {
						responseStream.close();
					}
				}
			}
		} catch (IOException e) {
			// Something went wrong, numbers are not accurate anymore, return -1 indicating that total delay could not be determined
			logger.error("Error calculating requests burst delay: {}", e);
			return -1;
		} finally {
			// No need to reuse the connection anymore, disconnect
			if(httpConn != null) {
				httpConn.disconnect();
			}
		}
		
		// Return as result, the total time in seconds elapsed between requests and responses
		return accumDelay;
	}

	/**
	 * Returns the current value of the Low Latency Metric in milliseconds, computed as the average of the time elapsed between the 
	 * instant when a request is sent to the URI of the dataset and the instant when any response is received
	 * @return Current value of the Low Latency metric, measured with respect to the dataset's URI
	 */
	@Override
	public double metricValue() {
		// Average latency is in milliseconds
		return ((double)totalDelay)/((double)NUM_HTTP_SAMPLES);
	}

	@Override
	public Resource getMetricURI() {
		return METRIC_URI;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		// TODO Auto-generated method stub
		return null;
	}

}
