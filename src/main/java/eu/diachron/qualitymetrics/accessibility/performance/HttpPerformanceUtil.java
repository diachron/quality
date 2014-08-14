package eu.diachron.qualitymetrics.accessibility.performance;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Santiago Londono
 * Utilitarian class containing methods usefult to measure the performance of an HTTP endpoint
 * TODO: Move this class out of the performance package, as it might be useful for metrics in other categories/dimensions
 */
public class HttpPerformanceUtil {
	
	private static Logger logger = LoggerFactory.getLogger(HttpPerformanceUtil.class);
	
	/**
	 * Maximum number of simultaneous HTTP request that can be sent in separate threads
	 */
	private static final int MAX_PARALLEL_REQS = 20;
	
	/**
	 * Calculates the time required to obtain the response resulting of a request to the specified dataset URL. 
	 * The calculation is performed by executing several requests to the dataSetUrl and counting the time elapsed until a response is received.  
	 * Note that the contents nor the code of the responses are taken into account
	 * @param dataSetUrl URL to which the requests will be sent
	 * @param numRequests total requests to be sent in the burst
	 * @return Total delay between the sending of the requests and the reception of the corresponding responses
	 * 			-1 if any of the requests failed and thus total delay could not be calculated accurately
	 */
	public static long measureReqsBurstDelay(String dataSetUrl, int numRequests) {
		// Accumulate the delay of serving all the HTTP requests sent
		long accumDelay = 0;
		URL targetUrl = null;
		
		try {
			// Build the target URL and abort if recognized as invalid
			targetUrl = new URL(dataSetUrl);
		} catch (MalformedURLException e) {
			// Something went wrong, numbers are not accurate anymore, return -1 indicating that total delay could not be determined
			logger.error("Error calculating parallel requests delay, invalid URL: {}. Details: {}", dataSetUrl, e);
			return -1;
		}

		// Send burst of numRequests, sequential HTTP GET requests
		for(int i = 0; i < numRequests; i++) {
			// Response received, calculate delay
			try {
				accumDelay += sendProbeHttpGetRequest(targetUrl);
			} catch (IOException e) {
				// An error occurred sending requests, return -1 as indication
				logger.error("Error calculating parallel requests delay, I/O error sending HTTP request URL: {}. Details: {}", dataSetUrl, e);
				return -1;
			}
		}
		
		// Return as result, the total time in seconds elapsed between requests and responses
		return accumDelay;
	}
	
	/**
	 * Calculates the time required to obtain the responses of a set of HTTP GET requests sent in parallel. 
	 * The calculation is performed by simultaneously sending several requests to the dataSetUrl, each on 
	 * a separate thread and counting the time elapsed until a response is received. 
	 * Warning: The whole set of requests has a timeout of - seconds. 
	 * Note that the contents nor the code of the responses are taken into account
	 * @param dataSetUrl URL to which the requests will be sent
	 * @param numRequests total requests to be sent in parallel
	 * @param timeoutMillisecs maximum time to wait for all the requests to be completed, if exceeded, operation will be aborted and threads properly disposed of
	 * @return Total delay between the sending of the requests and the reception of all the corresponding responses, 
	 * 			-1 if any of the requests failed and thus total delay could not be calculated accurately 
	 */
	public static long measureParallelReqsDelay(String dataSetUrl, int numRequests, long timeOutMillisecs) {
		long accumDelay = 0;
		URL targetUrl = null;
		ExecutorService poolRequest = null;
		
		try {
			// Build the target URL and abort if recognized as invalid
			targetUrl = new URL(dataSetUrl);
		} catch (MalformedURLException e) {
			// Something went wrong, numbers are not accurate anymore, return -1 indicating that total delay could not be determined
			logger.error("Error calculating parallel requests delay, invalid URL: {}. Details: {}", dataSetUrl, e);
			return -1;
		}
		
		try {
			// Hold the set of HTTP request tasks in an array, so that their resulting delays can be collected after completion
			HttpGetRequestTask[] arrHttpRequestTasks = new HttpGetRequestTask[numRequests];
			poolRequest = Executors.newFixedThreadPool(MAX_PARALLEL_REQS);
			
			for(int i = 0; i < numRequests; i++) {
				// Create a task to be scheduled, which will send an HTTP GET request
				arrHttpRequestTasks[i] = new HttpGetRequestTask(targetUrl);
				
				// Schedule the task to be executed asynchronously
				poolRequest.execute(arrHttpRequestTasks[i]);
			}
			
			// Block and wait until all requests are finish or timeout has been reached
			poolRequest.shutdown();
			poolRequest.awaitTermination(timeOutMillisecs, TimeUnit.MILLISECONDS);
			
			// Collect and accumulate delays resulting of all submitted requests
			for(int i = 0; i < numRequests; i++) {
				accumDelay += (arrHttpRequestTasks[i].getDelay());
				// If any of the requests failed, the calculation would not be accurate, return -1 as indication
				if(accumDelay < 0) {
					return -1;
				}
			}
		} catch(InterruptedException iex) {
			// The threadpool was interrupted for some reason, which is lethal enough to prevent the delay from being determined
			logger.error("Error calculating requests burst delay, thread-pool interrupted: {}", iex);
			return -1;
		} finally {
			// Make absolutely sure that Thread pool gets properly shutdown
			if(poolRequest != null) {
				poolRequest.shutdown();
			}
		}
		
		// Return as result, the total time in seconds elapsed between requests and responses
		return accumDelay;
	}
	
