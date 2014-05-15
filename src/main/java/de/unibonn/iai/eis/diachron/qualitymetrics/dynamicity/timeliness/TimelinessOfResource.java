package de.unibonn.iai.eis.diachron.qualitymetrics.dynamicity.timeliness;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.qualitymetrics.dynamicity.currency.TemporalDataAnalyzer;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;

/**
 * @author Santiago Londono
 * Assesses the difference between the last modified time of the original data source and the last 
 * modified time of the semantic web source, thereby indicating that the resource is most likely outdated
 */
public class TimelinessOfResource extends AbstractQualityMetric {
	
	private final Resource METRIC_URI = DQM.TimelinessOfResourceMetric;
	
	private static Logger logger = LoggerFactory.getLogger(TimelinessOfResource.class);
	
	/**
	 * Timestamp representing the instant when the dataset is considered to have been observed 
	 */
	private Date observationTime;
	
	/**
	 * Counter for the occurrences of Expiration/Valid Time properties, having literal values inadequately formatted
	 */
	private long countInvalidFormatDates;
	
	/**
	 * Counts the total number of subjects, described by the processed quads, for which an Expiration/Valid Time  
	 * has been detected and successfully parsed
	 */
	private long countTotalAccountedSubjects;
	
	/**
	 * Holds the summation of the difference between the Observation Time and Expiration/Valid Time, over all processed quads (in milliseconds)
	 */
	private long accuValidTimeDiffs;

	/**
	 * Default constructor
	 */
	public TimelinessOfResource() {
		// Set the observation time as the current instant
		observationTime = new Date();
	}
	
	/**
	 * Processes a single quad making part of the dataset. Firstly, tries to figure out whether the quad contains temporal information
	 * stating when an entity expires (i.e. its validity). This is done by evaluating the properties currently set as sources of 
	 * the Expiration/Valid Time (all other properties are ignored), if such a property is found, its value is substracted from the current 
	 * observation time, and the result is accumulated. The total accumulated differences will be used later to calculate the final value of the metric 
	 * @param quad Quad to be processed and examined for temporal information of interest for the calculation of the metric
	 */
	@Override
	public void compute(Quad quad) {
		// Extract the predicate and object of the statement
		Node subject = quad.getSubject();
		Node object = quad.getObject();
				
		// Check whether the current property corresponds to the Expiration/Valid Time of the subject described by the quad
		if(TemporalDataAnalyzer.isValidTime(quad)) {
			// Process the Expiration/Valid Time property
			logger.trace("Parsing valid time: {}, for subject: {}", object, subject);
						
			// Parse the date contained into object's the literal value and set it as current Publishing Time
			Date valValidTime = TemporalDataAnalyzer.extractTimeFromObject(quad);

			if(valValidTime != null) {
				// Calculate the difference between the Expiration/Valid time of the current instance and the observation time and accumulate it
				accuValidTimeDiffs += (observationTime.getTime() - valValidTime.getTime());	
				// The Expiration/Valid Time property has been successfully processed for another subject, count it
				countTotalAccountedSubjects++;
			} else {
				// Increase the counter of temporal values with unrecognized format 
				countInvalidFormatDates++;
			}
		}
		
	}

	/**
	 * Returns the current value of the Timeliness of the Resource Metric in seconds, computed as the average of the 
	 * differences between the Observation time and the Expiration/Validity Time of all resources described by the processed quads
	 * @return Current value of the Timeliness of the Resource Metric: AVG[(Observation Time) - (Expiration/Validity Time)] in seconds 
	 */
	@Override
	public double metricValue() {
		logger.trace("Calculating Timeliness of the Resource. No. of subjects with valid-time set: " + countTotalAccountedSubjects + ". No. of time values with invalid format: " + countInvalidFormatDates);
	
		// Verify that all the information required to perform the calculation is set
		if(observationTime != null && countTotalAccountedSubjects > 0) {
			// Calculate the metric value as the average of the differences between the Observation Time and 
			// the Expiration/Valid Time of each resource described in the quads
			return (((double)accuValidTimeDiffs)/((double)countTotalAccountedSubjects));
		} else {
			logger.trace("Timeliness of the Resource could not be calculated. Insufficient information");
			return 0; //If insufficient information, return 0 - lowest possible
		}
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

	@Override
	public Resource getMetricURI() {
		return METRIC_URI;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		// TODO Auto-generated method stub
		return null;
	}

}
