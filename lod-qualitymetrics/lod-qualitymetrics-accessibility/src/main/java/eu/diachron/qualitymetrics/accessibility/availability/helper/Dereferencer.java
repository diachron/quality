/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.availability.helper;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.StatusLine;

import de.unibonn.iai.eis.diachron.datatypes.StatusCode;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.DiachronCacheManager;
import eu.diachron.qualitymetrics.utilities.HTTPRetriever;

/**
 * @author Jeremy Debattista
 * 
 */
public class Dereferencer {

	public static boolean hasValidDereferencability(CachedHTTPResource httpResource){
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
		
		StatusCode scode = httpResource.getDereferencabilityStatusCode();
		return mapDerefStatusCode(scode);
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
	
//	public static void main (String [] args){
//		HTTPRetriever httpRetriever = new HTTPRetriever();
//		httpRetriever.addResourceToQueue("http://dbpedia.org/resource/11th_Bengal_Native_Infantry");
//		httpRetriever.start();
//		System.out.println(httpRetriever.isPossibleURL("http:/"));
//		
//		CachedHTTPResource res = (CachedHTTPResource) DiachronCacheManager.getInstance().getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, "http://dbpedia.org/resource/11th_Bengal_Native_Infantry");
//		while (res == null){
//			res = (CachedHTTPResource) DiachronCacheManager.getInstance().getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, "http://dbpedia.org/resource/11th_Bengal_Native_Infantry");
//		}
//		
//		System.out.println(hasValidDereferencability(res));
//	}

}
