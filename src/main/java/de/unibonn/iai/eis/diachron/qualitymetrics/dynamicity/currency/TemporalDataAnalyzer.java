package de.unibonn.iai.eis.diachron.qualitymetrics.dynamicity.currency;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.XSD;

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
		
		mapLastUpdateTimeProps.put(DCTerms.modified.getURI(), DateFormatters.INTL_LONG);
		mapLastUpdateTimeProps.put("http://purl.org/dc/terms/#modified", DateFormatters.INTL_LONG);
		mapLastUpdateTimeProps.put("http://semantic-mediawiki.org/swivt/1.0#wikiPageModificationDate", DateFormatters.INTL_LONG);
		
		mapPublishingTimeProps.put(DCTerms.issued.getURI(), DateFormatters.XSD);
		mapPublishingTimeProps.put(DCTerms.created.getURI(), DateFormatters.XSD);
		mapPublishingTimeProps.put("http://semantic-mediawiki.org/swivt/1.0#creationDate", DateFormatters.XSD);
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
			if(rdfDate.isValid(object.getLiteralValue().toString()) || rdfDateTime.isValid(object.getLiteralValue().toString())) {
				logger.trace("Parsing date/time: " + object.getLiteralValue().toString() + ", as xsd:date/time value");
				
				XSDDateTime xsdDateTime = (XSDDateTime)rdfDateTime.parse(object.getLiteralValue().toString());
				return xsdDateTime.asCalendar().getTime();
			}

			// For non-xsd:date/time literals, try to determine the appropriate format specifier, associated to their property. 
			// First, look for the property as publishing time...
			timeFormat = mapPublishingTimeProps.get(predicate.getURI());
			// ... if not found, look up again as last-modified-time
			if(timeFormat == null) {
				timeFormat = mapLastUpdateTimeProps.get(predicate.getURI());
			}
			
			if(timeFormat != null && !timeFormat.getFormatSpecifier().equals("")) {
				// Try to parse date if a suitable date/time format specifier was found
				logger.trace("Parsing date/time: " + object.getLiteralValue().toString() + ", as: " + timeFormat);
				
				try {
					SimpleDateFormat fmtDateTime = new SimpleDateFormat(timeFormat.getFormatSpecifier());
					return fmtDateTime.parse(object.getLiteralValue().toString());
				} catch(ParseException pex) {
					logger.debug("Unable to parse date/time literal: " + object.getLiteralValue().toString() + ". Format string: " + timeFormat);
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
	 */
	private enum DateFormatters {
		XSD(""),
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
