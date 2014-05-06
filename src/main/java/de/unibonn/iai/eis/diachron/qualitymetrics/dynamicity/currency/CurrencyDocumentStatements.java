package de.unibonn.iai.eis.diachron.qualitymetrics.dynamicity.currency;

import java.util.Date;

import org.apache.log4j.Logger;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;

/**
 * @author Santiago Londono
 * Provides a measure of the degree to which information is up to date, by comparing the current time (the 
 * instant when the present calculation of the metric was initiated) with the time when the data (the document or each of the quads) 
 * was last modified. In other words, this metric gives an idea of how recently the dataset has been updated
 */
public class CurrencyDocumentStatements extends AbstractQualityMetric {
	
	private final Resource METRIC_URI = DQM.CurrencyOfDocumentStatementsMetric;
	
	private static Logger logger = Logger.getLogger(CurrencyDocumentStatements.class);
	
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
		// Set the observation time as the current instant
		observationTime = new Date();
	}

	/**
	 * Processes a single quad making part of the dataset. Firstly, tries to figure out whether the quad contains temporal information
	 * stating when the dataset was published, or when a particular subject was last modified. This is done by evaluating the 
	 * properties currently set as sources of the Publishing and Last Modifidied Time. All other properties are ignored. When temporal 
	 * information is detected, tries to parse it in a best-effort-basis and on failure, quad is discarded and counted in countInvalidFormatDates
	 * @param quad Quad to be processed and examined for temporal information of interest for the calculation of the metric 
	 */
	@Override
	public void compute(Quad quad) {
		// Extract the predicate and object of the statement
		Node object = quad.getObject();
		
		// Check whether the current property corresponds to the Publishing time of the resource containing the quad
		if(TemporalDataAnalyzer.isPublisingTime(quad)) {
			// Process the Publishing Time property
			logger.trace("Parsing publishing time: " + object.toString());
			
			Date procPublishingTime = TemporalDataAnalyzer.extractTimeFromObject(quad);
			
			// The Publishing Time explicitly provided in the data, shall be used to normalize the metric's 
			// value. Thus, it will be appropriate only if it is lower than the minimum Last Modified Time
			if(publishingTime == null || procPublishingTime.compareTo(publishingTime) < 0) {
				publishingTime = procPublishingTime;
			}
		}

		// Check whether the current property corresponds to the Last Modified Time of the subject described by the quad
		if(TemporalDataAnalyzer.isLastUpdateTime(quad)) {
			// Process the Last Modified Time property
			logger.trace("Parsing last modified time: " + object.toString());

			// Parse the date contained into object's the literal value and set it as current Publishing Time
			Date valLastModifiedTime = TemporalDataAnalyzer.extractTimeFromObject(quad);

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
				// Increase the counter of temporal values with unrecognized format 
				countInvalidFormatDates++;
			}
		}
	}

	/**
	 * Performs the final calculation of the metric and returns its result. The formula driving the calculation is:
	 * Currency = AVG[1 - (<Observation Time> - <Last Modified Time N>)/(<Observation Time> - <Publishing Time>)].
	 * Where <Observation Time> is the instant when the calculation of the metric was initiated (set in the constructor);  
	 * <Last Modified Time N> is the last time subject N, described in the quads, was modified (taken from the property set in lastModifiedTimeProp) 
	 * and <Publishing Time> is the time when the dataset was made available (taken from the property set in publishingTimeProp or if not found, 
	 * set as the lowest value of the registered Last Modified Times.
	 * @return Value of the metric in the range [0,1] or -1 if it could not be calculated 
	 */
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
			return -1;
		}
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

	/**
	 * Returns the number of subjects for which a Last Modified Time property was detected and 
	 * successfully extracted, parsed and used in the final computation of the metric
	 * @return Number of subjects, as described by the set of quads, having a valid Last Modified Time
	 */
	protected long getCountTotalModifiedSubjects() {
		return countTotalModifiedSubjects;
	}

	/**
	 * Returns the accumulated differences between the observation time and the last-modified times of all the subjects 
	 * for which such data was provided. That is, returns the summation of the ages of all the resources in the dataset, 
	 * having a known and valid last-modified time
	 * @return Accumulated differences between the observation time and the last-modified times over all quads
	 */
	protected long getAccuModifiedTimeDiffs() {
		return accuModifiedTimeDiffs;
	}

}