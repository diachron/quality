/**
 * 
 */
package eu.diachron.qualitymetrics.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicStatusLine;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unibonn.iai.eis.diachron.datatypes.StatusCode;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.DiachronCacheManager;

/**
 * @author Jeremy Debattista
 * 
 * Retreives HTTP resources.
 * Furthermore, provides utilitarian methods to measure the performance of an HTTP endpoint, based on  
 * sending a set of requests either sequentially or in parallel 
 */
public class HTTPRetriever {

	final static Logger logger = LoggerFactory.getLogger(HTTPRetriever.class);
	
	/**
	 * Maximum number of simultaneous HTTP request that can be sent in separate threads, configuration parameter
	 * of the performance utilitarian methods for measurement of performance (namely measurement of parallel reqs.)
	 */
	private static final int MAX_PARALLEL_REQS = 20;

	//private ConcurrentLinkedQueue<String> httpQueue = new ConcurrentLinkedQueue<String>();
	private Set<String> httpQueue = Collections.synchronizedSet(new HashSet<String>());
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	private CountDownLatch mainHTTPRetreiverLatch;
				
	public void addResourceToQueue(String resourceURI) {
		this.httpQueue.add(resourceURI);
	}

	public void addListOfResourceToQueue(List<String> resourceURIs) {
		this.httpQueue.addAll(resourceURIs);
	}
	

	public void start() throws InterruptedException{
		//TODO: check if httpQUEUE is not empty yet 
		mainHTTPRetreiverLatch = new CountDownLatch(httpQueue.size());
		
		Runnable retreiver = new Runnable() {
			public void run() {
				try {
					runHTTPAsyncRetreiver();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		executor.submit(retreiver);
		mainHTTPRetreiverLatch.await();
	}
	
	/**
	 * Stops the HTTPRetreiver Process
	 */
	public void stop() {
		executor.shutdown();
	}

	private void runHTTPAsyncRetreiver() throws InterruptedException {
		RequestConfig requestConfig = this.getRequestConfig(true);
		CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
				.setDefaultRequestConfig(requestConfig).build();
		final HttpClientContext localContext = HttpClientContext.create();
		
		httpclient.start();
		for(final String queuePeek : this.httpQueue){
			Thread.sleep(3000);
			if (DiachronCacheManager.getInstance().existsInCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, queuePeek)) {
				continue;
			}

			final CachedHTTPResource newResource = new CachedHTTPResource();
			newResource.setUri(queuePeek);
			DiachronCacheManager.getInstance().addToCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, queuePeek, newResource);
																										  
			final HttpGet request = new HttpGet(queuePeek);
			Header accept = new BasicHeader("Accept", "application/rdf+xml; text/html");
			request.addHeader(accept);
			
			httpclient.execute(request, localContext,
					new FutureCallback<HttpResponse>() {
						public void completed(final HttpResponse response) {
							newResource.addResponse(response);
							try {
								if (localContext.getRedirectLocations().size() >= 1) {
									List<URI> uriRoute = new ArrayList<URI>();
									uriRoute.add(request.getURI());
									uriRoute.addAll(localContext.getRedirectLocations());
									try {
										newResource.addAllResponses(followAsyncRedirection(uriRoute));
									} catch (IOException e) {
										e.printStackTrace();
									}
								} else {
									newResource.addStatusLines(response.getStatusLine());
								}
							} catch (Exception e) {
								logger.debug("Exception during the request for redirect locations whith the following exception : {}",e.getLocalizedMessage());
								newResource.addStatusLines(response.getStatusLine());
							}
							DiachronCacheManager.getInstance().addToCache(DiachronCacheManager.HTTP_RESOURCE_CACHE,queuePeek, newResource);
							mainHTTPRetreiverLatch.countDown();
						}

						public void failed(final Exception ex) {
							newResource.setDereferencabilityStatusCode(StatusCode.BAD);
							newResource.addStatusLines(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 0,"Request could not be processed"));
							DiachronCacheManager.getInstance().addToCache(DiachronCacheManager.HTTP_RESOURCE_CACHE,queuePeek, newResource);

							logger.debug("Failed in retreiving request : {}, with the following exception : {}",request.getURI().toString(),ex.getLocalizedMessage());
							mainHTTPRetreiverLatch.countDown();
						}

						public void cancelled() {
							logger.debug("The retreival for {} was cancelled.",request.getURI().toString());
							mainHTTPRetreiverLatch.countDown();
						}
					});
		}
	}
	
