package eu.diachron.qualitymetrics.accessibility.availability;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.datatypes.StatusCode;
import de.unibonn.iai.eis.diachron.datatypes.Tld;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ReservoirSampler;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.accessibility.availability.helper.Dereferencer;
import eu.diachron.qualitymetrics.accessibility.availability.helper.ModelParser;
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
public class EstimatedDereferenceability implements QualityMetric {
	
	private final Resource METRIC_URI = DQM.DereferenceabilityMetric;

	final static Logger logger = LoggerFactory.getLogger(EstimatedDereferenceability.class);
	
	/**
	 * Constants controlling the maximum number of elements in the reservoir of Top-level Domains and 
	 * Fully Qualified URIs of each TLD, respectively
	 */
	private static int MAX_TLDS = 40;
	private static int MAX_FQURIS_PER_TLD = 20;
	
	/**
	 * Performs HTTP requests, used to try to fetch identified URIs
	 */
	private HTTPRetriever httpRetriever = new HTTPRetriever();
	private HTTPRetriever fqRetriever = new HTTPRetriever();

	
	/**
	 * Holds the set of dereferenceable top-level domains found among the subjects and objects of the triples,
	 * as a reservoir sampler, if its number of items grows beyond the limit (MAX_TLDS) items will be replaced 
	 * randomly upon forthcoming insertions. Moreover, the items will be indexed so that search operations are O(1)
	 */
	private ReservoirSampler<Tld> tldsReservoir = new ReservoirSampler<Tld>(MAX_TLDS, true);

	private DiachronCacheManager dcmgr = DiachronCacheManager.getInstance();

	private double metricValue = 0.0;
	private double totalURI = 0;
	private double dereferencedURI = 0;
	private boolean metricCalculated = false;
	
	private List<Quad> _problemList = new ArrayList<Quad>();
	
	private Queue<String> notFetchedQueue = new ConcurrentLinkedQueue<String>();


	
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
			
			httpRetriever.addListOfResourceToQueue(lstUrisToDeref);
			httpRetriever.start(false); //we do not need content negotiation for this
			
			List<String> lst = this.filterTLDs(lstUrisToDeref);	

			do {
				fqRetriever.addListOfResourceToQueue(lst);
				fqRetriever.start(true);
				this.startDereferencingProcess(lst);
				lst.clear();
				lst.addAll(this.notFetchedQueue);
				this.notFetchedQueue.clear();
			// Continue trying to dereference all URIs in uriSet, that is, those not fetched up to now
			} while(!lstUrisToDeref.isEmpty());
			
			this.metricCalculated = true;
			
		}
		
		this.metricValue = this.dereferencedURI / this.totalURI;
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
		// Extract the top-level domain (a.k.a pay level domain) and look for it in the reservoir 
		String uriTLD = httpRetriever.extractTopLevelDomainURI(uri);
		Tld newTld = new Tld(uriTLD, MAX_TLDS);		
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
	
	
	private List<String> filterTLDs(List<String> uriSet){
		List<String> possibleDeref = new ArrayList<String>();
		Queue<String> q = new ConcurrentLinkedQueue<String>();
		q.addAll(uriSet);
		while (!(q.isEmpty())){
			String uri = q.poll();
			CachedHTTPResource httpResource = (CachedHTTPResource) dcmgr.getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, uri);			
			if (httpResource == null || httpResource.getStatusLines() == null) {
				q.add(uri);
			} else {
				Tld newTld = new Tld(httpResource.getUri(), MAX_TLDS);		
				Tld foundTld = this.tldsReservoir.findItem(newTld);
				if (Dereferencer.hasOKStatus(httpResource)){
					if (foundTld.getfqUris().getItems() != null)
						possibleDeref.addAll(foundTld.getfqUris().getItems());
				} else {
					this.totalURI += foundTld.getfqUris().getItems().size();
					logger.trace("URI failed to be dereferenced: {}", httpResource.getUri());
					//problem report
				}
			}
		}
		return possibleDeref;
	}
	
	
	
	private void startDereferencingProcess(List<String> uriSet) {
		for(String uri : uriSet){
			CachedHTTPResource httpResource = (CachedHTTPResource) dcmgr.getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, uri);			
			if (httpResource == null || httpResource.getStatusLines() == null) {
				this.notFetchedQueue.add(uri);
			} else {
				this.totalURI++;
				if (Dereferencer.hasValidDereferencability(httpResource)){
					if(ModelParser.hasRDFContent(httpResource)){
						this.dereferencedURI++;
						logger.trace("URI successfully dereferenced and response OK and RDF: {}", httpResource.getUri());
					} else {
						this.createProblemQuad(httpResource.getUri(), DQM.NotMeaningful);
						logger.trace("URI was dereferenced but response was not valid: {}", httpResource.getUri());
					}
				} else {
					logger.trace("URI failed to be dereferenced: {}", httpResource.getUri());
				}

				createProblemReport(httpResource);
				
				logger.trace("{} - {} - {}", uri, httpResource.getStatusLines(), httpResource.getDereferencabilityStatusCode());
			}
		}
	}
	
	
	private void createProblemReport(CachedHTTPResource httpResource){
		StatusCode sc = httpResource.getDereferencabilityStatusCode();
		
		switch (sc){
			case SC200 : if (ModelParser.hasRDFContent(httpResource)) this.createProblemQuad(httpResource.getUri(), DQM.SC200WithRDF); 
						 else this.createProblemQuad(httpResource.getUri(), DQM.SC200WithoutRDF);
						 break;
			case SC301 : this.createProblemQuad(httpResource.getUri(), DQM.SC301MovedPermanently); break;
			case SC302 : this.createProblemQuad(httpResource.getUri(), DQM.SC302Found); break;
			case SC307 : this.createProblemQuad(httpResource.getUri(), DQM.SC307TemporaryRedirectory); break;
			case SC3XX : this.createProblemQuad(httpResource.getUri(), DQM.SC3XXRedirection); break;
			case SC4XX : this.createProblemQuad(httpResource.getUri(), DQM.SC4XXClientError); break;
			case SC5XX : this.createProblemQuad(httpResource.getUri(), DQM.SC5XXServerError); break;
			default	   : break;
		}
	}
	
	private void createProblemQuad(String resource, Resource problem){
		Quad q = new Quad(null, ModelFactory.createDefaultModel().createResource(resource).asNode(), QPRO.exceptionDescription.asNode(), problem.asNode());
		this._problemList.add(q);
	}
	
	
	public static int getMAX_TLDS() {
		return MAX_TLDS;
	}

	public static void setMAX_TLDS(int mAX_TLDS) {
		MAX_TLDS = mAX_TLDS;
	}

	public static int getMAX_FQURIS_PER_TLD() {
		return MAX_FQURIS_PER_TLD;
	}

	public static void setMAX_FQURIS_PER_TLD(int mAX_FQURIS_PER_TLD) {
		MAX_FQURIS_PER_TLD = mAX_FQURIS_PER_TLD;
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
