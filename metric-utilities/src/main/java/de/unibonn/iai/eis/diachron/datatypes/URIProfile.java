package de.unibonn.iai.eis.diachron.datatypes;

import java.util.HashSet;
import java.util.Set;

public class URIProfile {

	private String uri;
	private boolean isBroken = false;
	private Status uriStatus;
	
	private Set<String> structuredContentType = new HashSet<String>(); 

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
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public Status getUriStatus() {
		return uriStatus;
	}
	public void setUriStatus(Status uriStatus) {
		this.uriStatus = uriStatus;
	}

	
}
