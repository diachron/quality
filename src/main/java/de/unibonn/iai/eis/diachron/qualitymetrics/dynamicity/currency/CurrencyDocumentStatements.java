package de.unibonn.iai.eis.diachron.qualitymetrics.dynamicity.currency;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.XSD;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;

/**
 * @author Santiago Londono
 * Provides a measure of the degree to which information is up to date, by comparing the time when the data was 
 * observed with the time when the data (the document and each triple) was last modified. In other words, this 
 * metric gives an idea of how recently the dataset has been updated
 */
public class CurrencyDocumentStatements extends AbstractQualityMetric {
	
	private final Resource METRIC_URI = DQM.CurrencyOfDocumentStatementsMetric;
	
	private static Logger logger = Logger.getLogger(CurrencyDocumentStatements.class);
	
	private static RDFDatatype rdfDateTime = NodeFactory.getType(XSD.dateTime.getURI());

	/**
	 * Timestamp representing the instant when the dataset is considered to have been observed 
	 */
	private Date observationTime;

	/**
	 * Timestamp containing the value of the Publishing Time property, as currently found in the set of quads.
	 * if the Publishing Time property is not detected among the processed quads, will be set to the minimum 
	 * Last Modified Time found in the quads. 
	 * (This will also be the case if Publishing Time is found but exists Last Modified Time < Publishing Time).
	 */
	private Date publishingTime;

	/**
	 * RDF property from which the date of publication of the resource will be extracted 
	 */
	private Property publishingTimeProp;
	
	/**
	 * RDF property from which the last time of modification of each statement (quad) will be extracted 
	 */
	private Property lastModifiedTimeProp;
	
	/**
	 * Formatter to be used to parse literal values representing Last Modified Time objects
	 */
	private DateFormat fmtLastModifiedTime;
	
	/**
	 * Formatter to be used to parse literal values representing Publishing Time objects
	 */
	private DateFormat fmtPublishingTime;
	
	/**
	 * Counts the total number of subjects, described by the processed quads, for which a Last Modified Time 
	 * has been detected and successfully parsed
	 */
	private long countTotalModifiedSubjects;
	
	/**
	 * Counter for the occurrences of Last Modified and Publishing Time properties, having literal values inadequately formatted
	 */
	private long countInvalidFormatDates;
	
	/**
	 * Holds the summatory of the difference between the Observation Time and Last Modified Time, over all processed quads (in milliseconds)
	 */
	private long accuModifiedTimeDiffs;

	/**
	 * Default constructor 
	 */
	public CurrencyDocumentStatements() {
		// By default, get the date of publication of the resource from the Dublin Core term: <http://purl.org/dc/terms/issued>
		publishingTimeProp = DCTerms.issued;
		// By default, get the date of modification of the resource from the Dublin Core term: <http://purl.org/dc/terms/modified>
		lastModifiedTimeProp = DCTerms.modified;

		// Set the observation time as the current instant
		observationTime = new Date();
		// Initialize the currently found Publishing Time as null (will be set as soon as it's detected in the processed quads)
		publishingTime = null;

		// Set default custom formatters, used to parse time literals that are not in standard xsd:dateTime format
		fmtLastModifiedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		fmtPublishingTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
	}
	
	/**
	 * Constructor specifying the custom formatters to parse the Publishing Time and Last Modified Time properties. 
	 * Such formatters will be used when processing each of the object values of these properties
	 * @param publishingTimeProp Property corresponding to the Publishing Time of the resource
	 * @param lastModifiedTimeProp Property corresponding to the Last Modified Times of the subjects
	 * @param fmtPublishingTime Custom format to parse the object literals associated to the Publishing Time property
	 * @param fmtLastModifiedTime Custom format to parse the object literals associated to the Last Modified Time property
	 */
	public CurrencyDocumentStatements(Property publishingTimeProp, Property lastModifiedTimeProp, DateFormat fmtPublishingTime, DateFormat fmtLastModifiedTime) {
		// Initialize instance, thereby setting some required default values
		this();
		
		// Set the specified property URIs and custom formatters
		this.publishingTimeProp = publishingTimeProp;
		this.lastModifiedTimeProp = lastModifiedTimeProp;
		this.fmtPublishingTime = fmtPublishingTime;
		this.fmtLastModifiedTime = fmtLastModifiedTime;
	}

