package eu.diachron.qualitymetrics.accessibility.interlinking;

import java.util.Set;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.technques.probabilistic.ResourceBaseURIOracle;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.semantics.DQM;

/**
 * @author Santiago Londoño
 * 
 * Measures the degree to which the resource is linked to external data providers, 
 * that is, refers to the detection of links that connect the resource to external data provided by another data sources.
 * 
 * Based on: [1] Hogan Aidan, Umbrich Jürgen. An empirical survey of Linked Data conformance. Section 5.2, 
 * Linking, Issue VI: Use External URIs (page 26).
 * 
 */
public class LinkExternalDataProviders implements QualityMetric {
	
	private final Resource METRIC_URI = DQM.LinksToExternalDataProvidersMetric;
	
	final static Logger logger = LoggerFactory.getLogger(LinkExternalDataProviders.class);
	
	/**
	 * MapDB database, used to persist the Map containing the instances found to be declared in the dataset
	 */
	private DB mapDB = DBMaker.newTempFileDB().closeOnJvmShutdown().deleteFilesAfterClose().make();
	
	/**
	* A set containing the of pay-level domains (or base URIs) found among all the data-level constants of the 
	* dataset (data level constants are: subjects of triples and objects of triples not subject to an rdf:type predicate)
	*/
	private Set<String> pSetPayLevelDomainURIs = mapDB.createHashSet("set-link-external-data-providers").make();
	
	/**
     * Object used to determine the base URI of the resource based on its contents
     */
	private ResourceBaseURIOracle baseURIOracle = new ResourceBaseURIOracle();
	
	/**
	 * Counts the total number of data-level constant URIs found among the triples of the resource
	 */
	private int totalDataLevelConstURIs = 0;

	/**
	 * Processes a single quad making part of the dataset. Determines whether the subject and/or object of the quad 
	 * are data-level URIs, if so, extracts their pay-level domain and adds them to the set of PLD URIs.
	 * @param quad Quad to be processed as part of the computation of the metric
	 */
	public void compute(Quad quad) {

		// Feed the base URI oracle, which will be used to determine the resource's base URI
		this.baseURIOracle.addHint(quad);

		// Extract the URIs of the subject and object of the quad
		String subjectURI = (quad.getSubject().isURI())?(quad.getSubject().getURI()):("");
		String objectURI = (quad.getObject().isURI())?(quad.getObject().getURI()):("");
		
		// Process subject URI
		if(!subjectURI.equals("")) {
			// Extract the PLD of the subject's URI and process it
			this.processPayLevelDomain(ResourceBaseURIOracle.extractPayLevelDomainURI(subjectURI));
			this.totalDataLevelConstURIs++;
		}
		
		// As this metric is defined to account for data-level URIs, check that the predicate is not rdf:type
		// (as per the definition of data-level constant provided by Hogan et al. [1], section 3.1.3, pg. 6)
		if(quad.getPredicate().isURI() && !quad.getPredicate().getURI().equals(RDF.type.getURI())) {
			
			// Process object URI
			if(!objectURI.equals("")) {
				// Extract the PLD of the object's URI and process it, a new data-level PLD has been found
				logger.trace("Data-level URI found in object: {} with predicate: {}", objectURI, quad.getPredicate().getURI());
				this.processPayLevelDomain(ResourceBaseURIOracle.extractPayLevelDomainURI(objectURI));
				this.totalDataLevelConstURIs++;
			}
		}
	}

	/**
	 * Compute the value of the metric as the ratio between the number of different PLDs found among the data-level 
	 * constants of the resource that are different of the resource's PLD and the total number of 
	 * data-level constant URIs found in the resource.
	 * @return value of the existence of links to external data providers metric computed on the current resource
	 */	
	public double metricValue() {
		
		// Determine the current resource's PLD
		String resBaseURI = this.baseURIOracle.getEstimatedResourceBaseURI();
		String resPLD = ResourceBaseURIOracle.extractPayLevelDomainURI(resBaseURI);
		
		// Count the number of external PLDs found in the resource
		int totalExtPLDs = 0;
		
		for(String curPLD : this.pSetPayLevelDomainURIs) {
			
			// If currently examined PLD does not belong to the resource's PLD (or vice-versa), the current PLD is external
			logger.debug("Comparing PLD in triples: {} with resource's PLD: {}", curPLD, resPLD);
			
			if(!(curPLD.contains(resPLD) || resPLD.contains(curPLD))) {
				logger.debug("OK, PLD {} is external", curPLD);
				totalExtPLDs++;
			}
		}
		
		if(totalDataLevelConstURIs > 0) {
			return (double)totalExtPLDs / (double)totalDataLevelConstURIs;
		} else {
			return 0.0;
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
	 * Verifies a PLD found in a subject or object and decides if must be added to the set of pay-level domain URIs
	 */
	private void processPayLevelDomain(String pld) {
		
		if(pld != null && pld.length() > 0) {
			// Add the PLD to the set, if it doesn't exist already
			this.pSetPayLevelDomainURIs.add(pld);
			logger.debug("PLD OK and added to set: {}", pld);
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		
		// Destroy persistent HashMap and the corresponding database
		try {
			if(this.mapDB != null && !this.mapDB.isClosed()) {
				this.mapDB.close();
			}
		} catch(Throwable ex) {
			logger.warn("Persistent HashMap or backing database could not be closed", ex);
		} finally {
			super.finalize();
		}
	}

}
