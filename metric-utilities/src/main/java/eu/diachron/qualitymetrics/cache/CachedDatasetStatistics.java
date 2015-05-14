/**
 * 
 */
package eu.diachron.qualitymetrics.cache;

import de.unibonn.iai.eis.luzzu.cache.CacheObject;

/**
 * @author Jeremy Debattista
 * 
 */
public class CachedDatasetStatistics implements CacheObject {
	private static final long serialVersionUID = -3848853365217010700L;

	private String datasetURI = "";
	private String datasetLicense = "";
	private String sparqlEndPoint = "";
	private boolean datasetSubset = false;
	
	public CachedDatasetStatistics(String datasetURI){
		this.datasetURI = datasetURI;
	}
	
	public String getDatasetLicense() {
		return datasetLicense;
	}
	public void setDatasetLicense(String datasetLicense) {
		this.datasetLicense = datasetLicense;
	}
	public String getDatasetURI() {
		return datasetURI;
	}
	public String getSparqlEndPoint() {
		return sparqlEndPoint;
	}

	public void setSparqlEndPoint(String sparqlEndPoint) {
		this.sparqlEndPoint = sparqlEndPoint;
	}

	public boolean isDatasetSubset() {
		return datasetSubset;
	}

	public void setDatasetSubset(boolean datasetSubset) {
		this.datasetSubset = datasetSubset;
	}
	
	
}
