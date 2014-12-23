package eu.diachron.qualitymetrics.intrinsic.conciseness;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.commons.bigdata.RLBSBloomFilter;
import de.unibonn.iai.eis.luzzu.assessment.ComplexQualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.semantics.vocabulary.DQM;

/**
 * @author Santiago Londono
 * Provides an estimate of the redundancy of the dataset at the data level, by computing an approximation 
 * for the Duplicate Instance metric, which is part of the Conciseness dimension. The results are approximated 
 * in the sense that, when determining if an instance declaration was previously observed in the dataset, 
 * false positives/negatives might occur.
 */
public class UsageUnambiguousAnnotationsEstimated implements ComplexQualityMetric {
	
	private static Logger logger = LoggerFactory.getLogger(UsageUnambiguousAnnotationsEstimated.class);
	
	private final Resource METRIC_URI = DQM.UsageUnambiguousAnnotationsMetric;
	
	/**
	 * Randomized Load-balanced Biased Sampling Bloom Filter, used as repository to find duplicate instance declarations
	 */
	private RLBSBloomFilter rlbsBloomFilterDupls = null;
	
	/**
	 * Counter for the number of instances violating the uniqueness rule. Counts the duplicated 
	 * instances only (e.g. if an instance is declared twice, the value of the counter will be 1).
	 */
	private int countNonUniqueInst = 0;
	
	/**
	 * Counter for the total number of declared instances (occurrences of the triple A rdf:type C).
	 */
	private int countTotalInst = 0;
	
	@Override
	public void before(Object... args) {
		// Bloom Filters are based on an array of bits, whose size must be defined at creation. Ideally this size should 
		// match the max. number of items to be put into the filter. If the caller doesn't provide that number, initialize
		// the filter to default size
		Integer approxNumTriples = 512000;
		int k = 11;
		
		// Check whether the approximated number of triples has been provided
		if(args != null && args.length > 0 && args[0] != null && !(args[0] instanceof Integer )) {
			approxNumTriples = (Integer)args[0];
		}
		
		this.rlbsBloomFilterDupls = new RLBSBloomFilter(k, approxNumTriples, 0.01);
		logger.debug("RLBS Bloom Filter initialized. Num. of hash functions: {}, Total filter memory (bits): {}", k, approxNumTriples);
	}

	/**
	 * Re-computes the value of the Duplicate Instance Metric, by considering a new quad provided.
	 * @param quad The new quad to be considered in the computation of the metric. Must be not null.
	 */
	@Override
	public void compute(Quad quad) {
		// Check whether current triple corresponds to an instance declaration
		logger.trace("Computing triple with predicate: {}", quad.getPredicate().getURI());
		Node predicateEdge = quad.getPredicate();
		
		// Determines whether the specified predicate corresponds to an instance declaration 
		// statement, that is, whether the statement is of the form: Instance rdf:type Class
		if(predicateEdge != null && predicateEdge.isURI() && predicateEdge.hasURI(RDF.type.getURI())) {
			// Build the Id of the instance, concatenating the instance's (subject) URI and the URI of its class
			String newInstanceId = quad.getSubject().toString() + "||" + quad.getObject().toString();
			logger.trace("Instance declared: {}", newInstanceId);
			
			// Check if the instance already exists in the repository filter
			if(this.rlbsBloomFilterDupls.checkDuplicate(newInstanceId)) {
				// Instance was already declared, increase the number of appearances and the non-unique counter
				this.countNonUniqueInst++;
				logger.trace("Non-unique instance found. ID: {}", newInstanceId);
			}

			this.countTotalInst++;
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
		return 1.0 - ((double)this.countNonUniqueInst / (double)this.countTotalInst);
	}

	@Override
	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void after(Object... arg0) {
		// TODO Auto-generated method stub
	}
}
