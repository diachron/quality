package eu.diachron.qualitymetrics.dynamicity.currency;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.semantics.vocabulary.DQM;

/**
 * @author Santiago Londono
 * Provides a measure of the degree to which information is up to date, by comparing the total number of resources 
 * described by the dataset, versus the number of those that are recognized to be outdated
 */
public class ExclusionOutdatedData implements QualityMetric {
	
	private final Resource METRIC_URI = DQM.ExclusionOfOutdatedDataMetric;
	
	private static Logger logger = LoggerFactory.getLogger(ExclusionOutdatedData.class);
	
	/**
	 * Timestamp representing the instant when the dataset is considered to have been observed 
	 */
	private Date observationTime;
	
	/**
	 * Map indexing the subjects found to be declared in the dataset. Key of entries is a combination of the 
	 * URI of the subject and object of the statement (triple) declaring the instance. 
	 * A ConcurrentHashMap is used since it is synchronized and metric instances ought to be thread safe.
	 */
	private ConcurrentHashMap<String, Integer> mapInstances = new ConcurrentHashMap<String, Integer>();
	
	/**
	 * Counts the total number of subjects, described by the processed quads, that have been found to be expired. 
	 * This implies that an Expiration/Valid time for the subject was found and successfully parsed
	 */
	private long countTotalOutdatedSubjects;
	
	/**
	 * Counter for the occurrences of Expiration/Valid Time properties, having literal values inadequately formatted
	 */
	private long countInvalidFormatDates;
	
	/**
	 * Default constructor 
	 */
	public ExclusionOutdatedData() {
		// Set the observation time as the current instant
		observationTime = new Date();
	}

	/**
	 * Processes a single quad making part of the dataset. Firstly, tries to figure out whether the quad contains temporal information
	 * stating when the dataset expires (i.e. its validity). This is done by evaluating the properties currently set as sources of the 
	 * Expiration/Valid Time. All other properties are ignored. When temporal information is detected, tries to parse it in a 
	 * best-effort-basis and on failure, quad is discarded and counted in countInvalidFormatDates. Then, checks whether the recognized 
	 * Expiration/Valid Time indicates that the resource is outdated
	 * @param quad Quad to be processed and examined for temporal information of interest for the calculation of the metric 
	 */
	public void compute(Quad quad) {
		// Extract the predicate and object of the statement
		Node object = quad.getObject();
		
		// Check whether the current quad represents an instance declaration, in order to keep to date the total number of subjects
		checkNewInstanceDeclared(quad);
		
		// Check whether the current property corresponds to the Expiration/Valid Time of the subject described by the quad
		if(TemporalDataAnalyzer.isValidTime(quad)) {
			// Process the Expiration/Valid Time property
			logger.trace("Parsing valid time: {}", object.toString());

			// Parse the date contained into object's the literal value and set it as current Publishing Time
			Date valValidTime = TemporalDataAnalyzer.extractTimeFromObject(quad);

			if(valValidTime != null) {
				// Calculate the difference between the Observation Time and the Expiration/Valid Time for this quad...
				long remainingValidTime = (valValidTime.getTime() - observationTime.getTime());
				
				// Check whether the instance has expired, by verifying that its Expiration/Valid Time is not before the Observation Time
				if(remainingValidTime <= 0) {
					// Expired instance (subject) detected, increase count
					countTotalOutdatedSubjects++;
				}
			} else {
				// Increase the counter of temporal values with unrecognized format 
				countInvalidFormatDates++;
			}
		}
	}

	/**
	 * Returns the current value of the Exclusion of Outdated Data Metric, computed as one minus the ratio of the 
	 * Number of outdated subjects (instances) to the Total number of declared subjects.
	 * An instance is a resource R declared to be of a certain class C, by a statement R rdf:type C.
	 * If there are two or more statements R rdf:type C, with the same URI for R (subject) and C (class), 
	 * the corresponding declared instance is considered to be non-unique. Outdated instances are recognized by 
	 * comparing the value of their Expiration/Valid property with the current time (Observation Time). 
	 * @return Current value of the Exclusion of Outdated Data Metric: 1 - (No. of Outdated Instances / Total No. of Unique Declared Instances)
	 */
	public double metricValue() {
		// Determine the Total No. of Unique Declared Instances
		int countTotalUniqueInstances = mapInstances.size();
		logger.trace("Calculating value of Exclusion of outdated data. {}/{}. Invalid dates: {}", countTotalOutdatedSubjects, countTotalUniqueInstances, countInvalidFormatDates);
		
		return 1.0 - ((double)countTotalOutdatedSubjects / (double)countTotalUniqueInstances);
	}

	public Resource getMetricURI() {
		return METRIC_URI;
	}

	public ProblemList<?> getQualityProblems() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Verifies whether the quad provided as parameter corresponds to an instance declaration, that is, if it implies that 
	 * a new subject has been found to be described in the data. Additionally, checks if the instance has already been declared 
	 * previously, 
	 * @param quad Statement to be checked, in order to determine if it represents an new instance (subject) declaration
	 */
	private void checkNewInstanceDeclared(Quad quad) {
		// Check whether current triple corresponds to an instance declaration
		Node predicateEdge = quad.getPredicate();
		logger.trace("Checking new instance declaration withing predicate: {}" + predicateEdge);
		
		// Determines whether the specified predicate corresponds to an instance declaration 
		// statement, that is, whether the statement is of the form: Instance rdf:type Class
		if(predicateEdge != null && predicateEdge.isURI() && predicateEdge.hasURI(RDF.type.getURI())) {
			// Build the Id of the instance, concatenating the instance's (subject) URI and the URI of its class
			String newInstanceId = quad.getSubject().toString() + "||" + quad.getObject().toString();
			logger.trace("Instance declared: " + newInstanceId);

			// Check if the instance already exists in the instances map
			Integer countPrevInstDecls = mapInstances.get(newInstanceId);

			if(countPrevInstDecls == null) {
				// Put the new instance declaration in the map, with a value of 1 indicating that such an 
				// instance has been declared for the first time
				mapInstances.put(newInstanceId, 1);
				logger.trace("New instance declaration found");
			} else {
				// Instance was already declared, increase the number of appearances and the non-unique counter
				countPrevInstDecls++;
				logger.trace("Non-unique instance found. Occurrences: " + countPrevInstDecls);
			}
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

}
