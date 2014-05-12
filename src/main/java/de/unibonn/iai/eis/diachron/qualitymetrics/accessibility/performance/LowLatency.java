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
	 * Holds the average latency as currently calculated by the compute method
	 */
	private long avgLatency = -1;

	@Override
	public void compute(Quad quad) {
		// Get all parts of the quad required for the computation of this metric
		Node subject = quad.getSubject();
		Node predicate = quad.getPredicate();
		Node object = quad.getObject();

		// First level validation: all parts of the triple will be required
		if(subject != null && predicate != null && object != null) {			
			// Second level validation: all parts of the triple must be URIs
			if(subject.isURI() && predicate.isURI() && object.isURI()) {				
				// Check that the current quad corresponds to the dataset declaration, from which the dataset URL will be extracted...
				if(predicate.getURI().equals(RDF.type.getURI()) && object.getURI().equals(VOID.Dataset.getURI())) {
					// The URI of the subject of such quad, should be the dataset's URL. 
					// Try to calculate the latency associated to the current dataset
					avgLatency = measureAvgLatency(subject.getURI());
				}
			}
		}
	}
	
	/**
	 * Calculates the time required to obtain the response resulting of a request to the specified dataset URL. 
	 * The calculation is performed by executing several requests to the dataSetUrl and counting the time elapsed until a response is received 
	 * and then, by taking the average of the results. Note that the contents nor the code of the responses are taken into account
	 * @param dataSetUrl URL to which the requests will be sent
	 * @return Average delay between the sending of the request and the reception of the corresponding response
	 */
	private long measureAvgLatency(String dataSetUrl) {
		HttpURLConnection httpConn = null;
		InputStream responseStream = null;
		long accumDelay = 0;
		
		try {
			URL targetUrl = new URL(dataSetUrl);
			long startTimeStamp = 0;
			
			for(int i = 0; i < NUM_HTTP_SAMPLES; i++) {
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
			// Something went wrong, numbers are not accurate anymore, return -1 indicating that latency could not be determined
			logger.error("Error calculating latency: {}", e);
			return -1;
		} finally {
			// No need to reuse the connection anymore, disconnect
			if(httpConn != null) {
				httpConn.disconnect();
			}
		}
		
		// Return as result, the average time in seconds elapsed between request and response
		return accumDelay/NUM_HTTP_SAMPLES;
	}

	/**
	 * Returns the current value of the Low Latency Metric in milliseconds, computed as the average of the time elapsed between the 
	 * instant when a request is sent to the URI of the dataset and the instant when any response is received
	 * @return Current value of the Low Latency metric, measured with respect to the dataset's URI
	 */
	@Override
	public double metricValue() {
		// Average latency is in milliseconds
		return avgLatency;
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
