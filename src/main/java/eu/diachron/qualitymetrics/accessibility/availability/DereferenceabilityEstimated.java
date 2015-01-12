package eu.diachron.qualitymetrics.accessibility.availability;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.StatusLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.commons.bigdata.ReservoirSampler;
import de.unibonn.iai.eis.diachron.datatypes.StatusCode;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource.SerialisableHttpResponse;
import eu.diachron.qualitymetrics.cache.DiachronCacheManager;
import eu.diachron.qualitymetrics.utilities.HTTPRetriever;
import eu.diachron.semantics.vocabulary.DQM;

/**
 * @author Jeremy Debatista
 * 
 * This metric calculates an estimation of the number of valid redirects (303) or 
 * hashed links according to LOD Principles. Makes use of statistical sampling techniques 
 * to remain scalable to datasets of big-data proportions
 * 
 * Based on: <a href="http://www.hyperthing.org/">Hyperthing - A linked data Validator</a>
 * 
 * @see <a href="http://dl.dropboxusercontent.com/u/4138729/paper/dereference_iswc2011.pdf">
 * Dereferencing Semantic Web URIs: What is 200 OK on the Semantic Web? - Yang et al.</a>
 * 
 */
public class DereferenceabilityEstimated implements QualityMetric {
	
	private final Resource METRIC_URI = DQM.DereferenceabilityMetric;

	final static Logger logger = LoggerFactory.getLogger(DereferenceabilityEstimated.class);
	
	/**
	 * Constants controlling the maximum number of elements in the reservoir of Top-level Domains and 
	 * Fully Qualified URIs of each TLD, respectively
	 */
	private final int MAX_TLDS = 100;
	private final int MAX_FQURIS_PER_TLD = 10000;
	
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
	
	/**
	 * Processes each triple obtained from the dataset to be assessed (instance declarations, that is, 
	 * triples with predicate rdf:type are ignored). Identifies URIs appearing in both, the subject 
	 * and object of the triple and adds them to the set of URIs to be evaluated for dereferenceability
	 * @param quad Triple (in quad format) to be evaluated
	 */
	public void compute(Quad quad) {
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
				lstUrisToDeref.add(tld.uri);
			}
			
			// Dereference all TLD URIs
			List<DerefResult> lstDeRefTlds = this.deReferenceUris(lstUrisToDeref);
			
			long totalDerefUris = 0;
			long totalUris = 0;
			
			for(DerefResult curTldDeRefRes : lstDeRefTlds) {
				// Obtain the TLD corresponding to the URI whose result currently is being examined
				Tld derefTld = this.tldsReservoir.findItem(new Tld(curTldDeRefRes.uri));
				totalUris += ((derefTld.fqUris != null)?(derefTld.fqUris.size()):(0));
				
				// Only URIs comprised by dereferenceable TLDs are subject to be counted as successfully dereferenced
				if(curTldDeRefRes.isDeref && derefTld.fqUris != null) {
					// Dereference all URIs part of the TLD
					List<DerefResult> lstDeRefUris = this.deReferenceUris(derefTld.fqUris.getItems());
					
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
		// TODO Auto-generated method stub
		return null;
	}	
	
	/* ------------------------------------ Private Methods ------------------------------------------------ */
	
	/**
	 * Checks and properly processes an URI found as subject or object of a triple, adding it to the
	 * set of TLDs and fully-qualified URIs 
	 * @param uri URI to be processed
	 */
	private void addUriToDereference(String uri) {
		// Extract the top-level domain (a.k.a pay level domain) and look for it in the reservoir 
		String uriTLD = httpRetriever.extractTopLevelDomainURI(uri);
		Tld newTld = new Tld(uriTLD);		
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
		// Start the dereferenciation process, which will be run in parallel
		httpRetriever.addListOfResourceToQueue(uriSet);
		httpRetriever.start();
		
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
				
				if (this.isDereferenceable(httpResource)) {
					curUrlResult.isDeref = true;
					curUrlResult.isRdfXml = true;
				}
				if (httpResource.getDereferencabilityStatusCode() == StatusCode.SC200) {
					curUrlResult.isDeref = true;
					// Check if the resource contains RDF on XML
					if(this.is200AnRDF(httpResource)) {
						curUrlResult.isRdfXml = true;
					}
				}
				logger.trace("Resource fetched: {}. Deref. status: {}. Is RDF: {}", headUri, httpResource.getDereferencabilityStatusCode(), curUrlResult.isRdfXml);
			}
		}
		
