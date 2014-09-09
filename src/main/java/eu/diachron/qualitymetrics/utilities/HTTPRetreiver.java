/**
 * 
 */
package eu.diachron.qualitymetrics.utilities;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicStatusLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unibonn.iai.eis.diachron.datatypes.StatusCode;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.DiachronCacheManager;

/**
 * @author Jeremy Debattista
 * 
 * Retreives HTTP resources
 */

//TODO: Async methods and with consumer-producer queue
public class HTTPRetreiver {

	final static Logger logger = LoggerFactory.getLogger(HTTPRetreiver.class);
	private boolean completedActions = false;
	private boolean isRunning = true;

	private static HTTPRetreiver instance = null;
	private ConcurrentLinkedQueue<String> httpQueue = new ConcurrentLinkedQueue<String>();

	private ExecutorService executor = Executors.newSingleThreadExecutor();
	protected Future<?> futureTask;
	
	
	protected HTTPRetreiver() {
//		Runnable retreiver = new Runnable() {
//			public void run() {
//				runHTTPAsyncRetreiver();
//			}
//		};
//		executor.submit(retreiver);
	}

	public static HTTPRetreiver getInstance() {
		if (instance == null)
			instance = new HTTPRetreiver();
		return instance;
	}

	public void addResourceToQueue(String resourceURI) {
		this.httpQueue.add(resourceURI);
		//this.runHTTPRetreiver(resourceURI);
	}

	public void addListOfResourceToQueue(List<String> resourceURIs) {
		this.httpQueue.addAll(resourceURIs);
	}
	
	public void startHTTPRetreiver(){
		runHTTPAsyncRetreiver();
	}

	/**
	 * Stops the HTTPRetreiver Process and destroys the instance
	 */
	public void stopHTTPRetreiver() {
		executor.shutdown();
		instance = null;
	}

	private void runHTTPRetreiver(String queuePeak) {
		RequestConfig requestConfig = this.getRequestConfig(true);
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
		HttpClientContext localContext = HttpClientContext.create();
//		while (httpQueue.size() > 0) {
//			String queuePeak = httpQueue.peek();
			if (DiachronCacheManager.getInstance().existsInCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, queuePeak)) {
//				httpQueue.poll();
//				continue;
				return;
			}

			CachedHTTPResource newResource = new CachedHTTPResource();
			newResource.setUri(queuePeak);
			DiachronCacheManager.getInstance().addToCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, queuePeak, newResource);

			//HttpGet request = new HttpGet(httpQueue.poll());
			HttpGet request = new HttpGet(queuePeak);
			CloseableHttpResponse response;
			try {
				response = httpclient.execute(request, localContext);
				newResource.setResponse(response);

				try {
					if (localContext.getRedirectLocations().size() >= 1) {

						List<URI> uriRoute = new ArrayList<URI>();
						uriRoute.add(request.getURI());
						uriRoute.addAll(localContext.getRedirectLocations());
						newResource.addAllStatusLines(followRedirection(uriRoute));
					} else {
						newResource.addStatusLines(response.getStatusLine());
					}
				} catch (Exception e) {
					logger.debug("Exception during the request for redirect locations whith the following exception : {}",e.getLocalizedMessage());
					newResource.addStatusLines(response.getStatusLine());
				}
			} catch (ClientProtocolException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException ex) {
				newResource.setDereferencabilityStatusCode(StatusCode.BAD);
				newResource.addStatusLines(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 0,"Request could not be processed"));
				DiachronCacheManager.getInstance().addToCache(DiachronCacheManager.HTTP_RESOURCE_CACHE,queuePeak, newResource);
				logger.debug("Failed in retreiving request : {}, with the following exception : {}",request.getURI().toString(), ex.getLocalizedMessage());
			}
			DiachronCacheManager.getInstance().addToCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, queuePeak,newResource);

