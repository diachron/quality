/**
 * 
 */
package de.unibonn.iai.eis.diachron.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Carlos
 *
 */
public class ResultDataSet {

	private Date lastDate;
	private List<Results> results;
	
	
	public ResultDataSet(){
		this.results = new ArrayList<Results>();
	}
	/**
	 * @return the lastDate
	 */
	public Date getLastDate() {
		return lastDate;
	}
	/**
	 * @param lastDate the lastDate to set
	 */
	public void setLastDate(Date lastDate) {
		this.lastDate = lastDate;
	}
	/**
	 * @return the results
	 */
	public List<Results> getResults() {
		return results;
	}
	/**
	 * @param results the results to set
	 */
	public void setResults(List<Results> results) {
		this.results = results;
	}
	
}
