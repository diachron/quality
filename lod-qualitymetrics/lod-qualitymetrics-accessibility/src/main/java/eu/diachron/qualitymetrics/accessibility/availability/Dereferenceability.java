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

import org.apache.http.StatusLine;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.WebContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.datatypes.StatusCode;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource.SerialisableHttpResponse;
import eu.diachron.qualitymetrics.cache.DiachronCacheManager;
import eu.diachron.qualitymetrics.utilities.CommonDataStructures;
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
public class Dereferenceability implements QualityMetric {
	
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
			pl = new ProblemList<Quad>(this._problemList);
		} catch (ProblemListInitialisationException e) {
//			logger.debug(e.getStackTrace());
			logger.error(e.getMessage());
		}
		return pl;
	}

	public double metricValue() {
		if (!this.metricCalculated){
			ArrayList<String> uriList = new ArrayList<String>();
			uriList.addAll(uriSet);
			httpRetreiver.addListOfResourceToQueue(uriList);
			httpRetreiver.start();

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
		return this.metricValue;
	}
	
	
	/* Private Methods */
	private void startDereferencingProcess() {
		for(String uri : uriSet){
			CachedHTTPResource httpResource = (CachedHTTPResource) dcmgr.getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, uri);			
			if (httpResource == null || httpResource.getStatusLines() == null) {
				this.notFetchedQueue.add(uri);
			} else {
				if (this.isDereferenceable(httpResource)) {
					if (this.is200AnRDF(httpResource)) { 
						this.dereferencedURI++;
						logger.trace("URI successfully dereferenced and response OK and RDF: {}", httpResource.getUri());
					} else {
						this.createProblemQuad(httpResource.getUri(), DQM.NotMeaningful);
						logger.trace("URI was dereferenced but response was not valid: {}", httpResource.getUri());
					}
				} else {
					logger.trace("URI failed to be dereferenced: {}", httpResource.getUri());
				}
				this.totalURI++;

				if (httpResource.getDereferencabilityStatusCode() == StatusCode.SC200) this.is200AnRDF(httpResource);
//					this.dereferencedURI = (this.is200AnRDF(httpResource)) ? this.dereferencedURI + 1 : this.dereferencedURI;
		
				logger.trace("{} - {} - {}", uri, httpResource.getStatusLines(), httpResource.getDereferencabilityStatusCode());
			}
		}
	}
	
	private boolean isDereferenceable(CachedHTTPResource httpResource){
		if (httpResource.getDereferencabilityStatusCode() == null){
			List<Integer> statusCode = this.getStatusCodes(httpResource.getStatusLines());
			
			if (httpResource.getUri().contains("#") && statusCode.contains(200)) httpResource.setDereferencabilityStatusCode(StatusCode.HASH);
			else if (statusCode.contains(200)){
				httpResource.setDereferencabilityStatusCode(StatusCode.SC200);
				if (statusCode.contains(303)) httpResource.setDereferencabilityStatusCode(StatusCode.SC303);
				else {
					if (statusCode.contains(301)) { 
						httpResource.setDereferencabilityStatusCode(StatusCode.SC301);
						this.createProblemQuad(httpResource.getUri(), DQM.SC301MovedPermanently);
					}
					else if (statusCode.contains(302)){
						httpResource.setDereferencabilityStatusCode(StatusCode.SC302);
						this.createProblemQuad(httpResource.getUri(), DQM.SC302Found);
					}
					else if (statusCode.contains(307)) {
						httpResource.setDereferencabilityStatusCode(StatusCode.SC307);
						this.createProblemQuad(httpResource.getUri(), DQM.SC307TemporaryRedirectory);
					}
					else {
						if (hasBad3xxCode(statusCode)) this.createProblemQuad(httpResource.getUri(), DQM.SC3XXRedirection);
					}
				}
			}
			
			if (has4xxCode(statusCode)) {
				httpResource.setDereferencabilityStatusCode(StatusCode.SC4XX);
				this.createProblemQuad(httpResource.getUri(), DQM.SC4XXClientError);
			}
			if (has5xxCode(statusCode)) {
				httpResource.setDereferencabilityStatusCode(StatusCode.SC5XX);
				this.createProblemQuad(httpResource.getUri(), DQM.SC5XXServerError);
			}
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
					if (CommonDataStructures.ldContentTypes.contains(response.getHeaders("Content-Type"))) { 
						if (response.getHeaders("Content-Type").equals(WebContent.contentTypeTextPlain)){
							Model m = this.tryRead(resource.getUri());
							if (m.size() == 0){
								this.createProblemQuad(resource.getUri(), DQM.SC200WithoutRDF);
								return false;
							}
						}
						this.createProblemQuad(resource.getUri(), DQM.SC200WithRDF);
						return true;
					}
				}
			}
		}
		this.createProblemQuad(resource.getUri(), DQM.SC200WithoutRDF);
		return false;
	}
	
	private boolean hasBad3xxCode(List<Integer> statusCode){
		for (int i : statusCode){
			if ((i == 300) || (i == 304) || (i == 305) || 
					(i == 306) || (i == 308) ||
					((i >= 308) && (i < 399)))  return true; else continue;
		}
		return false;
	}
	
	private boolean has4xxCode(List<Integer> statusCode){
		for (int i : statusCode){
			if ((i >= 400) && (i < 499))  return true; else continue;
		}
		return false;
	}
	
	private boolean has5xxCode(List<Integer> statusCode){
		for (int i : statusCode){
			if ((i >= 500) && (i < 599))  return true; else continue;
		}
		return false;
	}

	private void createProblemQuad(String resource, Resource problem){
		Quad q = new Quad(null, ModelFactory.createDefaultModel().createResource(resource).asNode(), QPRO.exceptionDescription.asNode(), problem.asNode());
		this._problemList.add(q);
	}

	/**
	 * Try Read content returned by text/plain 
	 * @param uri
	 * @return
	 */
	private Model tryRead(String uri) {
		Model m = ModelFactory.createDefaultModel();
		try{
			m = RDFDataMgr.loadModel(uri, Lang.NTRIPLES);
		} catch (RiotException r) {
			Log.debug("Resource could not be parsed:", r.getMessage());
		}
		return m;
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
