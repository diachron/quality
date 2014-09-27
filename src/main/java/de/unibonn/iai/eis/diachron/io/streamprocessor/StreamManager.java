package de.unibonn.iai.eis.diachron.io.streamprocessor;

import com.hp.hpl.jena.query.QuerySolution;

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
 * This class is the one that manage all the Quad and compute all the streaming data
 * @author Carlos Montoya
 */
public class StreamManager {
	private boolean available = false; //This value is use as trafic Light
	public QuerySolution object; //Object to be pass between elements
	
	
	/**
	 * Metrics developed
	 */
	public DigitalSignatures digiMetric = new DigitalSignatures(); //Metrics to be apply
	public AuthenticityDataset authMetric = new AuthenticityDataset(); //Metrics to be apply
	public IdentityInformationProvider idenMetric = new IdentityInformationProvider();
	public ProvenanceInformation provMetric = new ProvenanceInformation();
	public TrustworthinessRDFStatement trusMetric = new TrustworthinessRDFStatement();
	public Coverage coveMetric = new Coverage();
	public RelevantTermsWithinMetaInformation releMetric = new RelevantTermsWithinMetaInformation();
	public Reputation repuMetric = new Reputation();
	public BlackListing blacMetric = new BlackListing();
	
	private String uriDataset;
	
	/**
	 * This class obtain the values published by the producer
	 * @return the value published
	 */
	public synchronized QuerySolution get() {
		while (available == false) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		available = false;
		notifyAll();
		return object;
	}

	/**
	 * Method that is use to publish the information
	 * @param value
	 */
	public synchronized void put(QuerySolution value) {
		while (available == true) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		object = value;
		available = true;
		notifyAll();
	}

	/**
	 * @return the uriDataset
	 */
	public String getUriDataset() {
		return uriDataset;
	}

	/**
	 * @param uriDataset the uriDataset to set
	 */
	public void setUriDataset(String uriDataset) {
		this.uriDataset = uriDataset;
		this.repuMetric.setUriDataset(this.uriDataset);
		this.trusMetric.setUriDataset(this.uriDataset);
	}
}
