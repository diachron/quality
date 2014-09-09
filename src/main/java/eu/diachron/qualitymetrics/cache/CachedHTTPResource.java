/**
 * 
 */
package eu.diachron.qualitymetrics.cache;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;

import de.unibonn.iai.eis.diachron.datatypes.StatusCode;

/**
 * @author Jeremy Debattista
 * 
 */
public class CachedHTTPResource {

	private String uri = "";
	private HttpResponse response = null;
	private List<StatusLine> statusLines = null;
	private StatusCode dereferencabilityStatusCode = null;
	
	public HttpResponse getResponse() {
		return response;
	}
	public void setResponse(HttpResponse response) {
		this.response = response;
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