		return lstDerefUris;
	}
	
	private boolean isDereferenceable(CachedHTTPResource httpResource){
		if (httpResource.getDereferencabilityStatusCode() == null){
			List<Integer> statusCode = this.getStatusCodes(httpResource.getStatusLines());
			
			if (httpResource.getUri().contains("#") && statusCode.contains(200)) httpResource.setDereferencabilityStatusCode(StatusCode.HASH);
			else if (statusCode.contains(200)){
				httpResource.setDereferencabilityStatusCode(StatusCode.SC200);
				if (statusCode.contains(303)) httpResource.setDereferencabilityStatusCode(StatusCode.SC303);
				else {
					if (statusCode.contains(301)) httpResource.setDereferencabilityStatusCode(StatusCode.SC301);
					if (statusCode.contains(302)) httpResource.setDereferencabilityStatusCode(StatusCode.SC302);
					if (statusCode.contains(307)) httpResource.setDereferencabilityStatusCode(StatusCode.SC303);
				}
			}
			
			if (has4xxCode(statusCode)) httpResource.setDereferencabilityStatusCode(StatusCode.SC4XX);
			if (has5xxCode(statusCode)) httpResource.setDereferencabilityStatusCode(StatusCode.SC5XX);
		} 			
		
		StatusCode scode = httpResource.getDereferencabilityStatusCode();
		return this.mapDerefStatusCode(scode);
		
	}
	
	private List<Integer> getStatusCodes(List<StatusLine> statusLines){
		ArrayList<Integer> codes = new ArrayList<Integer>();
		
		if(statusLines != null) {
			synchronized(statusLines) {
				for(StatusLine s : statusLines){
					codes.add(s.getStatusCode());
				}
			}
		}
		
		return codes;
	}
	
	private boolean mapDerefStatusCode(StatusCode statusCode){
		if(statusCode == null) {
			return false;
		} else {
			switch(statusCode){
				case SC303 : case HASH : return true;
				default : return false;
			}
		}
	}
	
	private boolean is200AnRDF(CachedHTTPResource resource) {
		if(resource != null && resource.getResponses() != null) {
			for (SerialisableHttpResponse response : resource.getResponses()) {
				if(response != null && response.getHeaders("Content-Type") != null) {
					if (response.getHeaders("Content-Type").equals("application/rdf+xml")) { 
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean has4xxCode(List<Integer> statusCode){
		for (int i : statusCode) {
			if ((i >= 400) && (i < 499))  return true; else continue;
		}
		return false;
	}
	
	private boolean has5xxCode(List<Integer> statusCode){
		for (int i : statusCode) {
			if ((i >= 500) && (i < 599))  return true; else continue;
		}
		return false;
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
	
	/**
	 * Structure representing a TLD, which includes its corresponding set of Fully Qualified URIs.
	 * Encapsulates the items that will be stored in the reservoir of TLDs
	 * @author slondono
	 */
	private class Tld {
		
		private String uri;
		private ReservoirSampler<String> fqUris;	// Set of fully qualified URIs part of the TLD
		
		private Tld(String uri) {
			this.uri = uri;
		}
		
		/**
		 * Adds a new fully-qualified URI to those comprised by the TLD, if it is not already there
		 * @param fqUri New fully-qualified URI to be added
		 * @return True if the URI was added, false otherwise (either because it's already registered or was discarded)
		 */
		private boolean addFqUri(String fqUri) {
			// Lazy initialization of reservoir containing fully-qualified URIs
			if(this.fqUris == null) {
				this.fqUris = new ReservoirSampler<String>(MAX_FQURIS_PER_TLD, true);
			}
			// Check that the URI is not already in the reservoir
			if(this.fqUris.findItem(fqUri) == null) {
				return this.fqUris.add(fqUri);
			}
			return false;
		}
		
		/**
		 * Returns the number of fully-qualified URIs comprised by this TLD
		 */
		private int countFqUris() {
			if(this.fqUris == null) {
				return 0;
			}
			return this.fqUris.size();
		}
		
		/**
		 * For performance purposes and in order to make instances of this class as lightweight as possible
		 * when stored in Hashed datastructures, use the URI to generate hash codes (which entails that URI 
		 * should be unique among all instances of Tld)
		 */
		@Override
		public int hashCode() {
			return this.uri.hashCode();
		}
		
		@Override
	    public boolean equals(Object obj) {
	       if (!(obj instanceof Tld))
	            return false;
	        if (obj == this)
	            return true;

	        return (this.uri.equals(((Tld)obj).uri));
	    }
	}
}
