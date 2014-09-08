/**
 * 
 */
package eu.diachron.qualitymetrics.cache;

import java.util.Set;
import java.util.TreeSet;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;

/**
 * @author Jeremy Debattista
 * 
 */
public class CachedHTTPResource {

	private String uri;
	private HttpResponse response;
	private Set<StatusLine> statusLines = null;
	
	public HttpResponse getResponse() {
		return response;
	}
	public void setResponse(HttpResponse response) {
		this.response = response;
	}
	public Set<StatusLine> getStatusLines() {
		return statusLines;
	}
	public void addStatusLines(StatusLine statusLine) {
		if (this.statusLines == null) this.statusLines = new TreeSet<StatusLine>();
		this.statusLines.add(statusLine);
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	
}
