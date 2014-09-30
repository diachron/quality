package eu.diachron.qualitymetrics.accessibility.availability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
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
public class DereferencibilityForwardLinks {
	
	private final Resource METRIC_URI = DQM.DereferenceabilityForwardLinksMetric;
	
	final static Logger logger = LoggerFactory.getLogger(DereferencibilityForwardLinks.class);
	
	/**
	 * URI of the dataset being processed
	 */
	private String datasetURI = null;
	
	/**
	 * Counter of the number of data-level subjects found to be an URI in the resource.
	 * Data-level subjects correspond to the definition of the ldlc(o) set, provided by Hogan et al. 
	 */
	private long totalDataLvlSubjects = 0;
	
	/**
	 * Counter of the number of outlinks (subjects having the URI of the resource as prefix, 
	 * except subjects of typeof statements) found in the resource
	 */
	private long totalOutlinkSubjects = 0;
		
	/**
	 * Processes a single quad making part of the dataset. Extracts the subject of the quad and determines if it's part
	 * of the resource's URI, if so, the subject is deemed as an outlink. Counts the number of outlinks and subjects 
	 * contained in the set of quads contained into the resource
	 * @param quad Quad to be processed as part of the computation of the metric
	 */
	public void compute(Quad quad) {
		
		// rdfs:type statements are not considered in this computation (as specified in Hogan et al.)
		if(quad.getPredicate().isURI() && quad.getPredicate().getURI().equals(RDF.type.getURI())) {
			return;						
		}
		
		// Extract the URIs of current subject and of the resource, both are required to perform the computation
		String subjectURI = (quad.getSubject().isURI())?(quad.getSubject().getURI()):("");
		String datasetURI = this.getDatasetURI(); 
		
		if(datasetURI != null && !datasetURI.equals("") && !subjectURI.equals("")) {
			
			// This is a data-level subject...
			this.totalDataLvlSubjects++;
			
			// ...now determine whether its an outlink on this resource
			if(subjectURI.startsWith(datasetURI)) {
				// Is an outlink, as the subject URI is part of the resource's URI (i.e. is locally minted)
				this.totalOutlinkSubjects++;
			}
		}
	}

	/**
	 * Compute the value of the metric as the ratio between the total amount of outlinks and the total amount of 
	 * data-level subjects in the resource (i.e. subjects of triples with predicate different to rdf:type)
	 * @return value of the dereferencibility forward links metric computed on the current resource
	 */
	public double metricValue() {
		
		if(this.totalDataLvlSubjects != 0) {
			return ((double)this.totalOutlinkSubjects / (double)this.totalDataLvlSubjects);
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
	
	/**
	 * Obtains the URI of the resource being currently processed. 
	 * @return URI of the resource being processed
	 */
	private String getDatasetURI() {
		
		// Check if the URI of the resource being assessed has been determined already, if no, load it from the environment
		if(this.datasetURI == null) {
			try {
				datasetURI = EnvironmentProperties.getInstance().getDatasetURI();
			} catch(Exception ex) {
				logger.error("Error retrieving dataset URI, processor not initialised yet", ex);
			}
		}
		
		return this.datasetURI;
	}

}
