/**
 * 
 */
package de.unibonn.iai.eis.diachron.datatypes;

/**
 * @author Jeremy Debattista
 * 
 */
public class Pair<U,V> {
	
	private U firstElement;
	private V secondElement;
	
	public Pair(U first, V second){
		this.setFirstElement(first);
		this.setSecondElement(second);
	}

	public U getFirstElement() {
		return firstElement;
	}

	public void setFirstElement(U firstElement) {
		this.firstElement = firstElement;
	}

	public V getSecondElement() {
		return secondElement;
	}

	public void setSecondElement(V secondElement) {
		this.secondElement = secondElement;
	}
	
	

}
