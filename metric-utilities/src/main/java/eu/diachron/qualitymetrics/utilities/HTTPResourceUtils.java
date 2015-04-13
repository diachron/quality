/**
 * 
 */
package eu.diachron.qualitymetrics.utilities;

import java.util.regex.Pattern;

import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;

import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource.SerialisableHttpResponse;

/**
 * @author Jeremy Debattista
 * 
 * Utility methods to parse HTTPResource responses
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
			if (WebContent.contentTypeToLang(ct) != null){
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
	
	public static ContentType determineActualContentType(CachedHTTPResource httpResource)
    {
		TypedInputStream in = RDFDataMgr.open(httpResource.getUri());
		String target = httpResource.getUri();
		String ctStr = in.getContentType();
		
        boolean isTextPlain = WebContent.contentTypeTextPlain.equals(ctStr) ;

        if (ctStr != null) ctStr = WebContent.contentTypeCanonical(ctStr) ;

        ContentType ct = null ;
        if ( ! isTextPlain ) ct = (ctStr==null) ? null : ContentType.create(ctStr) ;
        
        if ( ct == null ) ct = RDFLanguages.guessContentType(target) ;
        
        return ct ;
    }
	
	public static SerialisableHttpResponse getPossibleSemanticResponse(CachedHTTPResource httpResponse){
		for(SerialisableHttpResponse res : httpResponse.getResponses()){
			if ((res.getHeaders("Content-Location") != null) || (res.getHeaders("Content-Disposition") != null)  || (res.getHeaders("location") != null)){				
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
						null;
	}
	
}
