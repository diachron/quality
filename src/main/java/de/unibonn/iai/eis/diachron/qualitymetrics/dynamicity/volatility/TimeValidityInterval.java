package de.unibonn.iai.eis.diachron.qualitymetrics.dynamicity.volatility;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

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
 * Measures the frequency with which data varies over time, by calculating the length of the 
 * time interval during which data remains valid
 */
public class TimeValidityInterval extends AbstractQualityMetric {
	
	private final Resource METRIC_URI = DQM.TimeValidityIntervalMetric;
	
	private static Logger logger = LoggerFactory.getLogger(TimeValidityInterval.class);
	
	/**
	 * Map indexing the subjects found to be declared in the dataset. Key of entries is a combination of the 
	 * URI of the subject and object of the statement (triple) declaring the instance. The values are structures containing 
	 * the temporal information collected for the corresponding entity (subject).
	 * A ConcurrentHashMap is used since it is synchronized and metric instances ought to be thread safe.
	 */
	private ConcurrentHashMap<String, EntityTemporalInfoSet> mapInstances = new ConcurrentHashMap<String, EntityTemporalInfoSet>();
	
	/**
	 * Counter for the occurrences of Expiration/Valid Time properties, having literal values inadequately formatted
	 */
	private long countInvalidFormatDates;
	
	/**
	 * Timestamp containing the value of the earliest Publishing Time property detected among the processed quads.
	 * This value is to be used as a "best guess" of the Publishing Time of the resource containing the quads.
	 */
	private Date earliestPublishingTime;

	/**
	 * Processes a single quad making part of the dataset. Firstly, tries to figure out whether the quad contains temporal information
	 * stating when the dataset expires (i.e. its validity) or when it was published (i.e. input time). This is done by evaluating 
	 * the properties currently set as sources of the Expiration/Valid Time and Publishing Time. All other properties are ignored. 
	 * @param quad Quad to be processed and examined for temporal information of interest for the calculation of the metric
	 */
	@Override
	public void compute(Quad quad) {
		// Extract the predicate and object of the statement
		Node subject = quad.getSubject();
		Node object = quad.getObject();
		
		// Determine current subject's URI
		String subjectURI = null;
		
		// Figure out the type of temporal information contained into the quad, if it does
		boolean isQuadValidTime = TemporalDataAnalyzer.isValidTime(quad);
		boolean isQuadPublishingTime = TemporalDataAnalyzer.isPublisingTime(quad);
		boolean isQuadLastModifiedTime = TemporalDataAnalyzer.isLastUpdateTime(quad);
		
		boolean quadHasTemporalInfo = (isQuadValidTime || isQuadPublishingTime || isQuadLastModifiedTime);

		// Check conditions and mandatory information required for the computation of this metric: 
		// subject is uniquely identified by an URI and the current quad refers to temporal information of interest
		if((subject != null && subject.isURI()) && quadHasTemporalInfo) {
			// Allowed assignments for subjects: URIs or Blank nodes. Make sure the subject has an URI
			subjectURI = subject.getURI();
		} else {
			logger.debug("Ommiting blank subject or no temporal info computing for quad {}. Temporal info: {}", quad, quadHasTemporalInfo);
			return;
		}
		
		// Obtain the structure holding the temporal information of the current subject (entity)
		EntityTemporalInfoSet tempInfoSet = mapInstances.get(subjectURI);

		// Initialize the temporal information for the current subject if not yet created
		if(tempInfoSet == null) {
			logger.trace("Initializing subject: {}", subject);
			tempInfoSet = new EntityTemporalInfoSet();
			mapInstances.put(subjectURI, tempInfoSet);
		}
		
		// Check whether the current property corresponds to the Expiration/Valid Time of the subject described by the quad
		if(isQuadValidTime) {
			// Process the Expiration/Valid Time property
			logger.trace("Parsing valid time: {}, for subject: {}", object, subjectURI);
						
			// Parse the date contained into object's the literal value and set it as current Publishing Time
			Date valValidTime = TemporalDataAnalyzer.extractTimeFromObject(quad);

			if(valValidTime != null) {
				// Set the value of the property, for the current subject (entity)
				tempInfoSet.setValidTime(valValidTime);
			} else {
				// Increase the counter of temporal values with unrecognized format 
				countInvalidFormatDates++;
			}
		}
		// Check whether the current property corresponds to the Publishing Time of the subject described by the quad
		else if(isQuadPublishingTime) {
			// Process the Publishing Time property
			logger.trace("Parsing publishing time: {}, for subject: {}", object, subjectURI);

			// Parse the date contained into object's the literal value and set it as current Publishing Time
			Date valPublishingTime = TemporalDataAnalyzer.extractTimeFromObject(quad);

			if(valPublishingTime != null) {
				// Set the value of the property, for the current subject (entity)
				tempInfoSet.setPublishedTime(valPublishingTime);
				
				// Keep track of the earliest publishing time found among the quads
				if(earliestPublishingTime == null || earliestPublishingTime.getTime() > valPublishingTime.getTime()) {
					earliestPublishingTime = valPublishingTime;
				}
			} else {
				// Increase the counter of temporal values with unrecognized format 
				countInvalidFormatDates++;
			}
		}
		// Check whether the current property corresponds to the Expiration/Valid Time of the subject described by the quad
		if(isQuadLastModifiedTime) {
			// Process the Last Modified Time property
			logger.trace("Parsing last-modified time: {}, for subject: {}", object, subjectURI);
						
			// Parse the date contained into object's the literal value and set it as current Last Modified Time
			Date valLastModifiedTime = TemporalDataAnalyzer.extractTimeFromObject(quad);

			if(valLastModifiedTime != null) {
				// Set the value of the property, for the current subject (entity)
				tempInfoSet.setLastModifiedTime(valLastModifiedTime);
			} else {
				// Increase the counter of temporal values with unrecognized format 
				countInvalidFormatDates++;
			}
		}
	}

