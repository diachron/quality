package de.unibonn.iai.eis.diachron.io.streamprocessor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.io.utilities.DataSetResults;
import de.unibonn.iai.eis.diachron.io.utilities.UtilMail;
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
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.ConfigurationLoader;
import de.unibonn.iai.eis.diachron.util.Dimension;
import de.unibonn.iai.eis.diachron.util.Metrics;
import de.unibonn.iai.eis.diachron.util.ResultDataSet;
import de.unibonn.iai.eis.diachron.util.Results;
import de.unibonn.iai.eis.diachron.util.ResultsHelper;

/**
 * This class read all the values produce by the producer an execute the metrics
 * over the quad obtained
 * 
 * @author Carlos
 */
public class Consumer extends Thread {
	/**
	 * 
	 */
	private StreamManager streamManager;
	private Producer producer;
	private int cont = 0;
	private boolean running;
	private static List<DataSetResults> results;
	private String mail;

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
	
	/**
	 * Creator of the class
	 * 
	 * @param streamManager
	 *            , this class is the stream Manager to put all the values
	 *            obtain
	 * @param producer
	 *            , producer of the events to be read
	 */
	public Consumer(StreamManager streamManager, Producer producer) {
		this.streamManager = streamManager;
		this.producer = producer;

		this.repuMetric.setUriDataset(this.producer.getServiceUrl());
		this.trusMetric.setUriDataset(this.producer.getServiceUrl());
	}

	/**
	 * This method start the thread to run the consumer
	 */
	public void run() {
		Quad value;
		int contAux = 0;
		this.setRunning(true);
		// Run the consumer while the producer is publishing data or while the streaming has some information in it
		while (producer.isAlive() || streamManager.getCounter()>0) {
			ResultSet aux = streamManager.get(); //Retrieves the resulset that was published
			if(aux.hasNext()){
				while (aux.hasNext()) {
					value = new Object2Quad(aux.next()).getStatement();
					// Here we compute all the metrics
					// Verifiability Metrics
					this.digiMetric.compute(value);
					this.authMetric.compute(value);
					
					// Believability Metrics
					this.idenMetric.compute(value);
					this.provMetric.compute(value);
					this.trusMetric.compute(value);
					this.blacMetric.compute(value);
					
					// Relevancy Metrics
					this.coveMetric.compute(value);
					this.releMetric.compute(value);
					
					// Reputation Metrics
					this.repuMetric.compute(value);
					
					this.cont++;
					contAux++;
					if (contAux == 10000) { //Message to control the information
						contAux = 0;
						System.out.println("Read 10000 triples");
					}
					if(this.cont % 100000 == 0){
						this.writeFile(false);//When the dataset is to big it is better to store the information every 100000 triples processed, sometimes the service is shotdown and then the info is lost
					}
				}	
				streamManager.setCounter(streamManager.getCounter()-1);//Announced that it consumer the resource
			}			
		}
		this.writeFile(true);
		System.out.println("The number of quads read is: " + cont);
	}

	/**
	 * This method writes all the information related with the processing of the metrics into a local file
	 */
	public void writeFile(boolean sendMessage) {
		Consumer.setResults(new ArrayList<DataSetResults>());
		DataSetResults result = new DataSetResults();
		result.authMetric = this.authMetric;
		result.coveMetric = this.coveMetric;
		result.digiMetric = this.digiMetric;
		result.idenMetric = this.idenMetric;
		result.provMetric = this.provMetric;
		result.releMetric = this.releMetric;
		result.trusMetric = this.trusMetric;
		result.repuMetric = this.repuMetric;
		result.blacMetric = this.blacMetric;
		
		getResults().add(result);
		
		Dimension dimension1 = new Dimension();
		dimension1.setName("Verifiability");
		dimension1.getMetrics().add(new Metrics("Authenticity of the Dataset", Double.toString(result.authMetric.metricValue())));
		dimension1.getMetrics().add(new Metrics("Digital Signatures", Double.toString(result.digiMetric.metricValue())));

		Dimension dimension2 = new Dimension();
		dimension2.setName("Relevance");
		dimension2.getMetrics().add(new Metrics("Coverage", Double.toString(result.coveMetric.metricValue())));
		dimension2.getMetrics().add(new Metrics("Relevants Terms", Double.toString(result.releMetric.metricValue())));

		Dimension dimension3 = new Dimension();
		dimension3.setName("Believability");
		dimension3.getMetrics().add(new Metrics("Identity Information Provider", Double.toString(result.idenMetric.metricValue())));
		dimension3.getMetrics().add(new Metrics("Provenance Information", Double.toString(result.provMetric.metricValue())));
		dimension3.getMetrics().add(new Metrics("Trustworthiness RDF Statement", Double.toString(result.trusMetric.metricValue())));
		dimension3.getMetrics().add(new Metrics("BlackListing", Double.toString(result.blacMetric.metricValue())));
				
		Dimension dimension4 = new Dimension();
		dimension4.setName("Reputation");
		dimension4.getMetrics().add(new Metrics("Reputation", Double.toString(result.repuMetric.metricValue())));
				
		Results results = new Results();
		results.setUrl(this.producer.getServiceUrl());
		results.getDimensions().add(dimension1);
		results.getDimensions().add(dimension2);
		results.getDimensions().add(dimension3);
		results.getDimensions().add(dimension4);
		
		try {
			ConfigurationLoader conf = new ConfigurationLoader();
			ResultDataSet resultToWrite = ResultsHelper.read(conf.loadDataBase(ConfigurationLoader.CONFIGURATION_FILE));

			resultToWrite.setLastDate(new Date());
			boolean modified = false;
			
			List<Results> aux = new ArrayList<Results>();
			for (Results resultAux : resultToWrite.getResults()) {
				if (resultAux.getUrl().equals(this.producer.getServiceUrl())) {
					resultAux = results;
					modified = true;
				}else{
					aux.add(resultAux);
				}
					
			}

			if (!modified){
				resultToWrite.getResults().add(results);
			}else{
				aux.add(results);
				resultToWrite.setResults(aux);
			}
			
			if(sendMessage){			
				ResultsHelper.write(resultToWrite, conf.loadDataBase(ConfigurationLoader.CONFIGURATION_FILE));
				String text = "The process is already finish, you can check now in the web site of the DIACHRON INTERFACE";
				if (this.getMail() != null)
					UtilMail.sendMail(this.getMail(),"Proccess Finish", text);
				else 
					UtilMail.sendMail(conf.loadMailDefault(ConfigurationLoader.CONFIGURATION_FILE),"Proccess Finish", text);
			}

		} catch (Exception e) {
			System.out.println("****** Can't save the result because: "
					+ e.toString());
		}
	}

	/**
	 * @return the running
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * @param running
	 *            the running to set
	 */
	public void setRunning(boolean running) {
		this.running = running;
	}

	/**
	 * @return the mail
	 */
	public String getMail() {
		return mail;
	}

	/**
	 * @param mail
	 *            the mail to set
	 */
	public void setMail(String mail) {
		this.mail = mail;
	}

	/**
	 * @return the results
	 */
	public static List<DataSetResults> getResults() {
		return results;
	}

	/**
	 * @param results
	 *            the results to set
	 */
	public static void setResults(List<DataSetResults> results) {
		Consumer.results = results;
	}

	/**
	 * @return the cont
	 */
	public int getCont() {
		return cont;
	}

	/**
	 * @param cont
	 *            the cont to set
	 */
	public void setCont(int cont) {
		this.cont = cont;
	}
}
