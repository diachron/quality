package de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.conciseness;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;

/**
 * @author Santiago Londono
 * Provides a measure of the redundancy of the dataset at the data level, by calculating the 
 * Duplicate Instance metric, which is part of the Conciseness dimension.
 */
public class DuplicateInstance extends AbstractQualityMetric {
	
	private static Logger logger = Logger.getLogger(DuplicateInstance.class);
	
	private final Resource METRIC_URI = DQM.DuplicateInstanceMetric;
	
	/**
	 * Map indexing the instances found to be declared in the dataset. Key of entries is a combination of the 
	 * URI of the subject and object of the statement (triple) declaring the instance. 
	 * A ConcurrentHashMap is used since it is synchronized and metric instances ought to be thread safe.
	 */
	private ConcurrentHashMap<String, Integer> mapInstances = new ConcurrentHashMap<String, Integer>();
	
	/**
	 * Counter for the number of instances violating the uniqueness rule. Counts the duplicated 
	 * instances only (e.g. if an instance is declared twice, the value of the counter will be 1).
	 */
	private int countNonUniqueInst = 0;
	
	/**
	 * Counter for the total number of declared instances (occurrences of the triple A rdf:type C).
	 */
	private int countTotalInst = 0;

	/**
	 * Re-computes the value of the Duplicate Instance Metric, by considering a new quad provided.
	 * @param quad The new quad to be considered in the computation of the metric. Must be not null.
	 */
	@Override
	public void compute(Quad quad) {
		// Check whether current triple corresponds to an instance declaration
		logger.trace("Computing triple with predicate: " + quad.getPredicate().getURI());
		Node predicateEdge = quad.getPredicate();
		
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
				countNonUniqueInst++;
				logger.trace("Non-unique instance found. Occurrences: " + countPrevInstDecls);
			}

			countTotalInst++;
		}
	}

	/**
	 * Returns the current value of the Duplicate Instance Metric, computed as one minus the ratio of the 
	 * Number of instances violating the uniqueness rule to the Total number of declared instances.
	 * An instance is a resource R declared to be of a certain class C, by a statement R rdf:type C.
	 * If there are two or more statements R rdf:type C, with the same URI for R (subject) and C (class), 
	 * the corresponding declared instance is considered to be non-unique.
	 * @return Current value of the Duplicate Instance Metric: 1 - (No. of Non-unique Instances / Total No. of Declared Instances)
	 */
	@Override
	public double metricValue() {
		return 1.0 - ((double)countNonUniqueInst / (double)countTotalInst);
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
