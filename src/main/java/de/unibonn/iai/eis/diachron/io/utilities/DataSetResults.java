/**
 * 
 */
package de.unibonn.iai.eis.diachron.io.utilities;

import de.unibonn.iai.eis.diachron.qualitymetrics.contextual.relevancy.Coverage;
import de.unibonn.iai.eis.diachron.qualitymetrics.contextual.relevancy.RelevantTermsWithinMetaInformation;
import de.unibonn.iai.eis.diachron.qualitymetrics.trust.believability.BlackListing;
import de.unibonn.iai.eis.diachron.qualitymetrics.trust.believability.IdentityInformationProvider;
import de.unibonn.iai.eis.diachron.qualitymetrics.trust.believability.ProvenanceInformation;
import de.unibonn.iai.eis.diachron.qualitymetrics.trust.believability.TrustworthinessRDFStatement;
import de.unibonn.iai.eis.diachron.qualitymetrics.trust.reputation.Reputation;
import de.unibonn.iai.eis.diachron.qualitymetrics.trust.verifiability.AuthenticityDataset;
import de.unibonn.iai.eis.diachron.qualitymetrics.trust.verifiability.DigitalSignatures;

/**
 * @author Carlos
 *
 */
public class DataSetResults {

	private String url;
	//Trust
	public DigitalSignatures digiMetric; //Metrics to be apply
	public AuthenticityDataset authMetric; //Metrics to be apply
	public IdentityInformationProvider idenMetric;
	public ProvenanceInformation provMetric;
	public TrustworthinessRDFStatement trusMetric;
	public BlackListing blacMetric;
	
	//Relevance
	public Coverage coveMetric;
	public RelevantTermsWithinMetaInformation releMetric;
	
	//Reputation
	public Reputation repuMetric;
	
	/**
	 * Empty creator
	 */
	public DataSetResults(){
		
	}
	
	/**
	 * Creation method
	 * @param url
	 * @param digMetric
	 * @param autMetric
	 * @param freeMetric
	 */
	public DataSetResults(String url, DigitalSignatures digMetric, AuthenticityDataset autMetric){
		this.url = url;
		this.digiMetric = digMetric;
		this.authMetric = autMetric;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the digiMetric
	 */
	public DigitalSignatures getDigiMetric() {
		return digiMetric;
	}

	/**
	 * @param digiMetric the digiMetric to set
	 */
	public void setDigiMetric(DigitalSignatures digiMetric) {
		this.digiMetric = digiMetric;
	}

	/**
	 * @return the authMetric
	 */
	public AuthenticityDataset getAuthMetric() {
		return authMetric;
	}

	/**
	 * @param authMetric the authMetric to set
	 */
	public void setAuthMetric(AuthenticityDataset authMetric) {
		this.authMetric = authMetric;
	}

	/**
	 * @return the idenMetric
	 */
	public IdentityInformationProvider getIdenMetric() {
		return idenMetric;
	}

	/**
	 * @param idenMetric the idenMetric to set
	 */
	public void setIdenMetric(IdentityInformationProvider idenMetric) {
		this.idenMetric = idenMetric;
	}

	/**
	 * @return the provMetric
	 */
	public ProvenanceInformation getProvMetric() {
		return provMetric;
	}

	/**
	 * @param provMetric the provMetric to set
	 */
	public void setProvMetric(ProvenanceInformation provMetric) {
		this.provMetric = provMetric;
	}

	/**
	 * @return the trusMetric
	 */
	public TrustworthinessRDFStatement getTrusMetric() {
		return trusMetric;
	}

	/**
	 * @param trusMetric the trusMetric to set
	 */
	public void setTrusMetric(TrustworthinessRDFStatement trusMetric) {
		this.trusMetric = trusMetric;
	}

	/**
	 * @return the coveMetric
	 */
	public Coverage getCoveMetric() {
		return coveMetric;
	}

	/**
	 * @param coveMetric the coveMetric to set
	 */
	public void setCoveMetric(Coverage coveMetric) {
		this.coveMetric = coveMetric;
	}

	/**
	 * @return the releMetric
	 */
	public RelevantTermsWithinMetaInformation getReleMetric() {
		return releMetric;
	}

	/**
	 * @param releMetric the releMetric to set
	 */
	public void setReleMetric(RelevantTermsWithinMetaInformation releMetric) {
		this.releMetric = releMetric;
	}

	/**
	 * @return the repuMetric
	 */
	public Reputation getRepuMetric() {
		return repuMetric;
	}

	/**
	 * @param repuMetric the repuMetric to set
	 */
	public void setRepuMetric(Reputation repuMetric) {
		this.repuMetric = repuMetric;
	}

}
