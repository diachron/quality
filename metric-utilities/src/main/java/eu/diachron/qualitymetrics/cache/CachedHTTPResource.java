/**
 * 
 */
package eu.diachron.qualitymetrics.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;

import de.unibonn.iai.eis.diachron.datatypes.StatusCode;
import de.unibonn.iai.eis.luzzu.cache.CacheObject;

/**
 * @author Jeremy Debattista
 * 
 */
public class CachedHTTPResource implements CacheObject {
	private static final long serialVersionUID = -5625345902018709236L;
	
	private String uri = "";
	private List<SerialisableHttpResponse> responses = null;
	private List<StatusLine> statusLines = null;
	private StatusCode dereferencabilityStatusCode = null;
	private Boolean parsableContent = null;
	
	public List<SerialisableHttpResponse> getResponses() {
		return responses;
	}
	public void addResponse(HttpResponse response) {
		if (this.responses == null) this.responses = new ArrayList<SerialisableHttpResponse>();
		
		this.responses.add(new SerialisableHttpResponse(response));
		this.addStatusLines(response.getStatusLine());
	}
	public void addAllResponses(List<HttpResponse> responses) {
		if (this.responses == null) this.responses = new ArrayList<SerialisableHttpResponse>();
		
		for(HttpResponse res : responses){
			this.responses.add(new SerialisableHttpResponse(res));
			this.addStatusLines(res.getStatusLine());
		}
	}
	public List<StatusLine> getStatusLines() {
		return statusLines;
	}
	public void addStatusLines(StatusLine statusLine) {
		if (this.statusLines == null) this.statusLines = new ArrayList<StatusLine>();
		synchronized(statusLines) {
			this.statusLines.add(statusLine);
		}
	}
	public void addAllStatusLines(List<StatusLine> statusLine) {
		if (this.statusLines == null) this.statusLines = new ArrayList<StatusLine>();
		synchronized(statusLines) {
			this.statusLines.addAll(statusLine);
		}
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public StatusCode getDereferencabilityStatusCode() {
		return dereferencabilityStatusCode;
	}
	public void setDereferencabilityStatusCode(StatusCode dereferencabilityStatusCode) {
		this.dereferencabilityStatusCode = dereferencabilityStatusCode;
	}
	
	public Boolean isContentParsable() {
		return parsableContent;
	}
	public void setParsableContent(boolean containsRDF) {
		this.parsableContent = containsRDF;
	}

	public class SerialisableHttpResponse implements Serializable{
		
		//http://en.wikipedia.org/wiki/List_of_HTTP_header_fields

		private static final long serialVersionUID = 5007740429193218086L;
		private Map<String,String> headers = new HashMap<String,String>();
		
		public SerialisableHttpResponse(HttpResponse _response){
			for(Header h : _response.getAllHeaders()) headers.put(h.getName(), h.getValue());
		}

		public String getHeaders(String name){
			return headers.get(name);
		}
	}
	
}
