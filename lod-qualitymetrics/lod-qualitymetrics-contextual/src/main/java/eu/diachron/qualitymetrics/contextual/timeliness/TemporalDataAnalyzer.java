package eu.diachron.qualitymetrics.contextual.timeliness;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.XSD;

import de.unibonn.iai.eis.diachron.semantics.knownvocabs.PROV;


/**
 * @author slondono
 * Provides functionalities to determine whether a property corresponds to a particular form of temporal data, such as
 * the publishing or last-modified time of a resource and to parse such values in a best-effort basis, to convert them to 
 * java.util.Date instances
 */
public class TemporalDataAnalyzer {
	
	private static Logger logger = Logger.getLogger(TemporalDataAnalyzer.class);
	
	/**
	 * Map containing the set of properties known to provide the last-modified time of a resource
	 */
	private static HashMap<String, DateFormatters> mapLastUpdateTimeProps;
	
	/**
	 * Map containing the set of properties known to provide the publishing time of a resource
	 */
	private static HashMap<String, DateFormatters> mapPublishingTimeProps;
	
	/**
	 * Map containing the set of properties known to provide the creation time of a resource
	 */
	private static HashMap<String, DateFormatters> mapCreationTimeProps;
	
	
	/**
	 * Map containing the set of properties known to provide the valid time of a resource
	 */
	private static HashMap<String, DateFormatters> mapValidTimeProps;
	
	/**
	 * Singleton used to parse literals of type xsd:dateTime
	 */
	private static RDFDatatype rdfDate = NodeFactory.getType(XSD.date.getURI());
	
	/**
	 * Singleton used to parse literals of type xsd:dateTime
	 */
	private static RDFDatatype rdfDateTime = NodeFactory.getType(XSD.dateTime.getURI());
	
	static {
		// Fill the maps used to discover the properties providing the publishing and last-modified times of resources.
		// The key of the map corresponds to the URI of the property and its value provides information about the format of the objects
		mapLastUpdateTimeProps = new HashMap<String, DateFormatters>();
		mapPublishingTimeProps = new HashMap<String, DateFormatters>();
		mapValidTimeProps = new HashMap<String, DateFormatters>();
		mapCreationTimeProps = new HashMap<String, DateFormatters>();

		
		mapLastUpdateTimeProps.put(DCTerms.modified.getURI(), DateFormatters.XSD);		
//		mapLastUpdateTimeProps.put("http://purl.org/dc/terms/#modified", DateFormatters.INTL_LONG);
		mapLastUpdateTimeProps.put("http://semantic-mediawiki.org/swivt/1.0#wikiPageModificationDate", DateFormatters.INTL_LONG);
		
		mapPublishingTimeProps.put(DCTerms.issued.getURI(), DateFormatters.XSD);
		mapPublishingTimeProps.put(PROV.generatedAtTime.getURI(), DateFormatters.XSD);
		mapPublishingTimeProps.put("http://semantic-mediawiki.org/swivt/1.0#creationDate", DateFormatters.XSD);
		
		mapCreationTimeProps.put(DCTerms.created.getURI(), DateFormatters.XSD);
		

		mapValidTimeProps.put(DCTerms.valid.getURI(), DateFormatters.XSD);
		mapValidTimeProps.put(PROV.invalidatedAtTime.getURI(), DateFormatters.XSD);
//		mapValidTimeProps.put(DCTerms.accrualPeriodicity, value)
		
	}
	
	
	public static boolean isCreationTime(Quad statement) {
		// Extract the predicate of the statement, as the result will be determined based on its URI
		Node predicate = statement.getPredicate();
		
		if(predicate != null && predicate.isURI()) {
			// Check whether the URI of the predicate corresponds to one of the "Creation Time" properties 
			if(mapCreationTimeProps.containsKey(predicate.getURI())) {
				return true;
			}
		}

		return false;
	}
	
	
	/**
	 * Tells whether the quad provided as parameter, refers to the last time when the resource was updated. 
	 * The decision is based on a set of properties which are well-known and commonly used to store such data
	 * @param statement RDF quad (triple) to be tested, in order to determine if it contains "Last Modification Time" information
	 * @return True if the statement refers to the "Last Modification Time" of a resource. False otherwise
	 */
	public static boolean isLastUpdateTime(Quad statement) {
		// Extract the predicate of the statement, as the result will be determined based on its URI
		Node predicate = statement.getPredicate();
		
		if(predicate != null && predicate.isURI()) {
			// Check whether the URI of the predicate corresponds to one of the "Last Modified Time" properties 
			if(mapLastUpdateTimeProps.containsKey(predicate.getURI())) {
				return true;
			}
		}

		return false;
	}
	
