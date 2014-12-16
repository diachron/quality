/**
 * 
 */
package eu.diachron.qualitymetrics.cache;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;

import de.unibonn.iai.eis.diachron.datatypes.StatusCode;
import de.unibonn.iai.eis.luzzu.cache.CacheObject;

/**
 * @author Jeremy Debattista
 * 
 */
public class CachedHTTPResource implements CacheObject{

	private String uri = "";
	private List<HttpResponse> responses = null;
	private List<StatusLine> statusLines = null;
	private StatusCode dereferencabilityStatusCode = null;
	
	public List<HttpResponse> getResponses() {
		return responses;
	}
	public void addResponse(HttpResponse response) {
		if (this.responses == null) this.responses = new ArrayList<HttpResponse>();
		this.responses.add(response);
		this.addStatusLines(response.getStatusLine());
	}
	public void addAllResponses(List<HttpResponse> responses) {
		if (this.responses == null) this.responses = new ArrayList<HttpResponse>();
		this.responses.addAll(responses);
		
		for(HttpResponse res : responses){
			this.addStatusLines(res.getStatusLine());
		}
	}
	public List<StatusLine> getStatusLines() {
		return statusLines;
	}
	public void addStatusLines(StatusLine statusLine) {
		if (this.statusLines == null) this.statusLines = new ArrayList<StatusLine>();
		this.statusLines.add(statusLine);
	}
	public void addAllStatusLines(List<StatusLine> statusLine) {
		if (this.statusLines == null) this.statusLines = new ArrayList<StatusLine>();
		this.statusLines.addAll(statusLine);
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
	
	
}
