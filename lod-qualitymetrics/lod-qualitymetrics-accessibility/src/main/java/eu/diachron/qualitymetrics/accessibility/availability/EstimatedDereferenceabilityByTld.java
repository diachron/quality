package eu.diachron.qualitymetrics.accessibility.availability;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.datatypes.Tld;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.semantics.DQMPROB;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ReservoirSampler;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.accessibility.availability.helper.Dereferencer;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.DiachronCacheManager;
import eu.diachron.qualitymetrics.utilities.HTTPRetriever;

/**
 * @author Jeremy Debatista
 * 
 * This metric calculates an estimation of the number of valid redirects (303) or 
 * hashed links according to LOD Principles. Makes use of statistical sampling 
 * techniques to remain scalable to datasets of big-data proportions
 * 
 * Based on: <a href="http://www.hyperthing.org/">Hyperthing - A linked data Validator</a>
 * 
 * @see <a href="http://dl.dropboxusercontent.com/u/4138729/paper/dereference_iswc2011.pdf">
 * Dereferencing Semantic Web URIs: What is 200 OK on the Semantic Web? - Yang et al.</a>
 * 
 */
public class EstimatedDereferenceabilityByTld implements QualityMetric {
	
	private final Resource METRIC_URI = DQM.DereferenceabilityMetric;

	final static Logger logger = LoggerFactory.getLogger(EstimatedDereferenceability.class);
	
	/**
	 * Constants controlling the maximum number of elements in the reservoir of Top-level Domains and 
	 * Fully Qualified URIs of each TLD, respectively
	 */
	public int MAX_TLDS = 100;
	public int MAX_FQURIS_PER_TLD = 1000;
	
	/**
	 * Performs HTTP requests, used to try to fetch identified URIs
	 */
	private HTTPRetriever httpRetriever = new HTTPRetriever();
	
	/**
	 * Holds the set of dereferenceable top-level domains found among the subjects and objects of the triples,
	 * as a reservoir sampler, if its number of items grows beyond the limit (MAX_TLDS) items will be replaced 
	 * randomly upon forthcoming insertions. Moreover, the items will be indexed so that search operations are O(1)
	 */
	private ReservoirSampler<Tld> tldsReservoir = new ReservoirSampler<Tld>(MAX_TLDS, true);

	private DiachronCacheManager dcmgr = DiachronCacheManager.getInstance();

	private double metricValue = 0.0;
	private boolean metricCalculated = false;
	
	private List<Quad> _problemList = new ArrayList<Quad>();

	
	/**
	 * Processes each triple obtained from the dataset to be assessed (instance declarations, that is, 
	 * triples with predicate rdf:type are ignored). Identifies URIs appearing in both, the subject 
	 * and object of the triple and adds them to the set of URIs to be evaluated for dereferenceability
	 * @param quad Triple (in quad format) to be evaluated
	 */
	public void compute(Quad quad) {
		logger.debug("Assessing {}", quad.asTriple().toString());

		
		// we are currently ignoring triples ?s a ?o
		if (!(quad.getPredicate().getURI().equals(RDF.type.getURI()))){ 
			
			String subject = quad.getSubject().toString();
			if (httpRetriever.isPossibleURL(subject)) {
				logger.trace("URI found on subject: {}", subject);
				addUriToDereference(subject);
			}

			String object = quad.getObject().toString();
			if (httpRetriever.isPossibleURL(object)) {
				logger.trace("URI found on object: {}", object);
				addUriToDereference(object);
			}
		}
	}

	/**
	 * Initiates the dereferencing process of some of the URIs identified in the dataset, chosen in accordance 
	 * with a statistical sampling method, in order to compute the estimated dereferenceability of the whole dataset 
	 * @return estimated dereferencibility, computed as aforementioned
	 */
	public double metricValue() {
		
		if(!this.metricCalculated) {
			// Collect the list of URIs of the TLDs, to be dereferenced
			List<String> lstUrisToDeref = new ArrayList<String>(this.tldsReservoir.size());			
			for(Tld tld : this.tldsReservoir.getItems()) {
				lstUrisToDeref.add(tld.getUri());
			}
			
			// Dereference all TLD URIs
			List<DerefResult> lstDeRefTlds = this.deReferenceUris(lstUrisToDeref);
			
			long totalDerefUris = 0;
			long totalUris = 0;
			
			for(DerefResult curTldDeRefRes : lstDeRefTlds) {
				// Obtain the TLD corresponding to the URI whose result currently is being examined
				Tld derefTld = this.tldsReservoir.findItem(new Tld(curTldDeRefRes.uri, MAX_FQURIS_PER_TLD));
				totalUris += ((derefTld.getfqUris() != null)?(derefTld.getfqUris().size()):(0));
				
				// Only URIs comprised by dereferenceable TLDs are subject to be counted as successfully dereferenced
				if(curTldDeRefRes.isDeref && derefTld.getfqUris() != null) {
					// Dereference all URIs part of the TLD
					List<DerefResult> lstDeRefUris = this.deReferenceUris(derefTld.getfqUris().getItems());
					
					// Count those successfully dereferenced
					for(DerefResult curUriDeRefRes : lstDeRefUris) {						
						if(curUriDeRefRes.isDeref && curUriDeRefRes.isRdfXml) {
							logger.debug("-- URI successfully dereferenced: {}", curUriDeRefRes.uri);
							totalDerefUris += 1;
						} else {
							logger.debug("-- URI: {} failed to be dereferenced", curUriDeRefRes.uri);							
						}
					}
					logger.debug("TLD: {} successfully dereferenced, sampling from: {} URIs", curTldDeRefRes.uri, derefTld.countFqUris());
				} else {
					logger.debug("TLD: {} non-dereferenced", curTldDeRefRes.uri);
				}
			}
			
			this.metricValue = (double)totalDerefUris / (double)totalUris;
		}
		
		return this.metricValue;
	}
	
	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	public ProblemList<?> getQualityProblems() {
		ProblemList<Quad> pl = null;
		try {
			if(this._problemList != null && this._problemList.size() > 0) {
				pl = new ProblemList<Quad>(this._problemList);
			} else {
				pl = new ProblemList<Quad>();
			}
		} catch (ProblemListInitialisationException e) {
			logger.error(e.getMessage());
		}
		return pl;
	}
	
