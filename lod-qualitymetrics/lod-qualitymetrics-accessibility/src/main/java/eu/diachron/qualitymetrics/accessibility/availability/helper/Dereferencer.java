/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.availability.helper;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.web.HttpOp;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.unibonn.iai.eis.diachron.datatypes.StatusCode;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.DiachronCacheManager;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource.SerialisableHttpResponse;
import eu.diachron.qualitymetrics.utilities.HTTPRetriever;
import eu.diachron.qualitymetrics.utilities.LinkedDataContent;

/**
 * @author Jeremy Debattista
 * 
 */
public class Dereferencer {
	
	final static RequestConfig requestConfig = RequestConfig.custom()
			.setSocketTimeout(2000)
			.setConnectTimeout(2000)
			.setRedirectsEnabled(true)
			.build();

	final static CloseableHttpClient httpClient = HttpClientBuilder
								.create()
								.setDefaultRequestConfig(requestConfig)
								.build();
	
	static{
		HttpOp.setDefaultHttpClient(httpClient);
		HttpOp.createCachingHttpClient();
	}
	/*
	 * In order to improve the scalability of the dereferencer
	 * we will keep a fail-safe map, similar to the vocabulary
	 * loader, that keeps track of the availability of RDF
	 * resources in namespaces
	 */
    private static ConcurrentMap<String, Boolean> failSafeMap = new ConcurrentLinkedHashMap.Builder<String, Boolean>().maximumWeightedCapacity(10).build(); // A small fail-safe map that checks whether a domain retrieves vocabs.
    private static ConcurrentMap<String, Integer> failSafeCounter = new ConcurrentLinkedHashMap.Builder<String, Integer>().maximumWeightedCapacity(1000).build(); // keeps a counter of the number of times an NS was accessed before putting the NS to the fail-safe map
    private static final Integer NS_MAX_RETRIES = 40;
	
	private static synchronized void addToFailSafeDecision(String domain){
		if (failSafeCounter.containsKey(domain)){
			Integer current = failSafeCounter.get(domain) + 1;
			if (current == NS_MAX_RETRIES){
				failSafeMap.put(domain, true);
				failSafeCounter.put(domain, 0);
			} else {
				failSafeCounter.put(domain, current);
			}
		} else {
			failSafeCounter.putIfAbsent(domain, 0);
		}
	}
	
	public static boolean hasValidDereferencability(CachedHTTPResource httpResource){
		dereferencabilityCode(httpResource);
		parsable(httpResource);
		
		StatusCode scode = httpResource.getDereferencabilityStatusCode();
		return (mapDerefStatusCode(scode) && httpResource.isContentParsable());
	}
	
	private static void dereferencabilityCode(CachedHTTPResource httpResource){
		if (httpResource.getDereferencabilityStatusCode() == null){
			List<Integer> statusCode = getStatusCodes(httpResource.getStatusLines());
			
			if (httpResource.getUri().contains("#") && statusCode.contains(200)) httpResource.setDereferencabilityStatusCode(StatusCode.HASH);
			else if (statusCode.contains(200)){
				httpResource.setDereferencabilityStatusCode(StatusCode.SC200);
				if (statusCode.contains(303)) httpResource.setDereferencabilityStatusCode(StatusCode.SC303);
				else {
					if (statusCode.contains(301)) { 
						httpResource.setDereferencabilityStatusCode(StatusCode.SC301);
					}
					else if (statusCode.contains(302)){
						httpResource.setDereferencabilityStatusCode(StatusCode.SC302);
					}
					else if (statusCode.contains(307)) {
						httpResource.setDereferencabilityStatusCode(StatusCode.SC307);
					} else {
						if (hasBad3xxCode(statusCode)) httpResource.setDereferencabilityStatusCode(StatusCode.SC3XX);
					}
				}
			}
			
			if (has4xxCode(statusCode)) httpResource.setDereferencabilityStatusCode(StatusCode.SC4XX);
			
			if (has5xxCode(statusCode)) httpResource.setDereferencabilityStatusCode(StatusCode.SC5XX);
		} 			
	}
	