	@Override
	public void compute(Quad quad) {
		// Extract the predicate and the corresponding object from the quad
		Node predicate = quad.getPredicate();
		Node object = quad.getObject();

		if(object != null && object.isLiteral() && predicate.isURI()) {
			// Process only predicates identified by an URI, which should refer to a valid temporal property
			if(predicate.hasURI(publishingTimeProp.toString())) {
				// Process the Publishing Time property
				logger.trace("Parsing Publishing Time: " + object.getLiteralValue().toString() + ", as: " + fmtPublishingTime.toString());
				
				Date procPublishingTime = processDateTimeLiteral(object, fmtPublishingTime);
									
				// The Publishing Time explicitly provided in the data, shall be used to normalize the metric's 
				// value. Thus, it will be appropriate only if it is lower than the minimum Last Modified Time
				if(publishingTime == null || procPublishingTime.compareTo(publishingTime) < 0) {
					publishingTime = procPublishingTime;
				}
			}

			if(predicate.hasURI(lastModifiedTimeProp.toString())) {
				// Process the Last Modified Time property
				logger.trace("Parsing Last Modified Time: " + object.getLiteralValue().toString() + ", as: " + fmtLastModifiedTime.toString());

				// Parse the date contained into object's the literal value and set it as current Publishing Time
				Date valLastModifiedTime = processDateTimeLiteral(object, fmtLastModifiedTime);

				if(valLastModifiedTime != null) {
					// Calculate the difference between the Observation Time and the Last Modified Time for this quad, accumulate it
					accuModifiedTimeDiffs += (observationTime.getTime() - valLastModifiedTime.getTime());
					// Increase the number of subjects for which a Last Modified Time has been successfully determined
					countTotalModifiedSubjects++;

					// Make sure that there are no Last Modified Time values lower than the Publishing Time. Moreover, ensure 
					// that Publishing Time will be set even when the Publishing Time property is not provided in the set of quads
					if(publishingTime == null || valLastModifiedTime.compareTo(publishingTime) < 0) {
						publishingTime = valLastModifiedTime;
					}
				} else {
					logger.trace("Unable to parse date/time literal value: " + object.getLiteralValue().toString() + " of property: " + predicate.getURI());
					
					// Increase the counter of temporal values with unrecognized format 
					countInvalidFormatDates++;
				}
			}
		}
	}

	@Override
	public double metricValue() {
		logger.trace("Calculating Currency of Document/Statements" + publishingTime + " No. of subjects with modified time set: " + 
					 countTotalModifiedSubjects + ". No. of time values with invalid format: " + countInvalidFormatDates);
		
		// Verify that all the information required to perform the calculation is set
		if(observationTime != null && publishingTime != null && countTotalModifiedSubjects > 0) {
			// Calculate the metric value as the average of the normalized currency calculated for all the subjects
			return (countTotalModifiedSubjects - ((double)accuModifiedTimeDiffs/(double)(observationTime.getTime() - publishingTime.getTime())))/(double)countTotalModifiedSubjects;
		} else {
			logger.trace("Currency of Document/Statements could not be calculated. Insufficient information");
			return 0;
		}
	}
	
	/**
	 * Parses the literal node provided as parameter as a datetime value, in a best-effort basis. There are two ways to parse the literal: 
	 * through a custom datetime formatter (provided as parameter) or interpreting the literal value as an xsd:dateTime. The former 
	 * option is attempted first (if the custom formatter is provided), since the latter is computationally more expensive  
	 * @param dateLiteral Node literal, representing an object, whose value is to be parsed
	 * @param fmtDateTimeCustom Optional custom datetime formatter, which would be used as preferred way to parse the literal value
	 * @return Date object equivalent to the literal value, or null if it was not possible to process 
	 */
	private Date processDateTimeLiteral(Node dateLiteral, DateFormat fmtDateTimeCustom) {
		// Datetime values can be extracted from literal objects only
		if(dateLiteral != null && dateLiteral.isLiteral()) {
			// If a custom formatter was provided, try to use it first
			if(fmtDateTimeCustom != null) {
				try {
					return fmtDateTimeCustom.parse(dateLiteral.getLiteralValue().toString());
				} catch (ParseException pex) {
					logger.trace("Unable to parse date/time literal value: " + dateLiteral.getLiteralValue().toString());
				}
			}

			// Finally, try to parse the literal as an xsd:dateTime
			if(rdfDateTime.isValid(dateLiteral.getLiteralValue().toString())) {
				logger.trace("Parsing date/time literal value as xsd:dateTime: " + dateLiteral.getLiteralValue().toString());
				
				XSDDateTime xsdDateTime = (XSDDateTime)rdfDateTime.parse(dateLiteral.getLiteralValue().toString());
				return xsdDateTime.asCalendar().getTime();
			}
		}
		
		return null;
	}

	@Override
	public Resource getMetricURI() {
		return METRIC_URI;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Returns the timestamp used as Observation Time during the calculation of the metric value
	 * @return Observation Time associated to this instance
	 */
	public Date getObservationTime() {
		return observationTime;
	}

	/**
	 * Returns the number of subjects for which a Last Modified Time property was detected, but it 
	 * turned out to have an invalid value that could not be parsed
	 * @return Number of subjects, as described by the set of quads, having an invalid Last Modified Time
	 */
	public long getCountInvalidFormatDates() {
		return countInvalidFormatDates;
	}

}