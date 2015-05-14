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
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
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
import eu.diachron.qualitymetrics.accessibility.availability.helper.Dereferencer;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.DiachronCacheManager;
import eu.diachron.qualitymetrics.utilities.HTTPResourceUtils;
import eu.diachron.qualitymetrics.utilities.HTTPRetriever;
import eu.diachron.qualitymetrics.utilities.VocabularyLoader;

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
 * 
 */
public class DereferenceabilityForwardLinks implements QualityMetric {
	
	//TODO: FIX metric
	private final Resource METRIC_URI = DQM.DereferenceabilityForwardLinksMetric;
	
	final static Logger logger = LoggerFactory.getLogger(DereferenceabilityForwardLinks.class);
		
	private HTTPRetriever httpRetreiver = new HTTPRetriever();

	
	private boolean metricCalculated = false;
	private double metricValue = 0.0;
	
	private double totalDerefSubj = 0.0;
	private double totalDerefDataWithSub = 0.0;
	
	private List<Model> _problemList = new ArrayList<Model>();
	private List<String> uriSet = new ArrayList<String>();

	
	public void compute(Quad quad) {
		String subject = quad.getSubject().toString();
		if (httpRetreiver.isPossibleURL(subject)){
			httpRetreiver.addResourceToQueue(subject);
			uriSet.add(subject);
		}
	}

	public double metricValue() {
		if (!this.metricCalculated){
			httpRetreiver.start();

			this.checkForForwardLinking();
			this.metricCalculated = true;
			httpRetreiver.stop();
			
			
//			metricValue = (sum == 0.0) ? 0.0 : sum / do_p.keySet().size();
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
					
					List<Statement> stmtList;
					//lets first check if the vocabulary exists, so that we do not download it
					Resource res = ModelFactory.createDefaultModel().createResource(httpResource.getUri());
					Node n = res.asNode();
					String ns = n.getNameSpace();
					if (VocabularyLoader.knownVocabulary(ns)){
						Model m = VocabularyLoader.getModelForVocabulary(ns);
						stmtList = m.listStatements(res, (Property) null, (RDFNode) null).toList();
					} else {
						stmtList = ModelFactory.createDefaultModel().read(httpResource.getUri()).listStatements(res, (Property) null, (RDFNode) null).toList();
					}
					
//					if (stmtList.size() > 1) 
					
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
		return false;
	}

	@Override
	public Resource getAgentURI() {
		return 	DQM.LuzzuProvenanceAgent;
	}
}