	protected List<HttpResponse> followAsyncRedirection(List<URI> uriRoute) throws IOException, InterruptedException {
		final List<HttpResponse> httpResponses = Collections.synchronizedList(new ArrayList<HttpResponse>());
		RequestConfig requestConfig = this.getRequestConfig(false);
		CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom().setDefaultRequestConfig(requestConfig).build();
		try {
			final HttpClientContext localContext = HttpClientContext.create();
			httpclient.start();
			final List<HttpGet> requests = this.toHttpGetList(uriRoute);

			final CountDownLatch redirectionLatch = new CountDownLatch(requests.size());
			for (final HttpGet request : requests) {
				httpclient.execute(request, localContext, new FutureCallback<HttpResponse>() {

							public void completed(final HttpResponse response) {
								redirectionLatch.countDown();
								httpResponses.add(response);
							}

							public void failed(final Exception ex) {
								logger.debug("Failed in retreiving follow redirection request : {}, with the following exception : {}",request.getURI().toString(),ex.getLocalizedMessage());
								redirectionLatch.countDown();
							}

							public void cancelled() {
								logger.debug("The retreival for {} was cancelled.",request.getURI().toString());
								redirectionLatch.countDown();
							}
						});
			}
			redirectionLatch.await();
		} finally {
			httpclient.close();
		}
		return httpResponses;
	}

	private List<HttpGet> toHttpGetList(List<URI> uriRoute) {
		List<HttpGet> requests = new ArrayList<HttpGet>();
		for (URI uri : uriRoute) {
			requests.add(new HttpGet(uri.toString()));
		}

		return requests;
	}

	private RequestConfig getRequestConfig(boolean followRedirects) {
		return RequestConfig.custom().
				setSocketTimeout(3000).
				setConnectTimeout(3000).
				setRedirectsEnabled(followRedirects).
				build();
	}

	public boolean isPossibleURL(String url) {
		// TODO: add more protocols
		return ((url.startsWith("http")) || (url.startsWith("https")));
	}
	
	/**
	 * TODO: Remove main method, which was added solely for testing purposes
	 */
	public static void main(String [] args) throws InterruptedException{
		HTTPRetriever httpRetreiver = new HTTPRetriever();
	
		//String uri = "http://aksw.org/model/export/?m=http%3A%2F%2Faksw.org%2F&f=rdfxml";
		String uri = "http://aksw.org/MichaelMartin";
		httpRetreiver.addResourceToQueue(uri);
		httpRetreiver.start();
		Thread.sleep(5000);
	
		CachedHTTPResource httpResource = (CachedHTTPResource) DiachronCacheManager.getInstance().getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE,uri);
		//System.out.println(httpResource.getResponses().get(0).getEntity().getContentType().getValue());
		//httpResource.getResponses().get(0).getHeaders("Content-Disposition");
		String filename = httpResource.getResponses().get(0).getHeaders("Content-Disposition")[0].getValue().replace("filename=\"", "").replace("\"", "");
		Lang language = RDFLanguages.filenameToLang(filename);
		System.out.println(WebContent.mapLangToContentType(language));
	}
	
	/*******************************************************************************************
	 ** Performance measurement methods
	 *******************************************************************************************/
	
	/**
	 * Calculates the time required to obtain the response resulting of a request to the specified dataset URL. 
	 * The calculation is performed by sending several requests to the dataSetUrl and adding up the times elapsed until each response is received.  
	 * Note that the contents nor the code of the responses are taken into account.
	 * @param dataSetUrl URL to which the requests will be sent
	 * @param numRequests total requests to be sent in the burst
	 * @return Total delay (milliseconds) between the sending of the requests and the reception of the corresponding responses
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
	 * Warning: The whole set of requests has a timeout of timeOutMillisecs seconds. 
	 * Note that the contents nor the code of the responses are taken into account
	 * @param dataSetUrl URL to which the requests will be sent
	 * @param numRequests total requests to be sent in parallel
	 * @param timeoutMillisecs maximum time to wait for all the requests to be completed, if exceeded, operation will be aborted and threads properly disposed of
	 * @return Total delay (milliseconds) between the sending of the requests and the reception of all the corresponding responses, 
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
	 * @return Amount of time required to serve the request, in milliseconds
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
