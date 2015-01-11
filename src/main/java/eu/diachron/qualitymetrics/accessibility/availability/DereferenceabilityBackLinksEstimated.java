package eu.diachron.qualitymetrics.accessibility.availability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.commons.bigdata.ReservoirSampler;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
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
public class DereferenceabilityBackLinksEstimated implements QualityMetric {
	
	private final Resource METRIC_URI = DQM.DereferenceabilityBackLinksMetric;
	
	final static Logger logger = LoggerFactory.getLogger(DereferenceabilityBackLinksEstimated.class);
		
	/**
	 * Counter of the number of objects found to be an URI in the resource. Note that differently to the original
	 * specification given by Hogan et al, this count includes all objects, not only data-level objects.
	 * Data-level objects correspond to the definition of the ldlc(o) set, provided by Hogan et al. 
	 */
	private long totalObjects = 0;
	
	/**
	 * Counter of the number of inlinks (objects having the URI of the resource as prefix, 
	 * except objects of typeof statements) found in the resource
	 */
	private long totalInlinkObjects = 0;
		
	/**
	* A reservoir sampler, holding the set of URIs recognized as parent URIs of the objects of all the processed triples.
	* The parent URI is obtained by taking the substring behind the last appearance of "/" in the object's URI. Items in the 
 	* reservoir also contain the number of times the parent URI has appeared as part of the objects of the processed triples
	*/
	private ReservoirSampler<ParentUri> reservoirObjectURIs = new ReservoirSampler<ParentUri>(100000, true);
	
    /**
     * Object used to determine the base URI of the resource based on its contents and to count the number of 
     * subjects being part of it
     */
	private ResourceBaseURIOracle baseURIOracle = new ResourceBaseURIOracle();
	
	/**
	 * Processes a single quad making part of the dataset. Extracts the object of the quad and determines if it's part
	 * of the resource's URI, if so, the object is deemed as an inlink. Counts the number of inlinks and objects  
	 * contained in the set of quads contained into the resource
	 * @param quad Quad to be processed as part of the computation of the metric
	 */
	public void compute(Quad quad) {
		
		// Feed the base URI oracle, which will be used to determine the resource's base URI and to count outlinks
		this.baseURIOracle.addHint(quad);

		// Extract the URI of the object of the quad, which is required to perform the computation
		String objectURI = (quad.getObject().isURI())?(quad.getObject().getURI()):("");

		if(!objectURI.equals("")) {
			
			// This is an URI object...
			this.totalObjects++;
			
			// Determine the parent URI of the object's URI
			String parentURI = this.baseURIOracle.extractParentURI(quad.getObject());
			logger.debug("Object URI detected: {} Parent URI: {}", objectURI, parentURI);
			
			// Add the parent URI to the table of objects or update the corresponding entry if it's already there
			if(parentURI != null) {			
				// Check if the current parent URI has already an entry in the table, if no, add it
				ParentUri newParentUri = new ParentUri(parentURI);
				ParentUri foundParentUri = this.reservoirObjectURIs.findItem(newParentUri);
				
				if(foundParentUri != null) {
					// Parent URI was previously recognized, it's in the reservoir
					foundParentUri.numOccurrences++;
					logger.debug("Processing parent URI: {}, item found in reservoir with {} occurrences", parentURI, foundParentUri.numOccurrences);
				} else {
					// Add the new parent URI to the reservoir
					this.reservoirObjectURIs.add(newParentUri);
					logger.debug("Processing parent URI: {}, new item added to reservoir", parentURI);
				}
			}
		}
	}
	
	/**
	 * Compute the value of the metric as the ratio between the total amount of inlinks and the total amount of 
	 * data-level objects in the resource (i.e. objects of triples with predicate different to rdf:type)
	 * @return value of the dereferencibility back links metric computed on the current resource
	 */	
	public double metricValue() {

		// Determine the base URI of the resource
		String resourceBaseURI = this.baseURIOracle.getEstimatedResourceBaseURI();

		if(resourceBaseURI != null && this.totalObjects != 0) {
		
			// Look for the base URI among the parent URIs in the reservoir
			ParentUri baseParentUri = this.reservoirObjectURIs.findItem(new ParentUri(resourceBaseURI));
			logger.debug("Resource base URI: {}, found in reservoir: {}", resourceBaseURI, ((baseParentUri != null)?(baseParentUri.numOccurrences):("-")));
						
			if(baseParentUri != null) {
				// Determine the appropriate base for the ratio computation, if the reservoir discarded any items
				if(this.reservoirObjectURIs.isFull()) {
					this.totalObjects = 0;
					for(ParentUri pUri : this.reservoirObjectURIs.getItems()) {
						this.totalObjects += pUri.numOccurrences;
					}
					logger.debug("Reservoir full, computed adjusted total number of objects: {}", this.totalObjects);
				}
				
				// Get the total number of inlinks (objects part of the resource base URI)
				this.totalInlinkObjects = baseParentUri.numOccurrences;
				
				return ((double)this.totalInlinkObjects / (double)this.totalObjects);
			} else {
				// Since the reservoir starts to randomly discard items when its full, it's possible that the base resource is
				// removed during computation (with a probability indirectly proportional to the size of the reservoir)
				logger.debug("Resource base URI not found in the reservoir, metric value cannot be computed");
				return 0.0;
			}
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
	 * Represents an URI, that was recognized as parent of the URI of an object found within the processed triples. 
	 * Instances of this class are intended to be stored in the reservoir sampler used by the metric
	 * @author slondono
	 */
	private class ParentUri {
		
		// The parent URI is obtained by taking the substring behind the last appearance of "/" in the object's URI
		private String parentUri;
		// Number of times the parent URI has appeared as part of the objects in the processed triples
		private int numOccurrences;
		
		private ParentUri(String parentUri) {
			this.parentUri = parentUri;
			this.numOccurrences = 1;
		}
		
		/**
		 * For performance purposes and in order to make instances of this class as lightweight as possible
		 * when stored in Hashed datastructures, use the URI to generate hash codes (which entails that URI 
		 * should be unique among all instances of Tld)
		 */
		@Override
		public int hashCode() {
			return this.parentUri.hashCode();
		}
		
		@Override
	    public boolean equals(Object obj) {
	       if (!(obj instanceof ParentUri))
	            return false;
	        if (obj == this)
	            return true;

	        return (this.parentUri.equals(((ParentUri)obj).parentUri));
	    }				
	}

}
