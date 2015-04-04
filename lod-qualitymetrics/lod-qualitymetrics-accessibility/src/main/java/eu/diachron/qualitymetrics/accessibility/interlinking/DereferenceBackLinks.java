/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.interlinking;

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

import de.unibonn.iai.eis.diachron.datatypes.Pair;
import de.unibonn.iai.eis.diachron.datatypes.StatusCode;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.semantics.utilities.Commons;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.accessibility.availability.DereferenceabilityForwardLinks;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.DiachronCacheManager;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource.SerialisableHttpResponse;
import eu.diachron.qualitymetrics.utilities.CommonDataStructures;
import eu.diachron.qualitymetrics.utilities.HTTPRetriever;

/**
 * @author Jeremy Debattista
 * 
 * This metric measures the extent to which a resource includes all triples from the dataset that have 
 * the resource's URI as the object. This allows browsers and crawlers to traverse links in either direction, 
 * as specified by Hogan et al. To do so, it computes the ratio between the number of objects that are 
 * "back-links" a.k.a "inlinks" (are part of the resource's URI) and the total number of objects in the resource.
 * 
 * Based on: Hogan Aidan, Umbrich Jürgen. An empirical survey of Linked Data conformance.
 * 
 */
public class DereferenceBackLinks implements QualityMetric {

	
	private final Resource METRIC_URI = DQM.DereferenceabilityBackLinksMetric;
	
	final static Logger logger = LoggerFactory.getLogger(DereferenceabilityForwardLinks.class);
		
	private HTTPRetriever httpRetreiver = new HTTPRetriever();

	private Map<Pair<String,String>, Double> di_p = new ConcurrentHashMap<Pair<String,String>, Double>();
	
	private boolean metricCalculated = false;
	private double metricValue = 0.0;
	
	private List<Model> _problemList = new ArrayList<Model>();
	
	
	
	@Override
	public void compute(Quad quad) {
		if (!(quad.getPredicate().matches(RDF.type.asNode()))){
			String subject = quad.getSubject().toString();
			String object = quad.getObject().toString();
			if (httpRetreiver.isPossibleURL(object)){
				httpRetreiver.addResourceToQueue(object);
				di_p.put(new Pair<String,String>(subject,object), 0.0);
			}	
		}
	}

	@Override
	public double metricValue() {
		if (!this.metricCalculated){
			httpRetreiver.start();

			this.checkForBackwardLinking();
			this.metricCalculated = true;
			httpRetreiver.stop();
			
			double sum = 0.0;
			
			for(Double d : di_p.values()){
				sum += d;
			}
			
			metricValue = (sum == 0.0) ? 0.0 : sum / di_p.keySet().size();
		}
		
		return metricValue;
	}

	@Override
	public Resource getMetricURI() {
		return METRIC_URI;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		ProblemList<Model> pl = null;
		try {
			if(this._problemList != null && this._problemList.size() > 0) {
				pl = new ProblemList<Model>(this._problemList);
			} else {
				pl = new ProblemList<Model>();
			}
		} catch (ProblemListInitialisationException e) {
			logger.error(e.getMessage());
		}
		return pl;
	}

	@Override
	public boolean isEstimate() {
		return false;
	}

	@Override
	public Resource getAgentURI() {
		return 	DQM.LuzzuProvenanceAgent;
	}
	
	
	//Private Methods
	/**
	 * The object resource of an assessed triple is dereferenced. For a backlink to be valid
	 * the deferenced representation should have a triple, where the object is found in the subject
	 * position of this triple whilst the subject of the original assessed triple is found in the
	 * object position.
	 */
	private void checkForBackwardLinking(){
		List<Pair<String,String>> uriSet = new ArrayList<Pair<String,String>>(di_p.keySet());
		while(uriSet.size() > 0){
			Pair<String,String> p_uri = uriSet.remove(0);
			CachedHTTPResource httpResource = (CachedHTTPResource) DiachronCacheManager.getInstance().getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, p_uri.getSecondElement());
			if (httpResource.getResponses() == null) {
				uriSet.add(p_uri);
				continue;
			}
			if (this.isDereferenceable(httpResource)){
				Model m = this.getMeaningfulData(httpResource);
				if (m.size() > 0){
					
					List<Statement> allStatements = m.listStatements(null, null,  m.createResource(p_uri.getFirstElement())).toList();
					
					if (allStatements.size() > 0){
						di_p.put(p_uri, 1.0);
					} else {
						//no backlink found for p_uri.getFirstElement in p_uri.getSecondElement
						this.createBackLinkViolation(p_uri.getFirstElement(), p_uri.getSecondElement());
					}
					
					
				} else {
					// report problem Not Valid Dereferenced Backlink
					this.createNotValidDereferenceableBacklinkLink(p_uri.getSecondElement());
				}
			}
		}
	}
	
	private void createNotValidDereferenceableBacklinkLink(String resource){
		Model m = ModelFactory.createDefaultModel();
		
		Resource subject = m.createResource(resource);
		m.add(new StatementImpl(subject, QPRO.exceptionDescription, DQM.NotValidDereferenceableBackLink));
		
		this._problemList.add(m);
	}
	
	private void createBackLinkViolation(String subjectURI, String resource){
		Model m = ModelFactory.createDefaultModel();
		
		Resource subject = m.createResource(resource);
		m.add(new StatementImpl(subject, QPRO.exceptionDescription, DQM.NoBackLink));
		
		RDFNode violatedTriple = Commons.generateRDFBlankNode();
		m.add(new StatementImpl(violatedTriple.asResource(), RDF.subject, m.createResource(subjectURI)));
		
		m.add(new StatementImpl(subject, DQM.hasViolatingTriple, violatedTriple));

		this._problemList.add(m);
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
}
