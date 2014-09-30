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
 * the resource's URI as the object. This allows browsers and crawlers to traverse links in either direction, 
 * as specified by Hogan et al. To do so, it computes the ratio between the number of objects that are 
 * "back-links" a.k.a "inlinks" (are part of the resource's URI) and the total number of objects in the resource.
 * 
 * Based on: Hogan Aidan, Umbrich Jürgen. An empirical survey of Linked Data conformance.
 * 
 */
public class DereferencibilityBackLinks {
	
	private final Resource METRIC_URI = DQM.DereferenceabilityBackLinksMetric;
	
	final static Logger logger = LoggerFactory.getLogger(DereferencibilityBackLinks.class);
	
	/**
	 * URI of the dataset being processed
	 */
	private String datasetURI = null;
	
	/**
	 * Counter of the number of data-level objects found to be an URI in the resource.
	 * Data-level objects correspond to the definition of the ldlc(o) set, provided by Hogan et al. 
	 */
	private long totalDataLvlObjects = 0;
	
	/**
	 * Counter of the number of inlinks (objects having the URI of the resource as prefix, 
	 * except objects of typeof statements) found in the resource
	 */
	private long totalInlinkObjects = 0;
	
	/**
	 * Processes a single quad making part of the dataset. Extracts the object of the quad and determines if it's part
	 * of the resource's URI, if so, the object is deemed as an inlink. Counts the number of inlinks and objects  
	 * contained in the set of quads contained into the resource
	 * @param quad Quad to be processed as part of the computation of the metric
	 */
	public void compute(Quad quad) {
		
		// rdfs:type statements are not considered in this computation (as specified in Hogan et al.)
		if(quad.getPredicate().isURI() && quad.getPredicate().getURI().equals(RDF.type.getURI())) {
			return;						
		}
		
		// Extract the URIs of current object and of the resource, both are required to perform the computation
		String objectURI = (quad.getObject().isURI())?(quad.getObject().getURI()):("");
		String datasetURI = this.getDatasetURI(); 
		
		if(datasetURI != null && !datasetURI.equals("") && !objectURI.equals("")) {
			
			// This is a data-level object...
			this.totalDataLvlObjects++;
			
			// ...now determine whether its an inlink on this resource
			if(objectURI.startsWith(datasetURI)) {
				// Is an inlink, as the object URI is part of the resource's URI (i.e. is locally minted)
				this.totalInlinkObjects++;
			}
		}
	}
	
	/**
	 * Compute the value of the metric as the ratio between the total amount of inlinks and the total amount of 
	 * data-level objects in the resource (i.e. objects of triples with predicate different to rdf:type)
	 * @return value of the dereferencibility back links metric computed on the current resource
	 */	
	public double metricValue() {
		
		if(this.totalDataLvlObjects != 0) {
			return ((double)this.totalInlinkObjects / (double)this.totalDataLvlObjects);
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
