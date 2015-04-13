/**
 * 
 */
package eu.diachron.qualitymetrics.utilities;

import java.util.regex.Pattern;

import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.riot.RDFDataMgr;

import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource.SerialisableHttpResponse;

/**
 * @author Jeremy Debattista
 * 
 * Utility methods to parse HTTPResource responses.
 * Some of the below methods are modified from Apache Jena
 */
public class HTTPResourceUtils {
	
	/**
	 * Regular expression matching filenames as provided in the Content-Disposition header of HTTP responses.
	 * Note that Pattern instances are thread-safe and are intended to create a new Matcher instance upon each usage
	 */
	private static final Pattern ptnFileName = Pattern.compile(".*filename=([^;\\s]+).*");


	public static SerialisableHttpResponse getSemanticResponse(CachedHTTPResource httpResponse){
		for(SerialisableHttpResponse res : httpResponse.getResponses()){
			String ct = parsedContentType(res.getHeaders("Content-Type"));
			if (ct.equals("text/plain")) continue;
			if (LinkedDataContent.contentTypeToLang(ct) != null){
				return res;
			}
		}
		return null;
	}
	
	public static String parsedContentType(String ct){
		String[] s1 = ct.split(",");
		for(String s : s1){
			String[] p = s.split(";");
			return p[0];
		}
		return "";
	}
	
	public static String determineActualContentType(CachedHTTPResource httpResource)
    {
		TypedInputStream in = RDFDataMgr.open(httpResource.getUri());
		String target = httpResource.getUri();
		String ctStr = in.getContentType();
		
        boolean isTextPlain = (ctStr.equals("text/plain")) ? true : false ;

        if (ctStr != null) ctStr = LinkedDataContent.contentTypeCanonical(ctStr) ;

        String ct = null ;
        if (!isTextPlain ) ct = (ctStr==null) ? null : ctStr ;
        
        if ( ct == null ) ct = LinkedDataContent.guessContentType(target) ;
        
        return ct ;
    }
	
	public static SerialisableHttpResponse getPossibleSemanticResponse(CachedHTTPResource httpResponse){
		for(SerialisableHttpResponse res : httpResponse.getResponses()){
			if ((res.getHeaders("Content-Location") != null) || (res.getHeaders("Content-Disposition") != null)  || (res.getHeaders("location") != null) || (res.getHeaders("Location") != null)) {				
				return res;
			}
		}
		return null;
	}
	
	public static String getResourceLocation(SerialisableHttpResponse res){
		return (res.getHeaders("Content-Location") != null) 
				? res.getHeaders("Content-Location") :
				(res.getHeaders("Content-Disposition") != null && (ptnFileName.matcher(res.getHeaders("Content-Disposition"))).matches()) 
					? (ptnFileName.matcher(res.getHeaders("Content-Disposition"))).group(1) :
					(res.getHeaders("location") != null) 
						? res.getHeaders("location") : 
						(res.getHeaders("Location") != null) 
							? res.getHeaders("Location") : 
							null;
	}
	
	/**
	 * In this method we use a number of heuristics in order to do
	 * a URI lookup for semantic resources without having to parse
	 * (or process) the URI's content. This idea is based
	 * on the work of Umbrich et al.: Four Heuristics
	 * to Guide Structured Content Crawling.
	 * 
	 * In our case, we have two heuristics, that either of them would
	 * return true if satisfied by a semantic URI.
	 * 
	 * @param httpResponse
	 */
	public static boolean semanticURILookup(CachedHTTPResource httpResponse){
		//H1: Does the resource have the correct semantic content-type?
		SerialisableHttpResponse h1 = getSemanticResponse(httpResponse);
		if (h1 != null){
			//h1 passed
			return true;
		}
		
		//H2: Does the semantic resource have the correct file type?
		SerialisableHttpResponse h2 = getPossibleSemanticResponse(httpResponse);
		if (h2 != null){
			String resourceFile = getResourceLocation(h2);
			String ct = LinkedDataContent.guessContentType(resourceFile);
			if (ct != null){
				//h2 passed
				return true;
			}
		}
		
		return false;
	}	
}
