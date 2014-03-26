package de.unibonn.iai.eis.diachron.qualitymetrics.utilities;

import java.util.HashMap;
import java.util.Map.Entry;

import com.hp.hpl.jena.graph.Node;

/**
 * @author Santiago Londono
 * Represents a subject (i.e. a resource), that is described by a set of properties. Furthermore, provides 
 * the functionality necessary to determine the equivalence of subjects according to their properties. 
 * - Notice: this class is not thread safe (since HashMaps are not synchronized).
 */
public class ComparableSubject {
	
	/**
	 * URI identifying the subject, serves as its id.
	 */
	private String uri;
	
	/**
	 * Map containing the properties that describe this subject. Maps allow properties to be 
	 * retrieved by URI efficiently, which is quite convenient when comparing the sets of 
	 * properties of subjects. HashMap is used, since instances of this class ought not to be thread safe.
	 */
	private HashMap<String, String> mapProperties;
	
	/**
	 * Creates a new instance of a subject, identified by the provided URI and with
	 * an empty set of properties.
	 * @param uri Id of the subject
	 */
	public ComparableSubject(String uri) {
		this.uri = uri;
		mapProperties = new HashMap<String, String>();
	}
	
	/**	
	 * Adds or updates a property describing this subject.
	 * @param predicateUri URI of the predicate corresponding to the property to be added
	 * @param objectValue Value of the property to be added
	 */
	public void addProperty(String predicateUri, Node objectValue) {
		// To keep the memory footprint low, store in the properties hash a string representation of the node value. 
		// Node.toString() is suitable, as it "Answers a human-readable representation of the Node" (see javadoc for Node class)
		String objectValString = "";
		if(objectValue != null) {
			objectValString = objectValue.toString();
		}
		
		mapProperties.put(predicateUri, objectValString);		
	}
	
	/**
	 * Determines if this subject is equivalent to the one provided as parameter. 
	 * Two subjects are equivalent if they have the same set of properties, 
	 * all with the same values (but not necessarily the same ids).
	 * @param subject Subject this instance will be compared with
	 * @return true if the parameter is not null and if both subjects are equivalent, false otherwise.
	 */
	public boolean isEquivalentTo(ComparableSubject subject) {
		boolean result = false;
		
		if(subject != null) {
			
			int countPropertiesS2inThis = 0;		// Counts how many of the properties in subject are in this and have the same value in both resources

			// For each property in subject, check if it is contained in this and if it has the same value here
			for(Entry<String, String> curProperty : subject.mapProperties.entrySet()) {
				String curEquivProperty = this.mapProperties.get(curProperty.getKey());
				
				// Compare the properties values using their string representation
				if(curEquivProperty != null && curEquivProperty.equals(curProperty.getValue())) {
					countPropertiesS2inThis++;
				}
			}
			
			// Consider both resources equivalent if they have the same number of properties and all properties in r2
			// exist in r1 and have the same value in both resources
			if((countPropertiesS2inThis == subject.mapProperties.size()) && (this.mapProperties.size() == countPropertiesS2inThis)) {
				result = true;
			}	
		}
		
		return result;
	}

	/**
	 * URI identifying the subject, serves as its id.
	 * @return
	 */
	public String getUri() {
		return uri;
	}

}