	/**
	 * Returns the current value of the Time Validity Interval Metric in seconds, computed as the average of the 
	 * differences between the Expiration/Validity Time and Publishing Time (a.k.a Input Time) of the instances 
	 * described by the processed quads. 
	 * @return Current value of the Time Validity Interval Metric: AVG[(Expiration/Validity Time) - (Publishing/Input Time)] in seconds 
	 */
	@Override
	public double metricValue() {
		// Accumulated duration of all validity intervals
		long accumValidIntervalTime = 0;
		long countValidInstances = 0;
		
		// Process each of the instances found to have temporal information
		for(EntityTemporalInfoSet curTempInfoSet : mapInstances.values()) {
			
			// Check whether the instance has an Expiration/Validity Time set, as it is required for this computation
			if(curTempInfoSet.getValidTime() != null) {
				// Calculate the validity interval for the current instance according to whether its publishing time was specified
				if(curTempInfoSet.getPublishedTime() != null) {
					// The publishing time of this particular instance was specified, use it
					accumValidIntervalTime += Math.max((curTempInfoSet.getValidTime().getTime() - curTempInfoSet.getPublishedTime().getTime()), 0);
				} else if(earliestPublishingTime != null) {
					// If an earliest publishing time, used as publishing time of the whole document was estimated, use it
					accumValidIntervalTime += Math.max((curTempInfoSet.getValidTime().getTime() - earliestPublishingTime.getTime()), 0);
				}

				countValidInstances++;
			}
		}
		
		// Calculate the final result of the metric as the average of the length (in seconds) of all validity intervals
		return (((double)accumValidIntervalTime)/1000.0) / ((double)countValidInstances);
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
	 * @return	Counter for the occurrences of Expiration/Valid Time properties, having literal values inadequately formatted
	 */
	public long getCountInvalidFormatDates() {
		return countInvalidFormatDates;
	}

	/**
	 * @author Santiago Londono
	 * Behaves like a structure for holding temporal information regarding an entity
	 */
	protected static class EntityTemporalInfoSet {
		
		/**
		 * Expiration or Validity time of the entity
		 */
		private Date validTime;
		
		/**
		 * Time at which the entity was last updated
		 */
		private Date lastModifiedTime;
		
		/**
		 * Time at which the information about the entity was issued
		 */
		private Date publishedTime;
		
		/**
		 * Gets the Expiration or Validity time of the entity
		 */
		public Date getValidTime() {
			return validTime;
		}

		/**
		 * Sets the Expiration or Validity time of the entity
		 */
		public void setValidTime(Date validTime) {
			this.validTime = validTime;
		}

		/**
		 * Gets the Time at which the entity was last updated
		 */
		public Date getLastModifiedTime() {
			return lastModifiedTime;
		}

		/**
		 * Sets the Time at which the entity was last updated
		 */
		public void setLastModifiedTime(Date lastModifiedTime) {
			this.lastModifiedTime = lastModifiedTime;
		}

		/**
		 * Gets the Time at which the information about the entity was issued
		 */
		public Date getPublishedTime() {
			return publishedTime;
		}

		/**
		 * Sets the Time at which the information about the entity was issued
		 */
		public void setPublishedTime(Date publishedTime) {
			this.publishedTime = publishedTime;
		}
	}

}
