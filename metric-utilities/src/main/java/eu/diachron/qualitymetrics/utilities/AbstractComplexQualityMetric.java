/**
 * 
 */
package eu.diachron.qualitymetrics.utilities;


import de.unibonn.iai.eis.luzzu.assessment.ComplexQualityMetric;

/**
 * @author Jeremy Debattista
 * 
 */
public abstract class AbstractComplexQualityMetric implements ComplexQualityMetric {

	private String datasetURI = "";
	
	@Override
	public String getDatasetURI() {
		return this.datasetURI;
	}

	@Override
	public void setDatasetURI(String datasetURI) {
		this.datasetURI = datasetURI;
	}

}
