package eu.diachron.qualitymetrics.accessibility.availability;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.StatusLine;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.datatypes.StatusCode;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.semantics.utilities.Commons;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource.SerialisableHttpResponse;
import eu.diachron.qualitymetrics.cache.DiachronCacheManager;
import eu.diachron.qualitymetrics.utilities.CommonDataStructures;
import eu.diachron.qualitymetrics.utilities.HTTPRetriever;

/**
 * @author Santiago Londoño
 * 
 * This metric measures the extent to which a resource includes all triples from the dataset that have 
 * the resource's URI as the subject. This is the immediate description of the resource, as specified by 
 * Hogan et al. To do so, it computes the ratio between the number of subjects that are "forward-links" 
 * a.k.a "outlinks" (are part of the resource's URI) and the total number of subjects in the resource.
 * 
 * Based on: Hogan Aidan, Umbrich Jürgen. An empirical survey of Linked Data conformance.
 * 
 * Best Case 1, Worst Case 0
 */
public class DereferenceabilityForwardLinks implements QualityMetric {
	
	private final Resource METRIC_URI = DQM.DereferenceabilityForwardLinksMetric;
	
	final static Logger logger = LoggerFactory.getLogger(DereferenceabilityForwardLinks.class);
		
	private HTTPRetriever httpRetreiver = new HTTPRetriever();

	/** Stores the % of locally-minted subjects URIs of dereferenced Resource (i.e. the % of how many times 
	 *  the dereferenced resource appears to be as the subject in the dereferenced triples)
	 */
	private Map<String, Double> do_p = new ConcurrentHashMap<String, Double>();
	
	private boolean metricCalculated = false;
	private double metricValue = 0.0;
	
	private List<Model> _problemList = new ArrayList<Model>();

	
	public void compute(Quad quad) {
		String subject = quad.getSubject().toString();
		if (httpRetreiver.isPossibleURL(subject)){
			httpRetreiver.addResourceToQueue(subject);
			do_p.put(subject, 0.0);
		}
	}

	public double metricValue() {
		if (!this.metricCalculated){
			httpRetreiver.start();

			this.checkForForwardLinking();
			this.metricCalculated = true;
			httpRetreiver.stop();
			
			double sum = 0.0;
			for(String s : do_p.keySet()){
				sum += do_p.get(s);
			}
			
			metricValue = (sum == 0.0) ? 0.0 : sum / do_p.keySet().size();
		}
		
		return metricValue;
	}
	
	public Resource getMetricURI() {
		return this.METRIC_URI;
	}
	
	public ProblemList<?> getQualityProblems() {
		ProblemList<Model> pl = null;
		try {
			pl = new ProblemList<Model>(this._problemList);
		} catch (ProblemListInitialisationException e) {
			logger.error(e.getMessage());
		}
		return pl;
	}
	
	// Private Method for checking forward linking
	private void checkForForwardLinking(){
		List<String> uriSet = new ArrayList<String>(do_p.keySet());
		while(uriSet.size() > 0){
			String uri = uriSet.remove(0);
			CachedHTTPResource httpResource = (CachedHTTPResource) DiachronCacheManager.getInstance().getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, uri);
			if (httpResource.getResponses() == null) {
				uriSet.add(uri);
				continue;
			}
			if (this.isDereferenceable(httpResource)){
				Model m = this.getMeaningfulData(httpResource);
				if (m.size() > 0){
					List<Statement> allStatements = m.listStatements().toList();
					
					int correct = 0;
					for(Statement s : allStatements){
						if (s.getSubject().getURI().equals(httpResource.getUri())) correct++;
						else this.createViolatingTriple(s, httpResource.getUri());
					}
				
					double per_local_minted_uri = ((double) correct) / ((double)allStatements.size());
					do_p.put(httpResource.getUri(), per_local_minted_uri);
				} else {
					// report problem Not Valid Forward Link
					this.createNotValidForwardLink(httpResource.getUri());
				}
			}
		}
	}
	
	private void createNotValidForwardLink(String resource){
		Model m = ModelFactory.createDefaultModel();
		
		Resource subject = m.createResource(resource);
		m.add(new StatementImpl(subject, QPRO.exceptionDescription, DQM.NotValidForwardLink));
		
		this._problemList.add(m);
	}
	
	private void createViolatingTriple(Statement stmt, String resource){
		Model m = ModelFactory.createDefaultModel();
		
		Resource subject = m.createResource(resource);
		m.add(new StatementImpl(subject, QPRO.exceptionDescription, DQM.ViolatingTriple));
		
		RDFNode violatedTriple = Commons.generateRDFBlankNode();
		m.add(new StatementImpl(violatedTriple.asResource(), RDF.type, RDF.Statement));
		m.add(new StatementImpl(violatedTriple.asResource(), RDF.subject, stmt.getSubject()));
		m.add(new StatementImpl(violatedTriple.asResource(), RDF.predicate, stmt.getPredicate()));
		m.add(new StatementImpl(violatedTriple.asResource(), RDF.object, stmt.getObject()));
		
		m.add(new StatementImpl(subject, DQM.hasViolatingTriple, violatedTriple));

		this._problemList.add(m);
	}
	
	// Private Method to check content type
	private Model getMeaningfulData(CachedHTTPResource resource){
		Model m = null;
		if(resource != null && resource.getResponses() != null) {
			for (SerialisableHttpResponse response : resource.getResponses()) {
				if(response != null && response.getHeaders("Content-Type") != null) {
					if (CommonDataStructures.ldContentTypes.contains(response.getHeaders("Content-Type"))) { 
						m = this.tryRead(resource.getUri());
					}
				}
			}
		}
		return m;
	}
	
	private Model tryRead(String uri) {
		Model m = ModelFactory.createDefaultModel();
		try{
			m = RDFDataMgr.loadModel(uri);
		} catch (RiotException r) {
			Log.debug("Resource could not be parsed:", r.getMessage());
		}
		return m;
	}
	
	
	
	// Private Methods for Dereferenceability Process
	private boolean isDereferenceable(CachedHTTPResource httpResource){
		if (httpResource.getDereferencabilityStatusCode() == null){
			List<Integer> statusCode = this.getStatusCodes(httpResource.getStatusLines());
			
			if (httpResource.getUri().contains("#") && statusCode.contains(200)) httpResource.setDereferencabilityStatusCode(StatusCode.HASH);
			else if (statusCode.contains(200)){
				httpResource.setDereferencabilityStatusCode(StatusCode.SC200);
				if (statusCode.contains(303)) httpResource.setDereferencabilityStatusCode(StatusCode.SC303);
				else {
					if (statusCode.contains(301)) httpResource.setDereferencabilityStatusCode(StatusCode.SC301);
					else if (statusCode.contains(302)) httpResource.setDereferencabilityStatusCode(StatusCode.SC302);
					else if (statusCode.contains(307)) httpResource.setDereferencabilityStatusCode(StatusCode.SC307);
				}
			}
			
			if (has4xxCode(statusCode)) {
				httpResource.setDereferencabilityStatusCode(StatusCode.SC4XX);
			}
			if (has5xxCode(statusCode)) {
				httpResource.setDereferencabilityStatusCode(StatusCode.SC5XX);
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

}