//		}
	}
	
	private List<StatusLine> followRedirection(List<URI> uriRoute) throws ClientProtocolException, IOException{
		List<StatusLine> statusLines = Collections.synchronizedList(new ArrayList<StatusLine>());
		RequestConfig requestConfig = this.getRequestConfig(true);
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
		HttpClientContext localContext = HttpClientContext.create();
		List<HttpGet> requests = this.toHttpGetList(uriRoute);

		for (final HttpGet request : requests) {
			CloseableHttpResponse response = httpclient.execute(request, localContext);
			statusLines.add(response.getStatusLine());
		}
		
		return statusLines;
	}

	private void runHTTPAsyncRetreiver() {
		RequestConfig requestConfig = this.getRequestConfig(true);
		CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
				.setDefaultRequestConfig(requestConfig).build();
		final HttpClientContext localContext = HttpClientContext.create();

		httpclient.start();
		while ((httpQueue.size() > 0)){
			final String queuePeak = httpQueue.peek();
			if (DiachronCacheManager.getInstance().existsInCache(
					DiachronCacheManager.HTTP_RESOURCE_CACHE, queuePeak)) {
				httpQueue.poll();
				continue;
			}

			final CachedHTTPResource newResource = new CachedHTTPResource();
			newResource.setUri(queuePeak);
			DiachronCacheManager.getInstance().addToCache(
					DiachronCacheManager.HTTP_RESOURCE_CACHE, queuePeak,
					newResource);

			final HttpGet request = new HttpGet(httpQueue.poll());
			final CountDownLatch latch = new CountDownLatch(httpQueue.size());

			httpclient.execute(request, localContext,
					new FutureCallback<HttpResponse>() {
						public void completed(final HttpResponse response) {
							latch.countDown();
							newResource.setResponse(response);
							try {
								if (localContext.getRedirectLocations().size() >= 1) {
									List<URI> uriRoute = new ArrayList<URI>();
									uriRoute.add(request.getURI());
									uriRoute.addAll(localContext
											.getRedirectLocations());
									try {
										newResource
												.addAllStatusLines(followAsyncRedirection(uriRoute));
									} catch (IOException e) {
										e.printStackTrace();
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								} else {
									newResource.addStatusLines(response
											.getStatusLine());
								}
							} catch (Exception e) {
								logger.debug(
										"Exception during the request for redirect locations whith the following exception : {}",
										e.getLocalizedMessage());
								newResource.addStatusLines(response
										.getStatusLine());
							}
							DiachronCacheManager.getInstance().addToCache(
									DiachronCacheManager.HTTP_RESOURCE_CACHE,
									queuePeak, newResource);
						}

						public void failed(final Exception ex) {
							newResource.setDereferencabilityStatusCode(StatusCode.BAD);
							newResource.addStatusLines(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 0,"Request could not be processed"));
							DiachronCacheManager.getInstance().addToCache(DiachronCacheManager.HTTP_RESOURCE_CACHE,queuePeak, newResource);

							logger.debug("Failed in retreiving request : {}, with the following exception : {}",request.getURI().toString(),ex.getLocalizedMessage());
							latch.countDown();
						}

						public void cancelled() {
							logger.debug("The retreival for {} was cancelled.",
									request.getURI().toString());
							latch.countDown();
						}
					});
		}
	}

	protected List<StatusLine> followAsyncRedirection(List<URI> uriRoute) throws IOException, InterruptedException {
		final List<StatusLine> statusLines = Collections.synchronizedList(new ArrayList<StatusLine>());
		RequestConfig requestConfig = this.getRequestConfig(false);
		CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom().setDefaultRequestConfig(requestConfig).build();
		try {
			final HttpClientContext localContext = HttpClientContext.create();
			httpclient.start();
			final List<HttpGet> requests = this.toHttpGetList(uriRoute);

			final CountDownLatch latch = new CountDownLatch(requests.size());
			for (final HttpGet request : requests) {
				httpclient.execute(request, localContext, new FutureCallback<HttpResponse>() {

							public void completed(final HttpResponse response) {
								latch.countDown();
								statusLines.add(response.getStatusLine());
							}

							public void failed(final Exception ex) {
								latch.countDown();
							}

							public void cancelled() {
								latch.countDown();
							}

						});
			}
			latch.await();
		} finally {
			httpclient.close();
		}
		return statusLines;
	}

	private List<HttpGet> toHttpGetList(List<URI> uriRoute) {
		List<HttpGet> requests = new ArrayList<HttpGet>();
		for (URI uri : uriRoute) {
			requests.add(new HttpGet(uri.toString()));
		}

		return requests;
	}

	private RequestConfig getRequestConfig(boolean followRedirects) {
		return RequestConfig.custom().setSocketTimeout(3000)
				.setConnectTimeout(3000).setRedirectsEnabled(followRedirects)
				.build();
	}

	public boolean isPossibleURL(String url) {
		// TODO: add more protocols
		return ((url.startsWith("http")) || (url.startsWith("https")));
	}

	public boolean hasCompletedActions() {
		return this.httpQueue.isEmpty();
	}

	
//	 public static void main(String [] args) throws InterruptedException{
//		 HTTPRetreiver httpRetreiver = HTTPRetreiver.getInstance();
//		 httpRetreiver.addResourceToQueue("http://fb.comiles.eu/?c=person&id=natanael");
//		 Thread.sleep(5000);
//		 CachedHTTPResource httpResource = (CachedHTTPResource)
//		 DiachronCacheManager.getInstance().getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE,
//		 "http://fb.comiles.eu/?c=person&id=natanael");
//		 int i = 0;
//	 }
}
	