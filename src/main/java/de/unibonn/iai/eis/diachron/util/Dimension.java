/**
 * 
 */
package de.unibonn.iai.eis.diachron.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vm72i6p
 *
 */
public class Dimension {

	private String name;
	private List<Metrics> metrics;
	
	public Dimension(){
		this.metrics = new ArrayList<Metrics>();
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the metrics
	 */
	public List<Metrics> getMetrics() {
		return metrics;
	}
	/**
	 * @param metrics the metrics to set
	 */
	public void setMetrics(List<Metrics> metrics) {
		this.metrics = metrics;
	}
	
}
