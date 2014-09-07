/**
 * 
 */
package de.unibonn.iai.eis.diachron.util;

/**
 * @author vm72i6p
 *
 */
public class Metrics {

	private String name;
	private String value;
	
	/**
	 * 
	 */
	public Metrics(){		
	}
	
	/**
	 * 
	 * @param name
	 * @param value
	 */
	public Metrics(String name, String value){
		this.name = name;
		this.value = value;
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
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
}
