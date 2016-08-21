package eu.diachron.qualitymetrics.utilities;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.methods.AsyncCharConsumer;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.protocol.HttpContext;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;

import de.unibonn.iai.eis.diachron.datatypes.StatusCode;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource.SerialisableHttpResponse;
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
	private static final int MAX_PARALLEL_REQS = 15;
	
	private static final int TIMEOUT = 10000;

	private static final String ACCEPT_TYPE = "application/rdf+xml, text/n3, text/turtle, application/rdf+json, application/n-triples, application/ld+json, application/n-quads, application/trig, text/rdf+n3, application/n3, text/trig, text/xml" ;
	
	/**
	 * Web proxy to perform the HTTP requests, if set to null, no proxy will be used
	 */
	private static String webProxy = null;
	private static Integer webProxyPort = null;
	
	/**
	 * Indicates whether redirections obtained after successfully completing an HTTP request shold be followed
	 * Note that Apache HTTP Client automatically follows most redirect responses (3xx), hence following redirections
	 * is not necessary and redundant in most cases
	 */
	private static boolean followRedirections = true;


	/**
	 * A fail-safe mechanism in order to ensure that web URLs are not visited again if they are not responding after a number of maximum retries
	 */
	private ConcurrentMap<String, Boolean> failSafeMap = new ConcurrentLinkedHashMap.Builder<String, Boolean>().maximumWeightedCapacity(10000).build(); // A small fail-safe map that checks whether a domain retrieves vocabs.
    private ConcurrentMap<String, Integer> failSafeCounter = new ConcurrentLinkedHashMap.Builder<String, Integer>().maximumWeightedCapacity(10000).build(); // keeps a counter of the number of times an NS was accessed before putting the NS to the fail-safe map
    private final Integer NS_MAX_RETRIES = 3;

	
	private Queue<String> httpQueue = new ConcurrentLinkedQueue<String>();
	private Queue<String> httpGetQueue = new ConcurrentLinkedQueue<String>();
	
	private ExecutorService executor = null;
				
	public void addResourceToQueue(String resourceURI) {
		this.httpQueue.add(resourceURI);
	}

	public void addListOfResourceToQueue(List<String> resourceURIs) {
		this.httpQueue.addAll(resourceURIs);
	}
	

	public void start(){
		start(false);
	}
	
	private boolean useContentType = false;
	
	public void setUseContentType(boolean requiresContentType){
		this.useContentType = true;
	}
	
	public void start(final boolean requiresContentType) {
		// Dereference all the URIs stored in the queue, asynchronously. Wait until all have been resolved
		if(!httpQueue.isEmpty()) {
			executor = Executors.newSingleThreadExecutor();

			Runnable retreiver = new Runnable() {
				public void run() {
					try {
						runHTTPAsyncRetreiver(requiresContentType, true);
					} catch (InterruptedException e) {
						// The thread being interrupted for whatever reason, is severe enough to report a runtime exception
						logger.error("HTTP async request thread interrupted", e);
						throw new RuntimeException(e);
					}
				}
			};
			executor.submit(retreiver);
			executor.shutdown();
		}
		
//		while(!httpQueue.isEmpty()){};		
//		httpQueue.addAll(httpGetQueue);
//		httpGetQueue.clear();
//
//		if(!httpQueue.isEmpty()) {
//			executor = Executors.newSingleThreadExecutor();
//
//			Runnable retreiver = new Runnable() {
//				public void run() {
//					try {
//						//try again with the get now
//						runHTTPAsyncRetreiver(requiresContentType, true);
//					} catch (InterruptedException e) {
//						// The thread being interrupted for whatever reason, is severe enough to report a runtime exception
//						logger.error("HTTP async request thread interrupted", e);
//						throw new RuntimeException(e);
//					}
//				}
//			};
//			executor.submit(retreiver);
//			executor.shutdown();
//		}
	}
	
	/**
	 * Stops the HTTPRetreiver Process
	 */
	public void stop() {
		executor.shutdown();
	}
	
	
	private synchronized void addToFailSafeDecision(String domainTLD){
		if (this.failSafeCounter.containsKey(domainTLD)){
			Integer current = this.failSafeCounter.get(domainTLD) + 1;
			if (current == NS_MAX_RETRIES){
				this.failSafeMap.put(domainTLD, true);
				this.failSafeCounter.put(domainTLD, 0);
			} else {
				this.failSafeCounter.put(domainTLD, current);
			}
		} else {
			this.failSafeCounter.putIfAbsent(domainTLD, 0);

		}
	}
	
	
	private synchronized void updateFailSafeCache(String domainTLD){
		if (this.failSafeCounter.containsKey(domainTLD)){
				this.failSafeCounter.remove(domainTLD);
		}	
	}
	
	@SuppressWarnings("unused")
	private void runHTTPAsyncRetreiver(final boolean requiresContentType) throws InterruptedException {
		runHTTPAsyncRetreiver(requiresContentType,true);
	}
	
	private void addToGetQueue(String url){
		httpGetQueue.add(url);
	}
	
	private String contentToString(InputStream is, ContentType contentType){
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		
		String line;
		try {
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e){
					e.printStackTrace();
				}
			}
		}
		if (sb.toString().contains("</html>")) 
			return "";
		else {
			try{
				Lang tryLang = RDFLanguages.contentTypeToLang(contentType.toString());
				Model m = ModelFactory.createDefaultModel();
				RDFReader arp = m.getReader(tryLang.getName());
				arp.setProperty("WARN_REDEFINITION_OF_ID","EM_IGNORE");
				arp.read(m,new ByteArrayInputStream(sb.toString().getBytes()),"");
				return sb.toString();
			} catch (Exception e) {
				return "";
			}
		}
	}
	
	private void runHTTPAsyncRetreiver(final boolean requiresContentType, boolean useGet) throws InterruptedException {
		
		RequestConfig requestConfig = this.getRequestConfig(true);
		logger.trace("Starting HTTP retriever, HTTP queue size: {}", httpQueue.size());
		
		CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom().
				useSystemProperties().
				setDefaultRequestConfig(requestConfig).
				setMaxConnTotal(MAX_PARALLEL_REQS).
				setMaxConnPerRoute(MAX_PARALLEL_REQS/5).
				build();
		
		final CountDownLatch mainHTTPRetreiverLatch = new CountDownLatch(httpQueue.size());
				
		try {
			httpclient.start();
			
//			for(final String queuePeek : this.httpQueue) {
			
			while(!this.httpQueue.isEmpty()){
				final String queuePeek = this.httpQueue.poll();
				final String peekTLD = this.extractTopLevelDomainURI(queuePeek);
				
				// TODO: Remove artificial delay!!!! There must be a way to get rid of this
				logger.debug("Retrieving "+queuePeek);
				
				if (this.failSafeMap.containsKey(queuePeek)){
					this.failSafeMap.get(peekTLD);
					continue;
				}
				
				Thread.sleep(100);
				
				if (DiachronCacheManager.getInstance().existsInCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, queuePeek)) {
					// Request won't be sent, thus one pending request ought to be discounted from the latch
					mainHTTPRetreiverLatch.countDown();
					continue;
				}
				
				
				final CachedHTTPResource newResource = new CachedHTTPResource();
				final HttpClientContext localContext = HttpClientContext.create(); // Each request must have it's own context
				newResource.setUri(queuePeek);
				
				try {
					final HttpRequestBase request;
					if (useGet) 
						request = new HttpGet(queuePeek);	
					else
						request = new HttpHead(queuePeek);	

					if (requiresContentType || this.useContentType)
					{
						Header accept = new BasicHeader(HttpHeaders.ACCEPT, ACCEPT_TYPE);
						request.addHeader(accept);
					}
					HttpAsyncRequestProducer httpProd = HttpAsyncMethods.create(request);
		            
					AsyncCharConsumer<HttpResponse> consumer = new AsyncCharConsumer<HttpResponse>() {
		            	HttpResponse response = null;
		            	
						@Override
						protected void onCharReceived(CharBuffer buf, IOControl ioctrl) throws IOException {
							while (buf.hasRemaining()) {
								buf.get();
				            }
						}

						@Override
						protected void onResponseReceived(HttpResponse response) throws HttpException, IOException {
							this.response = response;
							logger.debug("Response received!", response.getStatusLine());
						}

						@Override
						protected HttpResponse buildResult(HttpContext context) throws Exception {
							return response;
						}
		            };
					
					httpclient.execute(httpProd, consumer, localContext, 
							new FutureCallback<HttpResponse>() {
								
								public void completed(final HttpResponse response) {
									if ((response.getStatusLine().getStatusCode() == 405)) {
										// if it was an HTTP Head with at 405 Method Not Allowed then we have to do it again with a GET :(
										addToGetQueue(request.getURI().toString());
									} else {
//										newResource.addResponse(response);
										try {
											if (followRedirections && localContext != null && localContext.getRedirectLocations() != null && localContext.getRedirectLocations().size() >= 1) {
												List<URI> uriRoute = new ArrayList<URI>();
												uriRoute.add(request.getURI());
												uriRoute.addAll(localContext.getRedirectLocations());
												try {
													logger.trace("Initiating redirection set for URI: {}. Num. requests: {}", queuePeek, uriRoute.size());
													
													List<HttpResponse> lst = followAsyncRedirection(uriRoute);
													for (HttpResponse res : lst){
														newResource.addResponse(res);
														if (((res.getEntity().getContent() != null) && (Double.valueOf(res.getHeaders("Content-Length")[0].getValue()) / 1000000) < 3)) {
															String cnt = contentToString(res.getEntity().getContent(),ContentType.getOrDefault(res.getEntity()));
															if (cnt.length() > 0) {
																newResource.setParsableContent(true);
																newResource.setContent(cnt);
															}
														}
													}
													logger.debug("Request completed with redirection set for URI: {}. {} pending requests", queuePeek, mainHTTPRetreiverLatch.getCount());
												} catch (IOException e) {
													logger.warn("Error following redirection: {}. Error: {}", uriRoute, e);
												}
											} else {
												logger.debug("Request for URI: {} successful. {}. {} redirs. {} pending requests", queuePeek, response.getStatusLine(), ((localContext.getRedirectLocations() != null)?(localContext.getRedirectLocations().size()):(0)), mainHTTPRetreiverLatch.getCount());
												newResource.addStatusLines(response.getStatusLine());
												newResource.addResponse(response);
												try{
													newResource.setContent(contentToString(response.getEntity().getContent(),ContentType.getOrDefault(response.getEntity())));
												} catch (Exception e1){
													logger.debug(e1.getLocalizedMessage());
												}
											}
										} catch (Exception e) {
											logger.debug("Exception during the request for redirect locations whith the following exception : {}", e.getLocalizedMessage());
											newResource.addStatusLines(response.getStatusLine());
										} finally {
											logger.trace("Adding resource to cache URI: {}", queuePeek);
											DiachronCacheManager.getInstance().addToCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, queuePeek, newResource);
											mainHTTPRetreiverLatch.countDown();
											updateFailSafeCache(peekTLD);
										}
									}
								}
		
								public void failed(final Exception ex) {
									BasicStatusLine statusLine = null;
									BasicHttpResponse response = null;
									// Properly set the status line
									if(ex instanceof TimeoutException) {
										statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_REQUEST_TIMEOUT, "Request failed: timed out");
									} else {
										statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 0, "Request could not be processed");
									}
									
									response = new BasicHttpResponse(statusLine);
									newResource.setDereferencabilityStatusCode(StatusCode.BAD);
									newResource.addStatusLines(statusLine);
									// If response could be inferred from the exception, set it
									if(response != null) {
										newResource.addResponse(response);
									}
									
									DiachronCacheManager.getInstance().addToCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, queuePeek, newResource);

									logger.debug("Failed in retreiving request : {}, with the following exception : {}. {} pending requests", request.getURI().toString(), ex, mainHTTPRetreiverLatch.getCount());
									mainHTTPRetreiverLatch.countDown();
									addToFailSafeDecision(peekTLD);
								}
		
								public void cancelled() {
									// Some unexpected, nasty problems, such as bad URIs can occur when trying to build or process the request, all of which must be handled
									BasicStatusLine statusLine = null;
									BasicHttpResponse response = null;
									// Properly set the status line
									statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 0, "Request could not be processed: Cancelled");
									response = new BasicHttpResponse(statusLine);
									
									newResource.setDereferencabilityStatusCode(StatusCode.BAD);
									newResource.addStatusLines(statusLine);
									// If response could be inferred from the exception, set it
									if(response != null) {
										newResource.addResponse(response);
									}
									
									DiachronCacheManager.getInstance().addToCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, queuePeek, newResource);
									logger.debug("The retreival for {} was cancelled. {} pending requests",request.getURI().toString(), mainHTTPRetreiverLatch.getCount());
									mainHTTPRetreiverLatch.countDown();
									addToFailSafeDecision(peekTLD);
								}
							});
					logger.trace("Request launched: {}", queuePeek);
				} catch(Throwable tex) {
					// Some unexpected, nasty problems, such as bad URIs can occur when trying to build or process the request, all of which must be handled
					BasicStatusLine statusLine = null;
					BasicHttpResponse response = null;
					// Properly set the status line
					statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 0, "Request could not be processed: Exception detected");
					response = new BasicHttpResponse(statusLine);
					
					newResource.setDereferencabilityStatusCode(StatusCode.BAD);
					newResource.addStatusLines(statusLine);
					// If response could be inferred from the exception, set it
					if(response != null) {
						newResource.addResponse(response);
					}
					
					this.addToFailSafeDecision(peekTLD);
					
					DiachronCacheManager.getInstance().addToCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, queuePeek, newResource);

					logger.warn("Unexpected error building or processing request : " + queuePeek, tex);
					mainHTTPRetreiverLatch.countDown();
				} 
			}
			
			mainHTTPRetreiverLatch.await();
			logger.trace("Completed HTTP retriever task");
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				logger.warn("I/O exception attempting to close HTTP client", e);
			}
		}
	}
	
	protected List<HttpResponse> followAsyncRedirection(List<URI> uriRoute) throws IOException, InterruptedException {
		final List<HttpResponse> httpResponses = Collections.synchronizedList(new LinkedList<HttpResponse>());
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
								logger.debug("---> Redirection completed: {}, {} pending requests", request.getURI().toString(), redirectionLatch.getCount());
								redirectionLatch.countDown();
								httpResponses.add(response);
							}

							public void failed(final Exception ex) {
								logger.debug("---> Failed in retreiving follow redirection request : {}, with the following exception : {}. {} pending requests", request.getURI().toString(), ex.getLocalizedMessage(), redirectionLatch.getCount());
								redirectionLatch.countDown();
							}

							public void cancelled() {
								logger.debug("---> The retreival for {} was cancelled. {} pending requests", request.getURI().toString(), redirectionLatch.getCount());
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
		Builder configBuilder = null;
				
		configBuilder = RequestConfig.custom().
				setSocketTimeout(TIMEOUT).
				setConnectTimeout(TIMEOUT).
				setConnectionRequestTimeout(TIMEOUT).
				setRedirectsEnabled(followRedirects).
				setAuthenticationEnabled(false);
		
		// If a proxy was set to be used, set it
		if(getWebProxy() != null && !getWebProxy().trim().equals("") && getWebProxyPort() != null) {
			configBuilder.setProxy(new HttpHost(getWebProxy(), getWebProxyPort()));
		}
		
		return configBuilder.build();
	}

	public boolean isPossibleURL(String url) {
		UrlValidator valid = new UrlValidator();
		return valid.isValid(url);
	}
	
	/**
	 * Sets the URL of the web proxy to be used when performing HTTP requests
	 * @param proxyUrlPort URL and port of the proxy (e.g. webcache.iai.uni-bonn.de)
	 */
	public static void setWebProxy(String proxyServer) {
		webProxy = proxyServer;
	}
	
	/**
	 * Sets the port of the web proxy to be used when performing HTTP requests
	 * @param proxyUrlPort URL and port of the proxy (e.g. 3128)
	 */
	public static void setWebProxyPort(int proxyPort) {
		webProxyPort = proxyPort;
	}
	
	/**
	 * Indicates if redirections returned on successful HTTP responses shall be followed
	 * @param follow True if redirections ought to be followed
	 */
	public static void setFollowRedirections(boolean follow) {
		followRedirections = follow;
	}
	
	/**
	 * Gets the web proxy server to be used when performing HTTP requests
	 * @param proxyUrlPort URL and port of the proxy (e.g. webcache.iai.uni-bonn.de)
	 */
	public static String getWebProxy() {
		return webProxy;
	}
	
	/**
	 * Gets the URL of the web proxy to be used when performing HTTP requests
	 * @param proxyUrlPort URL and port of the proxy (e.g. 3128)
	 */
	public static Integer getWebProxyPort() {
		return webProxyPort;
	}
	
	/**
	 * Gets whether redirections returned on successful HTTP responses shall be followed
	 * @return true if redirections will be followed, false otherwise
	 */
	public static boolean getFollowRedirections() {
		return followRedirections;
	}	
	
	/**
	 * Extract the Top Level Domain (for example http://bbc.co.uk) of the URI provided as parameter. 
	 * About URIs: The hierarchical part of the URI is intended to hold identification information hierarchical in nature. 
	 * If this part begins with a double forward slash ("//"), it is followed by an authority part and a path. 
	 * If it doesn't it contains only a path and thus it doesn't have a PLD (e.g. urns).
	 * @param resourceURI URI to extract the Top Level Domain from
	 * @return Top Level Domain of the URI
	 */
	public String extractTopLevelDomainURI(String resourceURI) {
		
		// Argument validation. Fail fast
		if(resourceURI == null) {
			return null;
		}
		
		String tldURI = null;
		
		int doubleSlashIx = resourceURI.indexOf("//");
		int pathFirstSlashIx = -1;
		
		// Check that the URI contains a double-slash, as the TLD is the scheme plus the authority part
		if(doubleSlashIx > 0 && (doubleSlashIx + 1) < resourceURI.length()) {
			
			pathFirstSlashIx = resourceURI.indexOf('/', doubleSlashIx + 2);
			
			// The TLD comprises the scheme and the path
			if(pathFirstSlashIx > (doubleSlashIx + 1)) {
				tldURI = resourceURI.substring(0, pathFirstSlashIx);
			} else {
				// There's no path part in the URI
				tldURI = resourceURI;
			}
		}
		
		return tldURI;
	}
	
	/**
	 * TODO: Remove main method, which was added solely for testing purposes
	 */
	public static void main(String [] args) throws InterruptedException{
		HTTPRetriever httpRetreiver = new HTTPRetriever();
	
//		String uri = "http://eprints.soton.ac.uk/id/eprint/10017";
//		String uri = "http://pdev.org.uk/pdevlemon/PDEN_LexicalEntry_1008";
//		String uri = "http://pleiades.stoa.org/places/55500001010#this";
//		String uri = "http://rdfs.org/ns/void#Dataset";
//		String uri = "http://pleiades.stoa.org/places/903083";
		String uri = "http://eprints.soton.ac.uk/10082/";
		httpRetreiver.addResourceToQueue(uri);
		httpRetreiver.start(true);
		Thread.sleep(5000);
	
		CachedHTTPResource httpResource = (CachedHTTPResource) DiachronCacheManager.getInstance().getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE,uri);
		while(httpResource == null){
			httpResource = (CachedHTTPResource) DiachronCacheManager.getInstance().getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE,uri);
		}
		
		Double len = -1.0d;
		for(SerialisableHttpResponse shr : httpResource.getResponses()){
			try {
				len = Double.valueOf(shr.getHeaders("Content-Length"));
				break;
			} catch (Exception e){}
		}
		if (len > -1)
			System.out.println(len/1000000);
		
		List<StatusLine> lst = httpResource.getStatusLines();
		for (StatusLine sl : lst){
			System.out.println(sl.getStatusCode());
		}
		
		System.out.println(httpResource.getContent());
		//System.out.println(httpResource.getResponses().get(0).getEntity().getContentType().getValue());
		//httpResource.getResponses().get(0).getHeaders("Content-Disposition");
//		String filename = httpResource.getResponses().get(0).getHeaders("Content-Disposition").replace("filename=\"", "").replace("\"", "");
//		Lang language = RDFLanguages.filenameToLang(filename);
//		System.out.println(WebContent.mapLangToContentType(language));
		
//		for (SerialisableHttpResponse res : httpResource.getResponses()){
//			System.out.println(res.getHeaders("Content-Type"));
//			System.out.println(res.getHeaders("Content-Disposition"));
//			try {
//				Model m = RDFDataMgr.loadModel(uri, Lang.TURTLE);
//				m.write(System.out);
//			} catch (RiotException e){
//				System.out.println("could not be identified");
//			}
//		}
		
		
		
		
		httpRetreiver.stop();
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
			httpConn.setRequestProperty("Content-Type", ACCEPT_TYPE);
			
			// Initiate the timer, as the call to getInputStream connects to the target resource and sends GET and HEADers
			startTimeStamp = System.currentTimeMillis();
			// Getting the input-stream of the response actually connects to the target and retrieves contents, which won't be consumed in this case
			responseStream = httpConn.getInputStream();
			// Response received, calculate delay
			delay = (System.currentTimeMillis() - startTimeStamp);
		} catch (Exception e){
			System.out.println(targetUrl);
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