	/**
	 * Tells whether the quad provided as parameter, refers to the first time when the data was made available (publishing time). 
	 * The decision is based on a set of properties which are well-known and commonly used to store such data
	 * @param statement RDF quad (triple) to be tested, in order to determine if it contains "Publising Time" information
	 * @return True if the statement refers to the "Publishing Time" of a resource. False otherwise
	 */
	public static boolean isPublisingTime(Quad statement) {
		// Extract the predicate of the statement, as the result will be determined based on its URI
		Node predicate = statement.getPredicate();
		
		if(predicate != null && predicate.isURI()) {
			// Check whether the URI of the predicate corresponds to one of the "Publising Time" properties 
			if(mapPublishingTimeProps.containsKey(predicate.getURI())) {
				return true;
			}
		}

		return false;
	}
	
	/**
	 * Tells whether the quad provided as parameter, refers to the time of expiration of the data (valid time). 
	 * The decision is based on a set of properties which are well-known and commonly used to store such data
	 * @param statement RDF quad (triple) to be tested, in order to determine if it contains "Expiration Time" information
	 * @return True if the statement refers to the "Valid Time" of a resource. False otherwise
	 */
	public static boolean isValidTime(Quad statement) {
		// Extract the predicate of the statement, as the result will be determined based on its URI
		Node predicate = statement.getPredicate();
		
		if(predicate != null && predicate.isURI()) {
			// Check whether the URI of the predicate corresponds to one of the "Expiration Time" properties 
			if(mapValidTimeProps.containsKey(predicate.getURI())) {
				return true;
			}
		}

		return false;
	}
	
	/**
	 * Extracts date and time data contained in an object's value, tries to parse it in a best-effort basis and returns 
	 * the result as a java.util.Date instance. If the extraction/parsing process could not be carried  out successfully, returns null 
	 * @param object RDF Node corresponding to the object of a triple and whose value is intended to be processed as a date/time
	 * @return Value of the provided object as an instance of java.util.Date, null if extraction or conversion process failed
	 */
	public static Date extractTimeFromObject(Quad statement) {
		// Extract the predicate of the statement. The appropriate date formatter to parse the value will be determined based on its URI
		Node predicate = statement.getPredicate();
		Node object = statement.getObject();
		DateFormatters timeFormat = null;
		
		// Datetime values can be extracted from literal objects only
		if(object != null && object.isLiteral()) {
			// First, try to parse date/time literal as an xsd type. Check whether the object is of xsd:dateTime
			if(rdfDateTime.isValid(object.getLiteralValue().toString())) {
				logger.trace("Parsing date/time: " + object.toString() + ", as xsd:date/time value");
				
				XSDDateTime xsdDateTime = (XSDDateTime)rdfDateTime.parse(object.getLiteralValue().toString());
				return xsdDateTime.asCalendar().getTime();
			} else if(rdfDate.isValid(object.getLiteralValue().toString())) {
				logger.trace("Parsing date: " + object.toString() + ", as xsd:date value");
				
				XSDDateTime xsdDate = (XSDDateTime)rdfDate.parse(object.getLiteralValue().toString());
				return xsdDate.asCalendar().getTime();
			}

			// For non-xsd:date/time literals, try to determine the appropriate format specifier, associated to their property. 
			// First, look for the property as publishing time...
			timeFormat = mapPublishingTimeProps.get(predicate.getURI());
			// ... if not found, look up again as last-modified-time
			if(timeFormat == null) {
				timeFormat = mapLastUpdateTimeProps.get(predicate.getURI());
			}
			// ... if still not found, look up again as valid-time
			if(timeFormat == null) {
				timeFormat = mapValidTimeProps.get(predicate.getURI());
			}
			
			if(timeFormat != null && !timeFormat.getFormatSpecifier().equals("")) {
				// Try to parse date if a suitable date/time format specifier was found
				logger.trace("Parsing date/time: " + object.toString() + ", as: " + timeFormat);
				
				try {
					SimpleDateFormat fmtDateTime = new SimpleDateFormat(timeFormat.getFormatSpecifier());
					return fmtDateTime.parse(object.getLiteralValue().toString());
				} catch(ParseException pex) {
					logger.debug("Unable to parse date/time literal: " + object.toString() + ". Format string: " + timeFormat);
				}
			}
		}

		return null;
	}

	/**
	 * @author slondono
	 * Enumeration listing all date formats supported by the analyzer. Additionally, for every supported format,
	 * provides the format specified that, together with java.text.SimpleDateFormat, can be used to parse literals 
	 * corresponding to date values
	 * WARNING: The format specifier used for XSD formats (yyyy-MM-dd'T'HH:mm:ss.SSSXXX) requires Java 7
	 */
	public enum DateFormatters {
		XSD("yyyy-MM-dd'T'HH:mm:ssX"),	// WARNING: This format specifier, which involves X, requires Java 7
		INTL_LONG("yyyy-MM-dd HH:mm:ss"),
		INTL_LONG_TIMEZONE("yyyy-MM-dd HH:mm:ssz");
		
		/**
		 * Format specifier as required by the class SimpleDateFormat to parse and format date objects
		 */
		private String formatSpecifier;
		
		private DateFormatters(String formatSpecifier) {
			this.formatSpecifier = formatSpecifier;
		}
		
		public String getFormatSpecifier() {
			return formatSpecifier;
		}
	}

}