	private static boolean mapDerefStatusCode(StatusCode statusCode){
		if(statusCode == null) {
			return false;
		} else {
			switch(statusCode){
				case SC303 : case HASH : return true;
				default : return false;
			}
		}
	}
	
	private static List<Integer> getStatusCodes(List<StatusLine> statusLines){
		ArrayList<Integer> codes = new ArrayList<Integer>();
		
		if(statusLines != null) {
			synchronized(statusLines) {
				for(StatusLine s : statusLines){
					codes.add(s.getStatusCode());
				}
			}
		}
		
		return codes;
	}
	
	private static boolean has4xxCode(List<Integer> statusCode){
		for (int i : statusCode){
			if ((i >= 400) && (i < 499))  return true; else continue;
		}
		return false;
	}
	
	private static boolean has5xxCode(List<Integer> statusCode){
		for (int i : statusCode){
			if ((i >= 500) && (i < 599))  return true; else continue;
		}
		return false;
	}
	
	private static boolean hasBad3xxCode(List<Integer> statusCode){
		for (int i : statusCode){
			if ((i == 300) || (i == 304) || (i == 305) || 
					(i == 306) || (i == 308) ||
					((i >= 308) && (i < 399)))  return true; else continue;
		}
		return false;
	}

	public static boolean hasOKStatus(CachedHTTPResource resource) {
		List<StatusLine> lstStatusLines = resource.getStatusLines();
		
		if(lstStatusLines != null) {
			synchronized(lstStatusLines) {
				return lstStatusLines.toString().contains("200 OK");
			}
		}
		return false;
	}
	
	public static void parsable(CachedHTTPResource resource){
		if (resource.isContentParsable() == null) {
			String ns = ModelFactory.createDefaultModel().createResource(resource.getUri()).getNameSpace();
			if (!(failSafeMap.containsKey(ns))){
				Lang tryLang = null;
				double len = -1.0d;
				for (SerialisableHttpResponse shr : resource.getResponses()){
					try {
						len = Double.valueOf(shr.getHeaders("Content-Length"));
					} catch (Exception e){}				
					try {
						tryLang = LinkedDataContent.contentTypeToLang(shr.getHeaders("Content-Type"));
					} catch (Exception e){}	
					
					if ((tryLang != null) && (len > -1)) break;
				}
				
				len = len/1000000;
				if ((len > 0) && (len < 10)){
					// Load model in memory if file is under 10 MB
					try{
						//Model m = RDFDataMgr.loadModel(resource.getUri(), (tryLang == null) ? Lang.RDFXML : tryLang);
						Model m = ModelFactory.createDefaultModel();
						if (resource.getContent() != null){
							m.read(new ByteArrayInputStream(resource.getContent().getBytes()),null, (tryLang == null) ? Lang.RDFXML.getName() : tryLang.getName());
						} else {
							m = RDFDataMgr.loadModel(resource.getUri(), (tryLang == null) ? Lang.RDFXML : tryLang);
						}
	
						if (m.size() > 0){
							resource.setParsableContent(true);
							failSafeCounter.remove(ns);
						} else {
							addToFailSafeDecision(ns);
							resource.setParsableContent(false);
						}
					} catch (RiotException re){
						resource.setParsableContent(false);
						addToFailSafeDecision(ns);
					} catch (Exception e){
						resource.setParsableContent(false);
						addToFailSafeDecision(ns);
					}
				} else {
					try{
						if (tryLang == null) resource.setParsableContent(ModelParser.timeoutModel(resource.getUri()));
						else resource.setParsableContent(ModelParser.timeoutModel(resource.getUri(),tryLang));
						
						failSafeCounter.remove(ns);
					} catch (Exception e){
						addToFailSafeDecision(ns);
						resource.setParsableContent(false);
					}
				}
			} else {
				resource.setParsableContent(false);
			}
		}
	}


