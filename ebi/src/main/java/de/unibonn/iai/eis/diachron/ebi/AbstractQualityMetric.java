/**
 * 
 */
package de.unibonn.iai.eis.diachron.ebi;


import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;

/**
 * @author Jeremy Debattista
 * 
 */
public abstract class AbstractQualityMetric implements QualityMetric {

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
