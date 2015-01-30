package eu.diachron.qualitymetrics.accessibility.interlinking;

import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ResourceBaseURIOracle;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;

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
public class DereferencibilityBackLinks implements QualityMetric {
	
	private final Resource METRIC_URI = DQM.DereferenceabilityBackLinksMetric;
	
	final static Logger logger = LoggerFactory.getLogger(DereferencibilityBackLinks.class);
		
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
	 * MapDB database, used to persist the Map containing the instances found to be declared in the dataset
	 */
	private DB mapDB = MapDbFactory.createFilesystemDB();
	
	/**
	* A table holding the set of URIs recognized as parent URIs of the objects of all the processed triples.
	* The parent URI is obtained by taking the substring behined the last appearance of "/" in the object's URI. As values,
	* the table contains the number of times the parent URI set as key has appeared as part of the objects of the processed triples
	*/
	private HTreeMap<String, Integer> pTblObjectURIs = this.mapDB.createHashMap("deferencibility-back-links-map").make();
	
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
				Integer curParentURICount = this.pTblObjectURIs.get(parentURI);
				this.pTblObjectURIs.put(parentURI, ((curParentURICount != null)?(curParentURICount + 1):(1)));
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
		
			// Get the total number of inlinks (objects part of the resource base URI)
			Integer totalObjectsInBase = this.pTblObjectURIs.get(resourceBaseURI); 
			this.totalInlinkObjects = (totalObjectsInBase != null)?(totalObjectsInBase):(0);
			
			return ((double)this.totalInlinkObjects / (double)this.totalObjects);
		
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
	
	@Override
	protected void finalize() throws Throwable {
		
		// Destroy persistent HashMap and the corresponding database
		try {
			if(this.pTblObjectURIs != null) {
				this.pTblObjectURIs.close();
			}
			if(this.mapDB != null && !this.mapDB.isClosed()) {
				this.mapDB.close();
			}
		} catch(Throwable ex) {
			logger.warn("Persistent HashMap or backing database could not be closed", ex);
		} finally {
			try {
				super.finalize();
			} catch(Throwable ex) {
				logger.warn("Persistent HashMap or backing database could not be closed", ex);
			}
		}
	}

}
