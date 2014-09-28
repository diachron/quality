package de.unibonn.iai.eis.diachron.io.streamprocessor;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;

import de.unibonn.iai.eis.diachron.datatypes.Object2Quad;
import de.unibonn.iai.eis.diachron.qualitymetrics.contextual.relevancy.Coverage;
import de.unibonn.iai.eis.diachron.qualitymetrics.contextual.relevancy.RelevantTermsWithinMetaInformation;
import de.unibonn.iai.eis.diachron.qualitymetrics.trust.believability.BlackListing;
import de.unibonn.iai.eis.diachron.qualitymetrics.trust.believability.IdentityInformationProvider;
import de.unibonn.iai.eis.diachron.qualitymetrics.trust.believability.ProvenanceInformation;
import de.unibonn.iai.eis.diachron.qualitymetrics.trust.believability.TrustworthinessRDFStatement;
import de.unibonn.iai.eis.diachron.qualitymetrics.trust.reputation.Reputation;
import de.unibonn.iai.eis.diachron.qualitymetrics.trust.verifiability.AuthenticityDataset;
import de.unibonn.iai.eis.diachron.qualitymetrics.trust.verifiability.DigitalSignatures;

public class SequentialStreaming {

	public  DigitalSignatures digiMetric = new DigitalSignatures(); //Metrics to be apply
	public  AuthenticityDataset authMetric = new AuthenticityDataset(); //Metrics to be apply
	public  IdentityInformationProvider idenMetric = new IdentityInformationProvider();
	public  ProvenanceInformation provMetric = new ProvenanceInformation();
	public  TrustworthinessRDFStatement trusMetric = new TrustworthinessRDFStatement();
	public  Coverage coveMetric = new Coverage();
	public  RelevantTermsWithinMetaInformation releMetric = new RelevantTermsWithinMetaInformation();
	public  Reputation repuMetric = new Reputation();
	public  BlackListing blacMetric = new BlackListing();

	private String serviceUrl;
	
	public SequentialStreaming(){
		this.repuMetric.setUriDataset(this.serviceUrl);
		this.trusMetric.setUriDataset(this.serviceUrl);
	}
	
	/**
	 * @param args
	 */
	public void run() {

		int iterationNumber = 0;
		while (true && iterationNumber < 100) {
			//while (true) {
				System.out.println("*************        ITERATION NUMBER: " + iterationNumber + " TRIPLES: " +  iterationNumber*10000 + "*******************");
				
				//Query to load all the information
				String query = "SELECT DISTINCT*"
						+ "{ ?s ?p ?o }"
						+ " LIMIT "+ 10000 
						+ " OFFSET "+ 10000*iterationNumber;
				iterationNumber++;
				QueryExecution qe = QueryExecutionFactory.sparqlService(serviceUrl,query);
				try {
					int counter = 0;
					ResultSet rs = qe.execSelect();
					while (rs.hasNext()) {
						counter++; //Increase the value of the counter that is used to finish the cicle

						Quad value = new Object2Quad(rs.next()).getStatement();
						
						digiMetric.compute(value);
						authMetric.compute(value);
						
						// Believability Metrics
						idenMetric.compute(value);
						provMetric.compute(value);
						trusMetric.compute(value);
						blacMetric.compute(value);
						
						// Relevancy Metrics
						coveMetric.compute(value);
						releMetric.compute(value);
						
						// Reputation Metrics
						repuMetric.compute(value);
						
					}
					if(counter == 0)//The next result not return any result then it finish
						break;
					else
						System.out.println("Process the triples: " + 10000);
				} catch (QueryExceptionHTTP e) {
					System.out.println(serviceUrl + " is Not working or is DOWN");
					break; //Close the cicle
				} 		
			}

	}

	/**
	 * @return the serviceUrl
	 */
	public String getServiceUrl() {
		return serviceUrl;
	}

	/**
	 * @param serviceUrl the serviceUrl to set
	 */
	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

}
