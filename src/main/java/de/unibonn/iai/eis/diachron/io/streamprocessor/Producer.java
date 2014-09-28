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
		this.streamManager.setUriDataset(serviceUrl);
		this.number = number;
		this.serviceUrl = serviceUrl;
	}

	/**
	 * This method start the thread to run the producer
	 */
	public void run() {
		this.setRunning(true);
		int iterationNumber = 0;
		streamManager.setCounter(0);
		while (true) {
			System.out.println("*************        ITERATION NUMBER: " + iterationNumber + " TRIPLES: " +  iterationNumber*number + "*******************");
			
			//Query to load all the information
			String query = "SELECT DISTINCT*"
					+ "{ ?s ?p ?o }"
					//+ " ORDER BY ?s" //If this order is made, then the SPARQL endpoint take to much time to respond.
					+ " LIMIT "+ number 
					+ " OFFSET "+ number*iterationNumber;
			iterationNumber++;
			QueryExecution qe = QueryExecutionFactory.sparqlService(this.serviceUrl,query);
			try {
				ResultSet rs = qe.execSelect();
			
				if(rs != null && rs.hasNext()){
					streamManager.put(rs);	//Put all the batch of answer into the streaming manager
					streamManager.setCounter(streamManager.getCounter()+1); // Announced that the producer publish a result set
					System.out.println("Process the triples: " + this.number);
				}
				else
					break;
			} catch (QueryExceptionHTTP e) {
				System.out.println(this.serviceUrl + " is Not working or is DOWN");
				break; //Close the cicle
			} 		
		}
		this.setRunning(false);
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
