/**
 * 
 */
package de.unibonn.iai.eis.diachron.datatypes;

import java.io.Serializable;


/**
 * @author Jeremy Debattista
 * 
 */
public class Pair<U,V> implements Serializable{
	
	private static final long serialVersionUID = -6713166656254601033L;
			
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
	
	@SuppressWarnings("unchecked")
	@Override
	 public boolean equals(Object obj) {
	    	
		 if (obj instanceof Pair<?,?>){
			 Pair<U,V> other = (Pair<U,V>)obj;
			 return (this.getFirstElement().equals(other.getFirstElement()) && this.getSecondElement().equals(other.getSecondElement()));
		 }
		 
		 return false;
	 }
	 
	 @Override
	 public int hashCode(){
		 return (this.getFirstElement().hashCode() + this.getSecondElement().hashCode());
	 }
	
	

}
