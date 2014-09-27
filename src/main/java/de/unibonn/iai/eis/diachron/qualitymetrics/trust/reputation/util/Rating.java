/**
 * 
 */
package de.unibonn.iai.eis.diachron.qualitymetrics.trust.reputation.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Carlos Montoya
 * Util class to help the Reputation Metric to navigate through the ratings given by the users.
 */
public class Rating {

	private String evaluator;
	private List<String> recomendedDataSets;
	
	/**
	 * 
	 */
	public Rating(){
		this.recomendedDataSets = new ArrayList<String>();
	}
	
	/**
	 * @return the evaluator
	 */
	public String getEvaluator() {
		return evaluator;
	}
	/**
	 * @param evaluator the evaluator to set
	 */
	public void setEvaluator(String evaluator) {
		this.evaluator = evaluator;
	}
	/**
	 * @return the recomendedDataSets
	 */
	public List<String> getRecomendedDataSets() {
		return recomendedDataSets;
	}
	/**
	 * @param recomendedDataSets the recomendedDataSets to set
	 */
	public void setRecomendedDataSets(List<String> recomendedDataSets) {
		this.recomendedDataSets = recomendedDataSets;
	}
	
	
}
