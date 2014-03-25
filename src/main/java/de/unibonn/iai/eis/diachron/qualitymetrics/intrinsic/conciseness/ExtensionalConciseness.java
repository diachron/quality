package de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.conciseness;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.ComparableSubject;

/**
 * @author Santiago Londono
 * Provides a measure of the consistency of the dataset, by calculating the Extensional Conciseness metric, 
 * which is part of the Conciseness dimension.
 */
public class ExtensionalConciseness extends AbstractQualityMetric {
	
	private static Logger logger = Logger.getLogger(ExtensionalConciseness.class);
	
	/**
	 * Map indexing the subjects detected during the computation of the metric. Every subject is identified 
	 * by a different id (URI), which serves as key of the map. The value of each subject consists of a 
	 * resource. A Hashtable is used since it is synchronized and metric instances ought to be thread safe.
	 */
	private Hashtable<String, ComparableSubject> mapSubjects = new Hashtable<String, ComparableSubject>();

	/**
	 * Re-computes the value of the Extensional Conciseness Metric, by considering a new quad provided.
	 * @param quad The new quad to be considered in the computation of the metric
	 */
	@Override
	public void compute(Quad quad) {
		// Every time a new quad is considered, check whether the subject has already been identified
		ComparableSubject subject = mapSubjects.get(quad.getSubject().getURI());

		if(subject == null) {
			// The subject does not exists in the map. Add it, indexed by its URI
			subject = new ComparableSubject(quad.getSubject().getURI());
			mapSubjects.put(quad.getSubject().getURI(), subject);
			logger.trace("Added new subject: " + quad.getSubject().getURI());
		}

		// Add or update the property stated by the current quad into the subject, as a predicate with a value.
		// The value of the property is extracted from the quad's object
		subject.addProperty(quad.getPredicate().getURI(), quad.getObject());
		logger.trace(" - Added property to subject: " + subject.getUri() + " -> " + quad.getObject().toString());
	}

	/**
	 * Returns the current value of the Extensional Conciseness Metric, computed as the ratio of the 
	 * Number of Unique Subjects to the Total Number of Subjects. 
	 * Subjects are the objects being described by the quads provided on invocations to the compute 
	 * method, each subject is identified by its URI (the value of the subject attribute of the quad). 
	 * Uniqueness of subjects is determined from its properties: one subject is said to be unique 
	 * if and only if there is no other subject equivalent to it.
	 * - Note that two equivalent subjects may have different ids (URIs).
	 * @return Current value of the Extensional Conciseness Metric: (No. of Unique Subjects / Total No. of Subjects)
	 */
	@Override
	public double metricValue() {
		// Keep a list free from redundant subjects, that is with all its unique elements
		List<ComparableSubject> lstUniqueSubjects = new ArrayList<ComparableSubject>();
		boolean isCurSubjectUnique;
		
		// Compare each of the subjects with the ones already recognized as unique...
		for(ComparableSubject curSubject : mapSubjects.values()) {
			isCurSubjectUnique = true;

			for(ComparableSubject curUniqueSubject : lstUniqueSubjects) {
				// and if the subject currently examined is equivalent to one of the existing 
				// unique subjects, do not regard it as unique, continue with the next one
				if(curSubject.isEquivalentTo(curUniqueSubject)) {
					isCurSubjectUnique = false;
					break;
				}
			}
			
			// Finally, if the current subject is not equivalent to any of 
			// the existing unique subjects, add it as unique
			if(isCurSubjectUnique) {
				lstUniqueSubjects.add(curSubject);
			}
		}
		
		// If any subject is equivalent to another, it will not be part of the list of unique subjects, then 
		// the size of this list is the "Count of Unique Subjects" required to calculate the metric
		return ((double)lstUniqueSubjects.size()) / ((double)mapSubjects.size());
	}

	@Override
	public Resource getMetricURI() {
		// TODO Implement getMetricURI(). Add URI of this metric to de.unibonn.iai.eis.diachron.vocabularies.DQM
		return null;
	}

}
