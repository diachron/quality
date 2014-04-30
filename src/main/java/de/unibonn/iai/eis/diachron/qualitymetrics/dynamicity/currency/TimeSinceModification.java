package de.unibonn.iai.eis.diachron.qualitymetrics.dynamicity.currency;

import org.apache.log4j.Logger;
import com.hp.hpl.jena.rdf.model.Resource;
import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;

/**
 * @author Santiago Londono
 * Provides a measure of the degree to which information is up to date, by taking the difference between the current time 
 * (i.e. the instant when the present calculation of the metric was initiated) and the time when the data (the document or each triple) 
 * was last modified. In other words, this metric gives an idea of how recently the dataset has been updated.
 */
public class TimeSinceModification extends CurrencyDocumentStatements {
	
	private final Resource METRIC_URI = DQM.TimeSinceModificationMetric;
	
	private static Logger logger = Logger.getLogger(TimeSinceModification.class);
			
	/**
	 * Default constructor 
	 */
	public TimeSinceModification() {
		super();
	}

	/**
	 * Performs the final calculation of the metric and returns its result. The formula driving the calculation is:
	 * Time-Since-Modification = AVG(<Observation Time> - <Last Modified Time N>). 
	 * Where <Observation Time> is the instant when the calculation of the metric was initiated (set in the constructor);  
	 * <Last Modified Time N> is the last time subject N, described in the quads, was modified (taken from the property set in lastModifiedTimeProp)
	 * @return Value of the metric in seconds, or -1 if it could not be calculated
	 */
	@Override
	public double metricValue() {
		logger.trace("Calculating Currency of Time Since Modification No. of subjects with modified time set: " + 
				 this.getCountTotalModifiedSubjects() + ". No. of time values with invalid format: " + this.getCountInvalidFormatDates());

		// Verify that all the information required to perform the calculation is set
		if(this.getObservationTime() != null && this.getCountTotalModifiedSubjects() > 0) {
			// Calculate the metric value as the average of the normalized currency calculated for all the subjects
			return (double)this.getAccuModifiedTimeDiffs() / (double)this.getCountTotalModifiedSubjects();
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

}
