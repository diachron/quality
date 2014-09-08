/**
 * 
 */
package eu.diachron.qualitymetrics.utilities;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.junit.runner.notification.StoppedByUserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.DiachronCacheManager;

/**
 * @author Jeremy Debattista
 * 
 * Retreives HTTP resources
 */
public class HTTPRetreiver {

	final static Logger logger = LoggerFactory.getLogger(HTTPRetreiver.class);
	private static HTTPRetreiver instance = null;
	private ConcurrentLinkedQueue<String> httpQueue = new ConcurrentLinkedQueue<String>(); 
	private boolean isStopped = true;
	
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	
	protected HTTPRetreiver(){
		this.isStopped = false;
		
		Runnable retreiver = new Runnable(){
			public void run() {
				runHTTPRetreiver();
			}
		};
		executor.submit(retreiver); 
	};
	
	public static HTTPRetreiver getInstance(){
		if (instance == null) instance = new HTTPRetreiver();
		return instance;
	}
	
	public void addResourceToQueue(String resourceURI){
		this.httpQueue.add(resourceURI);
	}
	
	public void addListOfResourceToQueue(List<String> resourceURIs){
		this.httpQueue.addAll(resourceURIs);
	}
	
	/**
	 * Stops the HTTPRetreiver Process and destroys the instance
	 */
	public void stopHTTPRetreiver(){
		this.isStopped = true;
		executor.shutdown();
		instance = null;
	}
	
	private void runHTTPRetreiver(){
		RequestConfig requestConfig = this.getRequestConfig(true);
		CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom().setDefaultRequestConfig(requestConfig).build();
		final HttpClientContext localContext = HttpClientContext.create();

		httpclient.start();
		while(!isStopped){
			while (httpQueue.size() > 0){
				String queuePeak = httpQueue.peek();
				if (DiachronCacheManager.getInstance().existsInCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, queuePeak)){
					httpQueue.poll();
					continue;
				}
				
				final CachedHTTPResource newResource = new CachedHTTPResource();
				newResource.setUri(queuePeak);
				DiachronCacheManager.getInstance().addToCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, queuePeak, newResource);
				
				final HttpGet request = new HttpGet(httpQueue.poll());
				final CountDownLatch latch = new CountDownLatch(httpQueue.size());
				
                httpclient.execute(request, localContext, new FutureCallback<HttpResponse>() {
                    public void completed(final HttpResponse response) {
                    	latch.countDown();
                    	newResource.setResponse(response);
                    	if (localContext.getRedirectLocations().size() >= 1){
                    		List<URI> uriRoute = new ArrayList<URI>();
                    		uriRoute.add(request.getURI());
                    		uriRoute.addAll(localContext.getRedirectLocations());
                    		try {
								followRedirection(newResource, uriRoute);
								for(StatusLine i : newResource.getStatusLines()) 
									System.out.println(i + " -> ");
							} catch (IOException e) {
								e.printStackTrace();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
                    	} else {
                    		newResource.addStatusLines(response.getStatusLine());
                    		
                    	}
                    }

					public void failed(final Exception ex) {
                    	logger.debug("Failed in retreiving request : {}, with the following exception : {}", request.getURI().toString(), ex.getLocalizedMessage());
                        latch.countDown();
                    }

                    public void cancelled() {
                    	logger.debug("The retreival for {} was cancelled.", request.getURI().toString());
                        latch.countDown();
                    }

                });
				DiachronCacheManager.getInstance().addToCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, queuePeak, newResource);
			}
		}
		
	}
	
	protected void followRedirection(final CachedHTTPResource newResource, List<URI> uriRoute) throws IOException, InterruptedException {
		RequestConfig requestConfig = this.getRequestConfig(false);
		CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom().setDefaultRequestConfig(requestConfig).build();
		try {
        	final HttpClientContext localContext = HttpClientContext.create();

            httpclient.start();
            final List<HttpGet> requests = this.toHttpGetList(uriRoute);
            
            final CountDownLatch latch = new CountDownLatch(requests.size());
            for (final HttpGet request: requests) {
                httpclient.execute(request, localContext, new FutureCallback<HttpResponse>() {
                	
                    public void completed(final HttpResponse response) {
                        latch.countDown();
                        newResource.addStatusLines(response.getStatusLine());
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
	}
		
	private List<HttpGet> toHttpGetList(List<URI> uriRoute){
		List<HttpGet> requests = new ArrayList<HttpGet>();
		for (URI uri : uriRoute){
			requests.add(new HttpGet(uri.toString()));
		}
		
		return requests;
	}

	private RequestConfig getRequestConfig(boolean followRedirects){
		return RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(3000).setRedirectsEnabled(followRedirects).build();
	}
	
	
	
	public static void main(final String[] args) throws Exception {
		HTTPRetreiver rt = HTTPRetreiver.getInstance();
		rt.addResourceToQueue("http://dbpedia.org/resource/Asturias");
		//rt.stopHTTPRetreiver();
		CachedHTTPResource res = (CachedHTTPResource) DiachronCacheManager.getInstance().getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, "http://dbpedia.org/resource/Asturias");
		for(StatusLine s : res.getStatusLines()){
			System.out.println(s.getStatusCode());
		}
		rt.stopHTTPRetreiver();
    }

	
}
