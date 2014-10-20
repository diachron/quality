package eu.diachron.qualitymetrics.accessibility.licensing;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;

/**
 * @author slondono
 * Encapsulates knowledge about how to determine if text representing an URI or text attribute, provides information 
 * about the licensing model of a resource. For example, this class recognizes all the URIs of the predicates that 
 * can be used to specify the license under which a resource is attributed. 
 */
public class LicensingModelClassifier {
	
	/**
	 * Set of all the URIs of properties known to provide licensing information
	 */
	private static HashSet<String> setLicenseProperties;
	
	/**
	 * Set of all the URIs of properties known to provide licensing information
	 */
	private static Pattern[] arrCopyLeftURIPatterns;
	
	static {
		// Initialize set of properties known to provide licensing information
		setLicenseProperties = new HashSet<String>();
		setLicenseProperties.add(DCTerms.license.getURI());
		setLicenseProperties.add(DCTerms.accessRights.getURI());
		setLicenseProperties.add(DCTerms.rights.getURI());
		setLicenseProperties.add(DC.rights.getURI());
		setLicenseProperties.add("http://www.w3.org/1999/xhtml/vocab#license");
		setLicenseProperties.add("http://creativecommons.org/ns#license");
		
		// Initialize set of regex patterns corresponding to CopyLeft license URIs
		arrCopyLeftURIPatterns = new Pattern[6];
		arrCopyLeftURIPatterns[0] = Pattern.compile("^http://creativecommons\\.org/licenses/by-sa/.*");
		arrCopyLeftURIPatterns[1] = Pattern.compile("^http://www\\.opendatacommons\\.org/licenses/odbl.*");
		arrCopyLeftURIPatterns[2] = Pattern.compile("^http://www\\.opendatacommons\\.org/licenses/pddl/.*");
		arrCopyLeftURIPatterns[3] = Pattern.compile("^http://www\\.opendatacommons\\.org/licenses/by/.*");
		arrCopyLeftURIPatterns[4] = Pattern.compile("^http://creativecommons\\.org/publicdomain/zero/.*");
		arrCopyLeftURIPatterns[5] = Pattern.compile("^http://www\\.gnu\\.org/licenses/licenses.html.*");
	}

	/**
	 * Tells whether the predicate provided as parameter is known to provide information about how a resource
	 * is licensed. More specifically, the URI of the predicate is verified to belong to a set of predicate URIs 
	 * recognized as standard means to state the licensing schema of the resource being described.
	 * @param predicate Predicate to be evaluated to correspond to a statement of license
	 * @return true if the predicate is known to state the license of a resource, false otherwise
	 */
	public boolean isLicensingPredicate(Node predicate) {

		if(predicate != null && predicate.isURI()) {
			// Search for the predicate's URI in the set of license properties...
			if(setLicenseProperties.contains(predicate.getURI())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Tells whether the object provided as parameter contains an URI, recognized as a CopyLeft license
	 * @param licenseObj Object of a triple expected to provide information about the license of the described resource
	 * @return true if the license is deemed as CopyLeft, false otherwise
	 */
	public boolean isCopyLeftLicenseURI(Node licenseObj) {
		
		if(licenseObj != null && licenseObj.isURI()) {
		
			String licenseURI = licenseObj.getURI();
			Matcher matcher = null;
			
			// Compare the license URI with all the licenses known to be CopyLeft
			for(Pattern uriPattern : arrCopyLeftURIPatterns) {
				
				matcher = uriPattern.matcher(licenseURI);
				
				if(matcher.matches()) {
					return true;
				}
			}
		}
		return false;
	}

}