	// Parse with the given language only an nothing else
	public static void parsable(CachedHTTPResource resource, Lang lang){
		if (resource.isContentParsable() == null) {
			String ns = ModelFactory.createDefaultModel().createResource(resource.getUri()).getNameSpace();
			if (!(failSafeMap.containsKey(ns))){
				double len = -1.0d;
				for (SerialisableHttpResponse shr : resource.getResponses()){
					try {
						len = Double.valueOf(shr.getHeaders("Content-Length"));
					} catch (Exception e){}			
					
					if  (len > 0) break;
				}
				
				len = len/1000000;
				if ((len > 0) && (len < 10)) {
					// Load model in memory if file is under 10 MB
					try{
						Model m = ModelFactory.createDefaultModel();
						if (resource.getContent() != null){
							m.read(new ByteArrayInputStream(resource.getContent().getBytes()),null, lang.getName());
						} else {
							m = RDFDataMgr.loadModel(resource.getUri(), lang);
						}
	
						if (m.size() > 0){
							resource.setParsableContent(true);
							failSafeCounter.remove(ns);
						} else {
							addToFailSafeDecision(ns);
							resource.setParsableContent(false);
						}
					} catch (RiotException re){
						resource.setParsableContent(false);
						addToFailSafeDecision(ns);
					} catch (Exception e){
						resource.setParsableContent(false);
						addToFailSafeDecision(ns);
					}
				} else {
					try{
						resource.setParsableContent(ModelParser.snapshotParser(resource.getUri(), lang));
						failSafeCounter.remove(ns);
					} catch (Exception e){
						addToFailSafeDecision(ns);
						resource.setParsableContent(false);
					}
				}
			} else {
				resource.setParsableContent(false);
			}
		}
	}
	
	public static void main (String [] args) throws InterruptedException{
		HTTPRetriever httpRetriever = new HTTPRetriever();
		
		List<String> lst = new ArrayList<String>();
		lst.add("http://eprints.soton.ac.uk/id/person/ext-1e76cb65e67ad783a57a8eeddfa5e9f8");
//		lst.add("http://eprints.soton.ac.uk/10081/");
//		lst.add("http://eprints.soton.ac.uk/10082/");
//		lst.add("http://eprints.soton.ac.uk/10083/");
//		lst.add("http://eprints.soton.ac.uk/10084/");
//		lst.add("http://eprints.soton.ac.uk/10085/");
//		lst.add("http://eprints.soton.ac.uk/10086/");
//		lst.add("http://eprints.soton.ac.uk/10087/");
//		lst.add("http://eprints.soton.ac.uk/10088/");
//		lst.add("http://eprints.soton.ac.uk/10089/");
//		lst.add("http://eprints.soton.ac.uk/10090/");
//		lst.add("http://eprints.soton.ac.uk/10091/");
//		lst.add("http://eprints.soton.ac.uk/10092/");
//		lst.add("http://eprints.soton.ac.uk/10093/");
//		lst.add("http://eprints.soton.ac.uk/10094/");
//		lst.add("http://eprints.soton.ac.uk/10095/");
//		lst.add("http://eprints.soton.ac.uk/10096/");
//		lst.add("http://eprints.soton.ac.uk/10097/");
//		lst.add("http://eprints.soton.ac.uk/10098/");
//		lst.add("http://eprints.soton.ac.uk/10099/");
		
//		String url = "http://eprints.soton.ac.uk/10094/";
//		httpRetriever.addResourceToQueue(url);
		httpRetriever.addListOfResourceToQueue(lst);
		httpRetriever.start();
		Thread.sleep(1000);
		
		for(String url : lst){
			CachedHTTPResource res = (CachedHTTPResource) DiachronCacheManager.getInstance().getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, url);
			while (res == null){
				res = (CachedHTTPResource) DiachronCacheManager.getInstance().getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, url);
			}
			System.out.println(url + " " +hasValidDereferencability(res));
//			StatusCode sc = res.getDereferencabilityStatusCode();
//			System.out.println(sc);
		}
		


	}

}
