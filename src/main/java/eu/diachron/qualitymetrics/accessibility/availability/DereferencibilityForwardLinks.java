package eu.diachron.qualitymetrics.accessibility.availability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.semantics.vocabulary.DQM;

/**
 * @author Santiago Londoño
 * 
 * This metric measures the extent to which a resource includes all triples from the dataset that have 
 * the resource's URI as the subject. This is the immediate description of the resource, as specified by 
 * Hogan et al. To do so, it computes the ratio between the number of subjects that are "forward-links" 
 * a.k.a "outlinks" (are part of the resource's URI) and the total number of subjects in the resource.
 * 
 * Based on: Hogan Aidan, Umbrich Jürgen. An empirical survey of Linked Data conformance.
 * 
 */
public class DereferencibilityForwardLinks implements QualityMetric {
	
	private final Resource METRIC_URI = DQM.DereferenceabilityForwardLinksMetric;
	
	final static Logger logger = LoggerFactory.getLogger(DereferencibilityForwardLinks.class);
		
	/**
	* Counter of the number of subjects found to be an URI in the resource. Note that differently to the original
	* specification given by Hogan et al, this count includes all subjects, not only data-level subjects.
	* Data-level subjects correspond to the definition of the ldlc(o) set, provided by Hogan et al. 
	*/
	private long totalSubjects = 0;
	
	/**
	 * Counter of the number of outlinks (subjects having the URI of the resource as prefix, 
	 * except subjects of typeof statements) found in the resource
	 */
	private long totalOutlinkSubjects = 0;
	
	/**
	* Object used to determine the base URI of the resource based on its contents and to count the number of 
	* subjects being part of it
	*/
	private ResourceBaseURIOracle baseURIOracle = new ResourceBaseURIOracle();
		
	/**
	 * Processes a single quad making part of the dataset. Extracts the subject of the quad and determines if it's part
	 * of the resource's URI, if so, the subject is deemed as an outlink. Counts the number of outlinks and subjects 
	 * contained in the set of quads contained into the resource
	 * @param quad Quad to be processed as part of the computation of the metric
	 */
	public void compute(Quad quad) {
		
		// Feed the base URI oracle, which will be used to determine the resource's base URI and to count outlinks
		this.baseURIOracle.addHint(quad);

		// Extract the URIs of current subject and of the resource, both are required to perform the computation
		String subjectURI = (quad.getSubject().isURI())?(quad.getSubject().getURI()):("");
		
		if(!subjectURI.equals("")) {
			// This is an URI subject...
			this.totalSubjects++;
		}
	}

	/**
	 * Compute the value of the metric as the ratio between the total amount of outlinks and the total amount of 
	 * data-level subjects in the resource (i.e. subjects of triples with predicate different to rdf:type)
	 * @return value of the dereferencibility forward links metric computed on the current resource
	 */
	public double metricValue() {
		
		// Determine the base URI of the resource
		String resourceBaseURI = this.baseURIOracle.getEstimatedResourceBaseURI();
		logger.debug("Estimated resource base URI: {}", resourceBaseURI);
		
		if(this.totalSubjects > 0 && resourceBaseURI != null) {
		
			// Get the total number of outlinks (subjects part of the resource base URI) 
			this.totalOutlinkSubjects = this.baseURIOracle.getBaseURICount();
			logger.debug("Total outlinks (subjects) in base URI: {}, vs. total subject URIs in resource: {}", this.totalOutlinkSubjects, this.totalSubjects);
			
			// Compute the ratio between the subjects that are actually part of the resource and the total of all subjects in the resource
			return ((double)this.totalOutlinkSubjects / (double)this.totalSubjects);
		} else {
			return 0;
		}
	}
	
	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	public ProblemList<?> getQualityProblems() {
		// TODO Auto-generated method stub
		return null;
	}

}
