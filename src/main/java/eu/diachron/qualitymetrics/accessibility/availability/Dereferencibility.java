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
import java.util.concurrent.ExecutionException;

import org.apache.http.StatusLine;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.datatypes.StatusCode;
import de.unibonn.iai.eis.luzzu.assessment.ComplexQualityMetric;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.DiachronCacheManager;
import eu.diachron.qualitymetrics.utilities.HTTPRetreiver;
import eu.diachron.semantics.vocabulary.DQM;

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
public class Dereferencibility implements ComplexQualityMetric {
	
	private final Resource METRIC_URI = DQM.DereferenceabilityMetric;

	private static Logger logger = Logger.getLogger(Dereferencibility.class);

	private double metricValue = 0.0;
	private double totalURI = 0;
	private double dereferencedURI = 0;
	
	private HTTPRetreiver httpRetreiver = HTTPRetreiver.getInstance();
	private DiachronCacheManager dcmgr = DiachronCacheManager.getInstance();
	private Queue<String> uriQueue = new ConcurrentLinkedQueue<String>();
	
	
	public void after(Object... arg0) {
		//maybe we do not need a complex metric after all?
		if (httpRetreiver.hasCompletedActions()) this.startDereferencingProcess();
	}

	public void before(Object... arg0) {
		// Do Nothing
	}

	public void compute(Quad quad) {
		if (!(quad.getPredicate().getURI().equals(RDF.type.getURI()))){ // we are currently ignoring triples ?s a ?o
			
			String subject = quad.getSubject().toString();
			if (httpRetreiver.isPossibleURL(subject)){
				httpRetreiver.addResourceToQueue(subject);
				uriQueue.add(subject);
			}
			
			String object = quad.getObject().toString();
			if (httpRetreiver.isPossibleURL(object)){
				httpRetreiver.addResourceToQueue(object);
				uriQueue.add(subject);
			}
		}
	}

	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	public ProblemList<?> getQualityProblems() {
		// TODO Auto-generated method stub
		return null;
	}

	public double metricValue() {
		this.metricValue = this.dereferencedURI / this.totalURI;
		return this.metricValue;
	}
	
	
	/* Private Methods */
	private void startDereferencingProcess() {
		while(!this.uriQueue.isEmpty()){
			String uri = this.uriQueue.poll();
			CachedHTTPResource httpResource = (CachedHTTPResource) dcmgr.getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, uri);
			if (httpResource.getStatusLines() == null) {
				this.uriQueue.add(uri);
			} else {
				if (this.isDereferenceable(httpResource)) this.dereferencedURI++;
				this.totalURI++;
			}
		}
	}
	
	private boolean isDereferenceable(CachedHTTPResource httpResource){
		if (httpResource.getDereferencabilityStatusCode() == null){
			List<Integer> statusCode = this.getStatusCodes(httpResource.getStatusLines());
			
			if (httpResource.getUri().contains("#") && statusCode.contains(200)) httpResource.setDereferencabilityStatusCode(StatusCode.HASH);
			
			if (statusCode.contains(200)){
				if (statusCode.size() == 1) httpResource.setDereferencabilityStatusCode(StatusCode.SC200);
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
		for(StatusLine s : statusLines){
			codes.add(s.getStatusCode());
		}
		
		return codes;
	}
	
	private boolean mapDerefStatusCode(StatusCode statusCode){
		switch(statusCode){
			case SC303 : case HASH : return true;
			default : return false;
		}
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
}
