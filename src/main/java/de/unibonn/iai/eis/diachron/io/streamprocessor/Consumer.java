package de.unibonn.iai.eis.diachron.io.streamprocessor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.io.utilities.DataSetResults;
import de.unibonn.iai.eis.diachron.io.utilities.UtilMail;
import de.unibonn.iai.eis.diachron.datatypes.Object2Quad;
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
	}

	/**
	 * This method start the thread to run the consumer
	 */
	public void run() {
		Quad value;
		int contAux = 0;
		this.setRunning(true);
		// Run the consumer while the producer is publishing data
		while (producer.isRunning()) {
			value = new Object2Quad(streamManager.get()).getStatement();
			// Here we compute all the metrics
			// Verifiability Metrics
			this.streamManager.digiMetric.compute(value);
			this.streamManager.authMetric.compute(value);
			
			// Believability Metrics
			this.streamManager.idenMetric.compute(value);
			this.streamManager.provMetric.compute(value);
			this.streamManager.trusMetric.compute(value);
			
			// Relevancy Metrics
			this.streamManager.coveMetric.compute(value);
			this.streamManager.releMetric.compute(value);
			
			// Reputation Metrics
			this.streamManager.repuMetric.compute(value);
			
			setCont(getCont() + 1);
			contAux++;
			if (contAux == 10000) {
				contAux = 0;
				System.out.println("Read 10000 triples");
			}
		}
		this.writeFile();
		// System.out.println("The number of quads read is: " + cont);
		// this.stop();
		this.setRunning(false);
		// this.writeFile();
	}

	/**
	 * 
	 */
	public void writeFile() {
		Consumer.setResults(new ArrayList<DataSetResults>());
		DataSetResults result = new DataSetResults();
		result.authMetric = this.streamManager.authMetric;
		result.coveMetric = this.streamManager.coveMetric;
		result.digiMetric = this.streamManager.digiMetric;
		result.idenMetric = this.streamManager.idenMetric;
		result.provMetric = this.streamManager.provMetric;
		result.releMetric = this.streamManager.releMetric;
		result.trusMetric = this.streamManager.trusMetric;
		result.repuMetric = this.streamManager.repuMetric;
		
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
			
			ResultsHelper.write(resultToWrite, conf.loadDataBase(ConfigurationLoader.CONFIGURATION_FILE));
			String text = "The process is already finish, you can check now in the web site of the DIACHRON INTERFACE";
			if (this.getMail() != null)
				UtilMail.sendMail(this.getMail(),"Proccess Finish", text);
			else 
				UtilMail.sendMail(conf.loadMailDefault(ConfigurationLoader.CONFIGURATION_FILE),"Proccess Finish", text);

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
