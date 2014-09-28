package de.unibonn.iai.eis.diachron.io.streamprocessor;

import com.hp.hpl.jena.query.ResultSet;


/**
 * This class is the one that manage all the Quad and compute all the streaming data
 * @author Carlos Montoya
 */
public class StreamManager {
	private boolean available = false; //This value is use as trafic Light
	public ResultSet object; //Object to be pass between elements
	
	private String uriDataset;
	private int counter;
	
	/**
	 * This class obtain the values published by the producer
	 * @return the value published
	 */
	public synchronized ResultSet get() {
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
	//public synchronized void put(List<QuerySolution> value) {ResultSet
	public synchronized void put(ResultSet value) {
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
	}

	/**
	 * @return the counter
	 */
	public int getCounter() {
		return counter;
	}

	/**
	 * @param counter the counter to set
	 */
	public void setCounter(int counter) {
		this.counter = counter;
	}
}
