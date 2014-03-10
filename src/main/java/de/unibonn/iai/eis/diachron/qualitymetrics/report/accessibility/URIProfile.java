package de.unibonn.iai.eis.diachron.qualitymetrics.report.accessibility;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;


public class URIProfile {

	private Node uriNode;
	private int httpStatusCode = 0;
	private boolean isValidDereferencableURI = false;
	private boolean isBroken = false;
	
	private Set<String> structuredContentType = new HashSet<String>(); 

	public Node getUriNode() {
		return uriNode;
	}
	public void setUriNode(Node uri) {
		this.uriNode = uri;
	}
	public int getHttpStatusCode() {
		return httpStatusCode;
	}
	public void setHttpStatusCode(int httpStatusCode) {
		this.httpStatusCode = httpStatusCode;
	}
	public boolean isValidDereferencableURI() {
		return isValidDereferencableURI;
	}
	public void setValidDereferencableURI(boolean isValidDereferencableURI) {
		this.isValidDereferencableURI = isValidDereferencableURI;
	}
	public boolean isBroken() {
		return isBroken;
	}
	public void setBroken(boolean isBroken) {
		this.isBroken = isBroken;
	}
	public Set<String> getStructuredContentType() {
		return structuredContentType;
	}
	public void addToStructuredContentType(String contentType) {
		this.structuredContentType.add(contentType);
	}

	
}
