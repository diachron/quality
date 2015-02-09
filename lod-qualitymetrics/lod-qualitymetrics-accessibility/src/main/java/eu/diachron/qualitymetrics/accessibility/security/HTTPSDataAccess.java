package eu.diachron.qualitymetrics.accessibility.security;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.technques.probabilistic.ResourceBaseURIOracle;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import de.unibonn.iai.eis.diachron.semantics.DQM;

/**
 * @author Santiago Londono
 * Determines whether access to data is protected against its illegal alteration and it's authenticity can be guaranteed, 
 * by verifying that access to the dataset is performed through a sound, HTTPS/SSL connection.
 * 
 * This is only required for the EBI use case
 */
public class HTTPSDataAccess implements QualityMetric {
	
	private final Resource METRIC_URI = DQM.HTTPSDataAccessMetric;
	
	private static Logger logger = LoggerFactory.getLogger(HTTPSDataAccess.class);
	
	/**
	 * TODO: Move to a better, more accessible location
	 * Name of the HTTPS protocol, as identified by the Java class URL
	 */
	private static final String HTTPS_PROTOCOL_NAME = "https";
	
	/**
	 * Indicates whether an HTTPS connection could be successfully established with the endpoint hosting the dataset
	 */
	private boolean isHttpsConnection = false;
	
	/**
	 * Flag stating whether the metric has been computed. This metric should be computed once, for the dataset's URI,
	 * but the compute method is run for every quad in the dataset. This flag prevents the metric from being computed per quad
	 */
	private boolean hasBeenComputed = false;
	
	/**
	 * Processes a single quad making part of the dataset. Firstly, tries to figure out the URI of the dataset wherefrom the quads were obtained. 
	 * If so, the URI is extracted from the corresponding subject. Then, attempts to establish an HTTPS connection with the aforesaid URI, if 
	 * successful, the isHttpsConnection is set to true, indicating that the analyzed dataset is secure 
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
		if(datasetURI != null) {
			try {
				// Given the dataset's URI, try to establish a secure connection with its source endpoint
				logger.trace("Testing HTTPS/SSL connection against dataset: {}...", datasetURI);
				
				// Try to connect to URI, verify that the secure connection via HTTPS can be established
				isHttpsConnection = establishHttpsConnection(datasetURI);
			} catch (IOException e) {
				// An IO error occurred while trying to establish secure connection, failed
				logger.debug("HTTPS/SSL connection failed due to IO error: {}...", e);
				isHttpsConnection = false;
			} finally {
				// Metric has been computed, prevent it from being re-computed for every quad in the dataset
				this.hasBeenComputed = true;
			}
		}		
	}

	/**
	 * Tries to establish a fully-functional, sound HTTPS connection with the specified URI, and indicates if 
	 * no errors were detected in the process and the connection was properly established
	 * @param dataSetUri URI to establish an HTTPS connection with
	 * @return true if the HTTPS/SSL connection was successfully established, false otherwise
	 * @throws IOException 
	 */
	private boolean establishHttpsConnection(String dataSetUri) throws IOException {
		// Initialize variables make sure the connection is closed at the end
		HttpsURLConnection httpConn = null;
		InputStream responseStream = null;
		URL targetUrl = null;
		
		try {
			// Build the target URL and abort if recognized as invalid
			targetUrl = new URL(dataSetUri);
		} catch (MalformedURLException e) {
			// Incorrect URI, impossible to even establish a connection
			logger.error("Error checking HTTPS connection, invalid URL: {}. Details: {}", dataSetUri, e);
			return false;
		}
		
		// First validation: URL protocol is HTTPS
		if(targetUrl.getProtocol() == null || !targetUrl.getProtocol().equals(HTTPS_PROTOCOL_NAME)) {
			return false;
		}
						
		try {
			// Create a new HttpURLConnection object for each request, since each instance is intended to perform a single request (view Javadoc)
			// note that this call does not establish the actual network connection to the target resource and thus the timer is not initiated here
			httpConn = (HttpsURLConnection)targetUrl.openConnection();
			httpConn.setRequestProperty("Content-Type", "application/rdf+xml");
			
			// The call to getInputStream connects to the target resource and sends GET and HEADers...
			responseStream = httpConn.getInputStream();
			// If no exceptions are thrown, connection was successful and contents can be retrieved securely
			return true;
		} finally {
			// Make sure the stream is closed, thereby freeing network resources associated to this particular trial
			if(responseStream != null) {
				responseStream.close();
			}
			// No need to reuse the connection anymore, disconnect
			if(httpConn != null) {
				httpConn.disconnect();
			}
		}
	}

	/**
	 * Returns the current value of the HTTPS access to data source metric, which is positive (1.0) if a secure, HTTPS connection could 
	 * be established with the URI whereby the dataset is accessed, otherwise, the metric rates the dataset with a value of 0.0
	 * @return 1.0 if access to dataset is confidential, through an HTTPS connection. 0.0 otherwise
	 */
	public double metricValue() {
		// If HTTPS/SSL connection could be established with the dataset's source, it is a positive result (valued as +1.0)
		if(isHttpsConnection) {
			return 1.0;
		} else {
			return 0.0;
		}
	}

	public Resource getMetricURI() {
		return METRIC_URI;
	}

	public ProblemList<?> getQualityProblems() {
		return null;
	}

}
