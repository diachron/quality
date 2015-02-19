/**
 * 
 */
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
import de.unibonn.iai.eis.diachron.datatypes.Tld;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ReservoirSampler;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.semantics.utilities.Commons;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.DiachronCacheManager;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource.SerialisableHttpResponse;
import eu.diachron.qualitymetrics.utilities.CommonDataStructures;
import eu.diachron.qualitymetrics.utilities.HTTPRetriever;

/**
 * @author Jeremy Debattista
 * 
 */
public class EstimatedDereferenceabilityForwardLinks implements QualityMetric {
	
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


	/**
	 * Constants controlling the maximum number of elements in the reservoir of Top-level Domains and 
	 * Fully Qualified URIs of each TLD, respectively
	 */
	private static int MAX_TLDS = 50;
	private static int MAX_FQURIS_PER_TLD = 10000;
	private ReservoirSampler<Tld> tldsReservoir = new ReservoirSampler<Tld>(MAX_TLDS, true);

	
	public void compute(Quad quad) {
		String subject = quad.getSubject().toString();
		if (httpRetreiver.isPossibleURL(subject)){
			addURIToReservoir(subject);
		}
	}
	
	private void addURIToReservoir(String uri) {
		// Extract the top-level domain (a.k.a pay level domain) and look for it in the reservoir 
		String uriTLD = httpRetreiver.extractTopLevelDomainURI(uri);
		Tld newTld = new Tld(uriTLD, MAX_FQURIS_PER_TLD);		
		Tld foundTld = this.tldsReservoir.findItem(newTld);
		
		if(foundTld == null) {
			logger.trace("New TLD found and recorded: {}...", uriTLD);
			// Add the new TLD to the reservoir
			// Add new fully qualified URI to those of the new TLD
			
			this.tldsReservoir.add(newTld); 
			newTld.addFqUri(uri);
		} else {
			// The identified TLD was found, it already exists on the reservoir, just add the fqdn to it
			foundTld.addFqUri(uri);
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
		for(Tld tld : this.tldsReservoir.getItems()){
			List<String> uriSet = tld.getfqUris().getItems(); 
			httpRetreiver.addListOfResourceToQueue(uriSet);
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
	
	@Override
	public boolean isEstimate() {
		return true;
	}

	@Override
	public Resource getAgentURI() {
		return 	DQM.LuzzuProvenanceAgent;
	}

}
