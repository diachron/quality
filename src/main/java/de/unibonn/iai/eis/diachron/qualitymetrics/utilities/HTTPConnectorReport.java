package de.unibonn.iai.eis.diachron.qualitymetrics.utilities;

import com.hp.hpl.jena.graph.Node;

/**
 * @author jdebattist
 *
 * A helper class for the HTTPConnector which provides information
 * about the an HTTP Connection
 */
public class HTTPConnectorReport {

	private Node node;
	private int responseCode;
	private boolean isContentParsable = false;
	private String contentType = "";
	private String redirectLocation = "";
	private String uri;
	
	public Node getNode() {
		return node;
	}
	public void setNode(Node node) {
		this.node = node;
	}
	public int getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
	public boolean isContentParsable() {
		return isContentParsable;
	}
	public void setContentParsable(boolean isRDFContentParsable) {
		this.isContentParsable = isRDFContentParsable;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getRedirectLocation() {
		return redirectLocation;
	}
	public void setRedirectLocation(String redirectLocation) {
		this.redirectLocation = redirectLocation;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	
}
