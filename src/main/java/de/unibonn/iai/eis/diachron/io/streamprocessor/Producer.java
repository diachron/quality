/**
 * 
 */
package de.unibonn.iai.eis.diachron.io.streamprocessor;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;

/**
 * This class is in charge to create the process that call the service and put it to be read it
 * @author Carlos Montoya
 */
public class Producer extends Thread {
	/**
	 * This variable is use to keep the manager of the resources
	 */
	private StreamManager streamManager;
	/**
	 * The value of the increment
	 */
	private int number;
	/**
	 * The url of the service
	 */
	private String serviceUrl;
	
	private boolean running;

	/**
	 * Creator of the class
	 * @param streamManager, this class is the stream Manager to put all the values obtain
	 * @param number, the value of the increment
	 * @param serviceUrl, the url of the service
	 */
	public Producer(StreamManager streamManager, int number, String serviceUrl) {
		this.streamManager = streamManager;
		this.number = number;
		this.serviceUrl = serviceUrl;
	}

	/**
	 * This method start the thread to run the producer
	 */
	@SuppressWarnings("deprecation")
	public void run() {
		this.setRunning(true);
		int iterationNumber = 0;
		while (true && iterationNumber < 14) {
		//while (true) {
			System.out.println("*************        ITERATION NUMBER: " + iterationNumber + " TRIPLES: " +  iterationNumber*number + "*******************");
			
			//Query to load all the information
			String query = "SELECT DISTINCT*"
					+ "{ ?s ?p ?o }"
					+ " LIMIT "+ number 
					+ " OFFSET "+ number*iterationNumber;
			iterationNumber++;
			QueryExecution qe = QueryExecutionFactory.sparqlService(this.serviceUrl,query);
			try {
				int counter = 0;
				ResultSet rs = qe.execSelect();
				while (rs.hasNext()) {
					counter++; //Increase the value of the counter that is used to finish the cicle
					streamManager.put(rs.next()); //This instruction put the value to be consume by the consumer
				}
				if(counter == 0)//The next result not return any result then it finish
					break;
				else
					System.out.println("Process the triples: " + this.number);
			} catch (QueryExceptionHTTP e) {
				System.out.println(this.serviceUrl + " is Not working or is DOWN");
				this.stop();
				break; //Close the cicle
			} 		
		}
		this.setRunning(false);
		//this.stop();
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

	/**
	 * @return the running
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * @param running the running to set
	 */
	public void setRunning(boolean running) {
		this.running = running;
	}

}
