/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.availability;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
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
import eu.diachron.qualitymetrics.accessibility.availability.helper.Dereferencer;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.DiachronCacheManager;
import eu.diachron.qualitymetrics.utilities.HTTPResourceUtils;
import eu.diachron.qualitymetrics.utilities.HTTPRetriever;
import eu.diachron.qualitymetrics.utilities.VocabularyLoader;

/**
 * @author Jeremy Debattista
 * 
 */
public class EstimatedDereferenceabilityForwardLinks implements QualityMetric {
	//TODO:fix
	private final Resource METRIC_URI = DQM.DereferenceabilityForwardLinksMetric;
	
	final static Logger logger = LoggerFactory.getLogger(EstimatedDereferenceabilityForwardLinks.class);
		
	private HTTPRetriever httpRetreiver = new HTTPRetriever();

	/** Stores the % of locally-minted subjects URIs of dereferenced Resource (i.e. the % of how many times 
	 *  the dereferenced resource appears to be as the subject in the dereferenced triples)
	 */
	private Map<String, Double> do_p = new ConcurrentHashMap<String, Double>();
	
	private boolean metricCalculated = false;
	private double metricValue = 0.0;
	
	private List<Model> _problemList = new ArrayList<Model>();
	private List<String> uriSet = new ArrayList<String>();

	/**
	 * Constants controlling the maximum number of elements in the reservoir of Top-level Domains and 
	 * Fully Qualified URIs of each TLD, respectively
	 */
	private static int MAX_TLDS = 10;
	private static int MAX_FQURIS_PER_TLD = 250;
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
			for(Tld tld : this.tldsReservoir.getItems()){
				uriSet.addAll(tld.getfqUris().getItems());
			}
			httpRetreiver.addListOfResourceToQueue(uriSet);
			
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
	
	// Private Method for checking forward linking
	private void checkForForwardLinking(){
		while(uriSet.size() > 0){
			String uri = uriSet.remove(0);
			CachedHTTPResource httpResource = (CachedHTTPResource) DiachronCacheManager.getInstance().getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, uri);
			
			if (httpResource == null || (httpResource.getResponses() == null && httpResource.getDereferencabilityStatusCode() != StatusCode.BAD)) {
				uriSet.add(uri);
				continue;
			}
			
			logger.info("Checking resource: {}. URIs left: {}.", httpResource.getUri(), uriSet.size());

			// We perform a semantic lookup using heuristics to check if we
			// really need to try parsing or not
			if (HTTPResourceUtils.semanticURILookup(httpResource)){
				logger.info("Trying to find any dereferencable forward links for {}.", httpResource.getUri());
				if (Dereferencer.hasValidDereferencability(httpResource)){
					logger.info("Dereferencable resource {}.", httpResource.getUri());
					
					Iterator<?> iter = null;
					//lets first check if the vocabulary exists, so that we do not download it
					Node n = ModelFactory.createDefaultModel().createResource(httpResource.getUri()).asNode();
					String ns = n.getNameSpace();
					if (VocabularyLoader.knownVocabulary(ns)){
						Model m = VocabularyLoader.getModelForVocabulary(ns);
						iter = m.listStatements();
					} else {
						iter = ModelFactory.createDefaultModel().read(httpResource.getUri()).listStatements();
					}
					
					int correct = 0;
					int triples = 0;
					while(iter.hasNext()){
						triples++;
						Statement s = ((StmtIterator)iter).next();

						if (s.getSubject().isURIResource()){
							if (s.getSubject().getURI().equals(httpResource.getUri())) correct++;
							else this.createViolatingTriple(s, httpResource.getUri());
						}
						
						if (triples > 0){
							double per_local_minted_uri = ((double) correct) / ((double)triples);
							do_p.put(httpResource.getUri(), per_local_minted_uri);
						} else {
							this.createNotValidForwardLink(httpResource.getUri());
						}
					}
				}
			} else {
				logger.info("Non-meaningful dereferencable resource {}.", httpResource.getUri());
				this.createNotValidForwardLink(httpResource.getUri());
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
	

	
	@Override
	public boolean isEstimate() {
		return true;
	}

	@Override
	public Resource getAgentURI() {
		return 	DQM.LuzzuProvenanceAgent;
	}

}
