/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.availability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.datatypes.StatusCode;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.semantics.DQMPROB;
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
 * This metric calculates the number of valid redirects (303) or hashed links
 * according to LOD Principles
 * 
 * Based on: <a href="http://www.hyperthing.org/">Hyperthing - A linked data Validator</a>
 * 
 * @see <a href="http://dl.dropboxusercontent.com/u/4138729/paper/dereference_iswc2011.pdf">
 * Dereferencing Semantic Web URIs: What is 200 OK on the Semantic Web? - Yang et al.</a>
 * 
 */
public class Dereferenceability extends AbstractQualityMetric {
	
	private final Resource METRIC_URI = DQM.DereferenceabilityMetric;

	final static Logger logger = LoggerFactory.getLogger(Dereferenceability.class);
	
	private double metricValue = 0.0;
	private double totalURI = 0;
	private double dereferencedURI = 0;
	
	private HTTPRetriever httpRetreiver = new HTTPRetriever();
	private DiachronCacheManager dcmgr = DiachronCacheManager.getInstance();
	private Queue<String> notFetchedQueue = new ConcurrentLinkedQueue<String>();
	private Set<String> uriSet = Collections.synchronizedSet(new HashSet<String>());
	private boolean metricCalculated = false;
	
	private List<Quad> _problemList = new ArrayList<Quad>();
	
	public void compute(Quad quad) {
		logger.debug("Computing : {} ", quad.asTriple().toString());
		
		if (!(quad.getPredicate().getURI().equals(RDF.type.getURI()))){ // we are currently ignoring triples ?s a ?o
			String subject = quad.getSubject().toString();
			if (httpRetreiver.isPossibleURL(subject)){
				uriSet.add(subject);
			}
			
			String object = quad.getObject().toString();
			if (httpRetreiver.isPossibleURL(object)){
				uriSet.add(object);
			}
		}
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

	public double metricValue() {
		if (!this.metricCalculated){
			ArrayList<String> uriList = new ArrayList<String>();
			uriList.addAll(uriSet);
			httpRetreiver.addListOfResourceToQueue(uriList);
			httpRetreiver.start(true);

			do {
				this.startDereferencingProcess();
				uriSet.clear();
				uriSet.addAll(this.notFetchedQueue);
				this.notFetchedQueue.clear();
			// Continue trying to dereference all URIs in uriSet, that is, those not fetched up to now
			} while(!this.uriSet.isEmpty());
			
			this.metricCalculated = true;
		}
		this.metricValue = this.dereferencedURI / this.totalURI;
		
		statsLogger.info("Dereferenceability. Dataset: {} - Total # URIs : {}; # Dereferenced URIs : {}; Previously calculated : {}", 
				EnvironmentProperties.getInstance().getDatasetURI(), totalURI, dereferencedURI, metricCalculated);
		
		return this.metricValue;
	}
	
	private void startDereferencingProcess() {
		for(String uri : uriSet){
			CachedHTTPResource httpResource = (CachedHTTPResource) dcmgr.getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, uri);			
			if (httpResource == null || httpResource.getStatusLines() == null) {
				this.notFetchedQueue.add(uri);
			} else {
				this.totalURI++;
				
				if (Dereferencer.hasValidDereferencability(httpResource)) {
					dereferencedURI++;
				}
				
//				createProblemReport(httpResource);
				
				logger.trace("{} - {} - {}", uri, httpResource.getStatusLines(), httpResource.getDereferencabilityStatusCode());
			}
		}
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

	@Override
	public boolean isEstimate() {
		return false;
	}

	@Override
	public Resource getAgentURI() {
		return 	DQM.LuzzuProvenanceAgent;
	}
}