	/* ------------------------------------ Private Methods ------------------------------------------------ */
	
	/**
	 * Checks and properly processes an URI found as subject or object of a triple, adding it to the
	 * set of TLDs and fully-qualified URIs 
	 * @param uri URI to be processed
	 */
	private void addUriToDereference(String uri) {
		// Extract the top-level domain and look for it within the reservoir 
		String uriTLD = httpRetriever.extractTopLevelDomainURI(uri);
		Tld newTld = new Tld(uriTLD, MAX_FQURIS_PER_TLD);		
		Tld foundTld = this.tldsReservoir.findItem(newTld);
		
		if(foundTld == null) {
			logger.trace("New TLD found and recorded: {}...", uriTLD);
			// Add the new TLD to the reservoir
			this.tldsReservoir.add(newTld);
			// Add new fully qualified URI to those of the new TLD
			newTld.addFqUri(uri);
		} else {
			// The identified TLD was found, it already exists on the reservoir, just add the fqdn to it
			foundTld.addFqUri(uri);
		}
	}
	
	/**
	 * Tries to dereference all the URIs contained in the parameter, by retrieving them from the cache. URIs
	 * not found in the cache are added to the queue containing the URIs to be fetched by the async HTTP retrieval process
	 * @param uriSet Set of URIs to be dereferenced
	 * @return list with the results of the dereferenceability operations, for those URIs that were found in the cache 
	 */
	private List<DerefResult> deReferenceUris(List<String> uriSet) {
		// Start the dereferencing process, which will be run in parallel
		httpRetriever.addListOfResourceToQueue(uriSet);
		httpRetriever.start(true);
		
		List<DerefResult> lstDerefUris = new ArrayList<DerefResult>();
		List<String> lstToDerefUris = new ArrayList<String>(uriSet);
				
		// Dereference each and every one of the URIs contained in the specified set
		while(lstToDerefUris.size() > 0) {
			// Remove the URI at the head of the queue of URIs to be dereferenced                
			String headUri = lstToDerefUris.remove(0);
			
			// First, search for the URI in the cache
			CachedHTTPResource httpResource = (CachedHTTPResource)dcmgr.getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, headUri);
			
			if (httpResource == null || httpResource.getStatusLines() == null) {
				// URIs not found in the cache, is still to be fetched via HTTP, add it to the end of the list
				lstToDerefUris.add(headUri);
			} else {
				// URI found in the cache (which means that was fetched at some point), check if successfully dereferenced
				DerefResult curUrlResult = new DerefResult(headUri, false, false);
				lstDerefUris.add(curUrlResult);
				
				if (Dereferencer.hasValidDereferencability(httpResource)) {
				} else {
					this.createProblemQuad(httpResource.getUri(), DQMPROB.SC303WithoutParsableContent);
				}
				logger.trace("Resource fetched: {}. Deref. status: {}. Is RDF: {}", headUri, httpResource.getDereferencabilityStatusCode(), curUrlResult.isRdfXml);
			}
		}
		
		return lstDerefUris;
	}
	
	
	/**
	 * Inner class, with the purpose of coupling an URI with the result of its dereferencing process
	 * It's basically a pair establishing a relation between an URI and its dereferenceability
	 * @author slondono
	 */
	private class DerefResult {
		
		private String uri;
		private boolean isDeref;
		private boolean isRdfXml;
		
		private DerefResult(String uri, boolean isDeref, boolean isRdfXml) {
			this.uri = uri;
			this.isDeref = isDeref;
			this.isRdfXml = isRdfXml;
		}
	}
			
//	public static int getMAX_TLDS() {
//		return MAX_TLDS;
//	}
//
//	public static void setMAX_TLDS(int mAX_TLDS) {
//		MAX_TLDS = mAX_TLDS;
//	}
//
//	public static int getMAX_FQURIS_PER_TLD() {
//		return MAX_FQURIS_PER_TLD;
//	}
//
//	public static void setMAX_FQURIS_PER_TLD(int mAX_FQURIS_PER_TLD) {
//		MAX_FQURIS_PER_TLD = mAX_FQURIS_PER_TLD;
//	}
	
	private void createProblemQuad(String resource, Resource problem){
		Quad q = new Quad(null, ModelFactory.createDefaultModel().createResource(resource).asNode(), QPRO.exceptionDescription.asNode(), problem.asNode());
		this._problemList.add(q);
	}
	


	
	@Override
	public boolean isEstimate() {
		return true;
	}

	@Override
	public Resource getAgentURI() {
		return 	DQM.LuzzuProvenanceAgent;
	}
}