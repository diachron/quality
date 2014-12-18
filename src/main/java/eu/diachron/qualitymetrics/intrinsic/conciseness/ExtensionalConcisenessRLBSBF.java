package eu.diachron.qualitymetrics.intrinsic.conciseness;

import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.commons.bigdata.RLBSBloomFilter;
import de.unibonn.iai.eis.luzzu.assessment.ComplexQualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.semantics.vocabulary.DQM;

/**
 * @author Santiago Londono
 * Implementation of the Extensional Conciseness metric by means of the Randomized Load-balanced Biased Sampling
 * Bloom Filter, as proposed by Bera et al. [bera12]. This algorithm is designed to scale well for huge amounts 
 * of data, but providing approximate results
 */
public class ExtensionalConcisenessRLBSBF implements ComplexQualityMetric {
	
	private static Logger logger = LoggerFactory.getLogger(ExtensionalConcisenessRLBSBF.class);
	
	private final Resource METRIC_URI = DQM.ExtensionalConcisenessMetric;
	
	// Randomized Load-balanced Biased Sampling Bloom Filter, used to find duplicate instance declarations
	private RLBSBloomFilter rlbsBloomFilterDupls = null;
	
	// Subject URI of the instance being currently built
	private String currentSubjectURI = null;
	
	// Set of predicates-values of the instance being currently built
	private SortedSet<String> currentStatements = null;
	
	// Estimated number of duplicates
	private Long estimatedDuplInstances = new Long(0);
	private Long totalCreatedInstances = new Long(0);
	
	@Override
	public void before(Object... args) {
		// Bloom Filters are based on an array of bits, whose size must be defined at creation. Ideally this size should 
		// match the max. number of items to be put into the filter. If the caller doesn't provide that number, initialize
		// the filter to default size
		Integer approxNumTriples = 512000;
		
		// Check whether the approximated number of triples has been provided
		if(args != null && args.length > 0 && args[0] != null && !(args[0] instanceof Integer )) {
			approxNumTriples = (Integer)args[0];
		}
		
		this.rlbsBloomFilterDupls = new RLBSBloomFilter(11, approxNumTriples, 0.01);
	}
	
	/**
	 * Re-computes the value of the Extensional Conciseness Metric, by considering a new quad provided.
	 * @param quad The new quad to be considered in the computation of the metric. Must be not null.
	 */
	public void compute(Quad quad) {
		// Every time a new quad is considered, get the URI of the subject
		String subjectURI = quad.getSubject().getURI();
		String predicateURI = ((quad.getPredicate() != null)?(quad.getPredicate().toString()):(""));
		String objectValue = ((quad.getObject() != null)?(quad.getObject().toString()):(""));;
		
		// Serializate statement
		String statement = predicateURI + " " + objectValue;
		
		// Check if the same object is being processed
		if(!subjectURI.equals(this.currentSubjectURI)) {
			
			// Check if subject just finished being created is in the filter
			if(this.currentStatements != null) {
				
				if(this.rlbsBloomFilterDupls.checkDuplicate(this.currentStatements.toString())) {
					// Bits didn't changed, it might be a false positive, but most likely it's duplicated
					this.estimatedDuplInstances++;
					logger.debug("Duplicate instance definition detected, subject URI: {}", this.currentSubjectURI);
				}
			}
			
			// A new instance is to be created
			this.currentStatements = new TreeSet<String>();
			this.currentSubjectURI = subjectURI;
			this.totalCreatedInstances++;
			logger.debug("Creating new instance with subject: " + this.currentSubjectURI);
		}
		
		this.currentStatements.add(statement);
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
		return ((double)(this.totalCreatedInstances - this.estimatedDuplInstances))/((double)this.totalCreatedInstances);
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