	/**
	 * Sends an HTTP Get request to the target URL and measures the amount of time elapsed between the instant when the request was sent
	 * and the instant when the respective response was received, which is returned as result
	 * @param targetUrl URL to sent the HTTP GET request to
	 * @return Amount of time required to serve the request
	 * @throws IOException 
	 */
	private static long sendProbeHttpGetRequest(URL targetUrl) throws IOException {
		// Initialize variables make sure the connection is closed at the end
		HttpURLConnection httpConn = null;
		InputStream responseStream = null;
		long startTimeStamp = 0;
		long delay = 0;
		
		try {
			// Create a new HttpURLConnection object for each request, since each instance is intended to perform a single request (view Javadoc)
			// note that this call does not establish the actual network connection to the target resource and thus the timer is not initiated here
			httpConn = (HttpURLConnection)targetUrl.openConnection();
			httpConn.setRequestProperty("Content-Type", "application/rdf+xml");
			
			// Initiate the timer, as the call to getInputStream connects to the target resource and sends GET and HEADers
			startTimeStamp = System.currentTimeMillis();
			// Getting the input-stream of the response actually connects to the target and retrieves contents, which won't be consumed in this case
			responseStream = httpConn.getInputStream();
			// Response received, calculate delay
			delay = (System.currentTimeMillis() - startTimeStamp);
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
		
		// Return as result, the total time in seconds elapsed between requests and responses
		return delay;
	}
	
	/**
	 * @author slondono
	 * A simple, runnable task (intended to be run as a separate thread) consisting of sending an HTTP GET request to 
	 * a specific URL, and holding as result, the amount of time in milliseconds, required to serve the request
	 */
	private static class HttpGetRequestTask implements Runnable {
		
		private URL targetUrl;
		private long delay = 0;
		
		/**
		 * Default constructor
		 * @param targetUrl URL to sent the HTTP GET request to
		 */
		public HttpGetRequestTask(URL targetUrl) {
			this.targetUrl = targetUrl;
		}

		/**
		 * Run the task, which executes an HTTP GET request on the URL provided upon instantiation
		 */
		public void run() {
			try {
				delay = sendProbeHttpGetRequest(targetUrl);
			} catch (IOException e) {
				// An error occurred sending requests, set result to -1 as indication
				logger.error("Error calculating parallel requests delay, I/O error sending HTTP request URL: {}. Details: {}", targetUrl, e);
				delay = -1;
			}
		}
		
		/**
		 * After the task has been completed, returns the amount of time elapsed between the instant when the request was sent
		 * and the instant when the respective response was received
		 * @return Amount of time required to serve the request
		 */
		public long getDelay() {
			return delay;
		}
		
	}

}
