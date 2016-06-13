package eu.diachron.qualitymetrics.accessibility.licensing;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
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
	
	/**
	 * Set of all the URIs of properties known to provide licensing information but not recommended to use
	 * according to the voID vocabulary
	 */
	private static Pattern[] arrNotRecommendedCopyLeftURIPatterns;
	
	/**
	 * Regular expressions represeting the patterns of the text deemed to be a licensing statement
	 */
	private static Pattern[] arrLicenseTextPatterns;
	
	static {
		// Initialize set of properties known to provide licensing information
		// For licencing properties we use the 10 top properties identified by Hogan et al. in An Empirical Survey of Linked Data conformance
		setLicenseProperties = new HashSet<String>();
		setLicenseProperties.add(DCTerms.license.getURI()); //dct:license
		setLicenseProperties.add(DCTerms.rights.getURI()); //dct:rights
		setLicenseProperties.add(DC.rights.getURI()); //dc:rights
		setLicenseProperties.add("http://www.w3.org/1999/xhtml/vocab#license"); //xhtml:license
		setLicenseProperties.add("http://creativecommons.org/ns#license"); //cc:license
		setLicenseProperties.add("http://purl.org/dc/elements/1.1/licence"); //dc:licence
		setLicenseProperties.add("http://dbpedia.org/ontology/licence"); //dbo:licence
		setLicenseProperties.add("http://dbpedia.org/property/licence"); //dbp:licence
		setLicenseProperties.add("http://usefulinc.com/ns/doap#license"); //doap:license
		setLicenseProperties.add("https://schema.org/license"); //schema:license

		

		// Initialize set of regex patterns corresponding to CopyLeft license URIs
		arrCopyLeftURIPatterns = new Pattern[7];
		arrCopyLeftURIPatterns[0] = Pattern.compile("^http://www\\.opendatacommons\\.org/licenses/odbl.*", Pattern.CASE_INSENSITIVE);
		arrCopyLeftURIPatterns[1] = Pattern.compile("^http://www\\.opendatacommons\\.org/licenses/pddl/.*", Pattern.CASE_INSENSITIVE);
		arrCopyLeftURIPatterns[2] = Pattern.compile("^http://www\\.opendatacommons\\.org/licenses/by/.*", Pattern.CASE_INSENSITIVE);
		arrCopyLeftURIPatterns[3] = Pattern.compile("^http://creativecommons\\.org/publicdomain/zero/.*", Pattern.CASE_INSENSITIVE);
		arrCopyLeftURIPatterns[4] = Pattern.compile("^http://creativecommons\\.org/licenses/by/.*", Pattern.CASE_INSENSITIVE);
		arrCopyLeftURIPatterns[5] = Pattern.compile("^http://purl\\.org/NET/rdflicense/.*", Pattern.CASE_INSENSITIVE);
		arrCopyLeftURIPatterns[6] = Pattern.compile("^http://www\\.gnu\\.org/licenses/.*", Pattern.CASE_INSENSITIVE);

		arrNotRecommendedCopyLeftURIPatterns = new Pattern[3];
		arrNotRecommendedCopyLeftURIPatterns[0] = Pattern.compile("^http://creativecommons\\.org/licenses/by-sa/.*", Pattern.CASE_INSENSITIVE);
		arrNotRecommendedCopyLeftURIPatterns[1] = Pattern.compile("^http://www\\.gnu\\.org/copyleft/.*", Pattern.CASE_INSENSITIVE);
		arrNotRecommendedCopyLeftURIPatterns[2] = Pattern.compile("^http://creativecommons\\.org/licenses/by-nc/.*", Pattern.CASE_INSENSITIVE);

		
		// Initialize the licensing text pattern
		arrLicenseTextPatterns = new Pattern[1];
		arrLicenseTextPatterns[0] = Pattern.compile(".*(licensed?|copyrighte?d?).*(under|grante?d?|rights?).*", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
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
			// Compare the license URI with all the licenses known to be CopyLeft
			return matchesAnyPattern(licenseObj.getURI(), arrCopyLeftURIPatterns);
		}
		return false;
	}
	
	/**
	 * Tells whether the object provided as parameter contains an URI, recognized as a CopyLeft license that is not recommended
	 * @param licenseObj Object of a triple expected to provide information about the license of the described resource
	 * @return true if the license is deemed as CopyLeft, false otherwise
	 */
	public boolean isNotRecommendedCopyLeftLicenseURI(Node licenseObj) {
		
		if(licenseObj != null && licenseObj.isURI()) {
			// Compare the license URI with all the licenses known to be CopyLeft
			return matchesAnyPattern(licenseObj.getURI(), arrNotRecommendedCopyLeftURIPatterns);
		}
		return false;
	}
	
	
	/**
	 * Evaluates the text contained into the literal to determine whether it contains a licensing statement.
	 * @param licenseLiteralObj Text literal corresponding to the object of a triple
	 * @return true if the literal contains text considered to be of a license statement, false otherwise
	 */
	public boolean isLicenseStatement(Node licenseLiteralObj) {
		
		if(licenseLiteralObj != null && licenseLiteralObj.isLiteral()) {
			// Check whether the contents of the object match any of the license patterns
			return matchesAnyPattern(licenseLiteralObj.toString(), arrLicenseTextPatterns);
		}
		return false;
	}
	
	/**
	 * Evaluates the text contained into the literal to determine whether it contains a licensing statement
	 * that is not recommended.
	 * @param licenseLiteralObj Text literal corresponding to the object of a triple
	 * @return true if the literal contains text considered to be of a license statement, false otherwise
	 */
	public boolean isNotRecommendedLicenseStatement(Node licenseLiteralObj) {
		
		if(licenseLiteralObj != null && licenseLiteralObj.isLiteral()) {
			// Check whether the contents of the object match any of the license patterns
			return matchesAnyPattern(licenseLiteralObj.toString(), arrNotRecommendedCopyLeftURIPatterns);
		}
		return false;
	}
	
	/**
	 * Matches the text against all the patterns provided in the second argument, 
	 * to determine if the text matches any of them.
	 * @param text Text to be matched 
	 * @return true if the text matches any pattern in arrPatterns, false otherwise
	 */
	private boolean matchesAnyPattern(String text, Pattern[] arrPatterns) {
		
		Matcher matcher = null;
		
		for(Pattern pattern : arrPatterns) {
			
			matcher = pattern.matcher(text);
			
			if(matcher.matches()) {
				return true;
			}

		}
		return false;
	}
	
	// TODO: Remove main method introduced for testing purposes
	public static void main(String[] args) {
		String[] arrTestStrs = new String[8];
		arrTestStrs[0] = "Subject to the terms and conditions of this Public License, the Licensor hereby grants You a worldwide, royalty-free, non-sublicensable, non-exclusive, irrevocable license to exercise the Licensed Rights in the Licensed Material";
		arrTestStrs[1] = "Moral rights, such as the right of integrity, are not licensed under this Public License, nor are publicity, privacy, and/or other similar personality rights";
		arrTestStrs[2] = "All rights granted under this License are granted for the term of copyright on the Program, and are irrevocable provided the stated conditions are met. This License explicitly affirms your unlimited permission to run the unmodified Program.";
		arrTestStrs[3] = "RDF data extracted from Wikipedia";
		arrTestStrs[4] = "To the extent possible under law, The Example Organisation has waived all copyright and related or neighboring rights to The Example Dataset.";
		arrTestStrs[5] = "Creative Commons Attribution-Share Alike 3.0 Unported License";
		arrTestStrs[6] = "### About Data exposed: a large life sciences data set about proteins and their function. ### Openness Not open. [Copyright page](ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/rdf/README) states: Copyright 2007-2012 UniProt Consortium. We have chosen to apply the Creative Commons Attribution-NoDerivs License (http://creativecommons.org/licenses/by-nd/3.0/) to all copyrightable parts (http://sciencecommons.org/) of our databases. This means that you are free to copy, distribute, display and make commercial use of these databases, provided you give us credit. However, if you intend to distribute a modified version of one of our databases, you must ask us for permission first. All databases and documents in the UniProt FTP directory may be copied and redistributed freely, without advance permission, provided that this copyright statement is reproduced with each copy.";
		arrTestStrs[7] = "copyleft, copyright, free software, intellectual property, license, open source, software piracy";
		
		LicensingModelClassifier classif = new LicensingModelClassifier();
		
		for(String licenseText : arrTestStrs) {
			Node curNode = NodeFactory.createLiteral(licenseText);
			System.out.println("String " + licenseText.substring(0, 10) + " is license: " + classif.isLicenseStatement(curNode));
		}
	}

}
