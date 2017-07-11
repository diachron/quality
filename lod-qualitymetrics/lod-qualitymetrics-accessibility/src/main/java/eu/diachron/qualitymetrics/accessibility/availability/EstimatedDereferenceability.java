package eu.diachron.qualitymetrics.accessibility.availability;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.datatypes.StatusCode;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.semantics.DQMPROB;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ReservoirSampler;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.accessibility.availability.helper.Dereferencer;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.DiachronCacheManager;
import eu.diachron.qualitymetrics.utilities.AbstractQualityMetric;
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
public class EstimatedDereferenceability extends AbstractQualityMetric {
	
	private final Resource METRIC_URI = DQM.DereferenceabilityMetric;

	final static Logger logger = LoggerFactory.getLogger(EstimatedDereferenceability.class);
	
	/**
	 * Constants controlling the maximum number of elements in the reservoir of URIs, i.e. sample size
	 */
	public int MAX_FQURIS = 6000; //static
	
	/**
	 * Performs HTTP requests, used to try to fetch identified URIs
	 */
	private HTTPRetriever httpRetriever = new HTTPRetriever();

	
	/**
	 * Holds the set of dereferenceable top-level domains found among the subjects and objects of the triples,
	 * as a reservoir sampler, if its number of items grows beyond the limit (MAX_TLDS) items will be replaced 
	 * randomly upon forthcoming insertions. Moreover, the items will be indexed so that search operations are O(1)
	 */
	private ReservoirSampler<String> fqUrisReservoir = new ReservoirSampler<String>(MAX_FQURIS, true);

	private DiachronCacheManager dcmgr = DiachronCacheManager.getInstance();

	private long totalUris = 0;
	private long totalDerefUris = 0;
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
		logger.debug("Computing : {} ", quad.asTriple().toString());
		
		// we are currently ignoring triples ?s a ?o
		if (!(quad.getPredicate().getURI().equals(RDF.type.getURI()))){ 
			
			String subject = quad.getSubject().toString();
			if (httpRetriever.isPossibleURL(subject)) {
				// Check also, that the URI has not been already added
				if(this.fqUrisReservoir.findItem(subject) == null) {
					boolean uriAdded = this.fqUrisReservoir.add(subject);
					logger.trace("URI found on subject: {}, was added to reservoir? {}", subject, uriAdded);
				}
			}

			String object = quad.getObject().toString();
			if (httpRetriever.isPossibleURL(object)) {
				// Check also, that the URI has not been already added
				if(this.fqUrisReservoir.findItem(object) == null) {
					boolean uriAdded = this.fqUrisReservoir.add(object);
					logger.trace("URI found on object: {}, was added to reservoir? {}", object, uriAdded);
				}
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
			// Collect the list of URIs to be dereferenced. The reservoir contains a random sample of the 
			// whole population of URIs found in the data resource
			List<String> lstUrisToDeref = new ArrayList<String>(this.fqUrisReservoir.size());			
			for(String fqUri : this.fqUrisReservoir.getItems()) {
				lstUrisToDeref.add(fqUri);
			}
			
			// Dereference all URIs
			this.totalUris = lstUrisToDeref.size();
			this.totalDerefUris = this.deReferenceUris(lstUrisToDeref);

			this.metricValue = (double)totalDerefUris / (double)totalUris;
		}
				
		statsLogger.info("EstimatedDereferenceability. Dataset: {} - Total # URIs : {}; # Dereferenced URIs : {}; Previously calculated : {}", 
				EnvironmentProperties.getInstance().getDatasetURI(), this.totalUris, this.totalDerefUris, metricCalculated);
		
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
	
	
	/**
	 * Tries to dereference all the URIs contained in the parameter, by retrieving them from the cache. URIs
	 * not found in the cache are added to the queue containing the URIs to be fetched by the async HTTP retrieval process
	 * @param uriSet Set of URIs to be dereferenced
	 * @return total number of URIs that were successfully dereferenced
	 */
	private long deReferenceUris(List<String> uriSet) {
		// Start the dereferenciation process, which will be run in parallel
		httpRetriever.addListOfResourceToQueue(uriSet);
		httpRetriever.start(true);
		
		List<String> lstToDerefUris = new ArrayList<String>(uriSet);
		long totalDerefUris = 0;
				
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
				if (Dereferencer.hasValidDereferencability(httpResource)) {
					totalDerefUris++;
				}
				
				createProblemReport(httpResource);
				logger.trace("{} - {} - {}", headUri, httpResource.getStatusLines(), httpResource.getDereferencabilityStatusCode());
			}
		}
		
		return totalDerefUris;
	}
	
	private void createProblemReport(CachedHTTPResource httpResource){
		StatusCode sc = httpResource.getDereferencabilityStatusCode();
		
		switch (sc){
			case SC200 : this.createProblemQuad(httpResource.getUri(), DQMPROB.SC200OK); break;
			case SC301 : this.createProblemQuad(httpResource.getUri(), DQMPROB.SC301MovedPermanently); break;
			case SC302 : this.createProblemQuad(httpResource.getUri(), DQMPROB.SC302Found); break;
			case SC307 : this.createProblemQuad(httpResource.getUri(), DQMPROB.SC307TemporaryRedirectory); break;
			case SC3XX : this.createProblemQuad(httpResource.getUri(), DQMPROB.SC3XXRedirection); break;
			case SC4XX : this.createProblemQuad(httpResource.getUri(), DQMPROB.SC4XXClientError); break;
			case SC5XX : this.createProblemQuad(httpResource.getUri(), DQMPROB.SC5XXServerError); break;
			case SC303 : if (!httpResource.isContentParsable())  this.createProblemQuad(httpResource.getUri(), DQMPROB.SC303WithoutParsableContent); break;
			default	   : break;
		}
	}
	
	private void createProblemQuad(String resource, Resource problem){
		Quad q = new Quad(null, ModelFactory.createDefaultModel().createResource(resource).asNode(), QPRO.exceptionDescription.asNode(), problem.asNode());
		this._problemList.add(q);
	}
	
	public int getMAX_FQURIS() {
		return MAX_FQURIS;
	}

	public void setMAX_FQURIS(int mAX_FQURIS) {
		MAX_FQURIS = mAX_FQURIS;
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
