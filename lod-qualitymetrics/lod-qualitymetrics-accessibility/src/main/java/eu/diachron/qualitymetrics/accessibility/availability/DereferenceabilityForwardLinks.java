package eu.diachron.qualitymetrics.accessibility.availability;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.datatypes.StatusCode;
import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;
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
import eu.diachron.qualitymetrics.utilities.HTTPResourceUtils;
import eu.diachron.qualitymetrics.utilities.HTTPRetriever;

/**
 * @author Santiago Londoño
 * 
 * This metric measures the extent to which a resource includes all triples from the dataset that have 
 * the resource's URI as the subject. This is the immediate description of the resource, as specified by 
 * Hogan et al. To do so, it computes the ratio between the number of subjects that are "forward-links" 
 * a.k.a "outlinks" (are part of the resource's URI) and the total number of subjects in the resource.
 * 
 * This is explained further in 
 * http://wifo5-03.informatik.uni-mannheim.de/bizer/pub/LinkedDataTutorial/#deref
 * 
 * In short, if a subject resource has a description, then it is a valid forward link
 * because from that URI we can get its description directly.
 * 
 * Based on: Hogan Aidan, Umbrich Jürgen. An empirical survey of Linked Data conformance.
 * 
 * Best Case 1, Worst Case 0
 * 
 */
public class DereferenceabilityForwardLinks extends AbstractQualityMetric {
	
	private final Resource METRIC_URI = DQM.DereferenceabilityForwardLinksMetric;
	
	final static Logger logger = LoggerFactory.getLogger(DereferenceabilityForwardLinks.class);
		
	private HTTPRetriever httpRetreiver = new HTTPRetriever();

	
	private boolean metricCalculated = false;
	private double metricValue = 0.0;
	
	private double totalDerefDataWithSub = 0.0;
	
	private List<Model> _problemList = new ArrayList<Model>();
	private NavigableSet<String> uriSet = MapDbFactory.createAsyncFilesystemDB().createTreeSet("uri-set").make();
	private Queue<String> notFetchedQueue = new ConcurrentLinkedQueue<String>();

	@Override
	public void compute(Quad quad) {
		logger.debug("Computing : {} ", quad.asTriple().toString());
		
		String predicate = quad.getPredicate().getURI();
		
		if (!(predicate.equals(RDF.type))){
			if ((quad.getSubject().isURI()) && (!(quad.getSubject().isBlank())))
				uriSet.add(quad.getSubject().getURI());
		}
	}

	@Override
	public double metricValue() {
		if (!this.metricCalculated){
			Double uriSize = (double) uriSet.size();
			httpRetreiver.addListOfResourceToQueue(new ArrayList<String>(uriSet));
			httpRetreiver.start(true);

			do {
				this.checkForForwardLinking();
				uriSet.clear();
				uriSet.addAll(this.notFetchedQueue);
				this.notFetchedQueue.clear();
			} while(!this.uriSet.isEmpty());
			
			this.metricCalculated = true;
			httpRetreiver.stop();
			
			metricValue = (totalDerefDataWithSub == 0.0) ? 0.0 : (double) totalDerefDataWithSub / uriSize;
			
			statsLogger.info("Dereferencable Forward Links Metric: Dataset: {} - Total # Forward Links URIs {}; Total URIs : {}", 
					EnvironmentProperties.getInstance().getDatasetURI(), totalDerefDataWithSub, uriSize);
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
		for(String uri : uriSet){
			CachedHTTPResource httpResource = (CachedHTTPResource) DiachronCacheManager.getInstance().getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, uri);
			
			if (httpResource == null || (httpResource.getResponses() == null && httpResource.getDereferencabilityStatusCode() != StatusCode.BAD)) {
				this.notFetchedQueue.add(uri);
			} else {
				logger.info("Checking resource: {}. URIs left: {}.", httpResource.getUri(), uriSet.size());

				// We perform a semantic lookup using heuristics to check if we
				// really need to try parsing or not
				if (HTTPResourceUtils.semanticURILookup(httpResource)){
					logger.info("Trying to find any dereferencable forward links for {}.", httpResource.getUri());
					if (Dereferencer.hasValidDereferencability(httpResource)){
						logger.info("Dereferencable resource {}.", httpResource.getUri());
						
						Model m = RDFDataMgr.loadModel(httpResource.getUri());
						Resource r = m.createResource(httpResource.getUri());
						List<Statement> stmtList = m.listStatements(r, (Property) null, (RDFNode) null).toList();
						
						if (stmtList.size() > 1){
							//ok
							logger.info("A description exists for resource {}.", httpResource.getUri());

							totalDerefDataWithSub++;
						} else {
							//not ok
							this.createNotValidForwardLink(httpResource.getUri());
						}
						
					}
				} else {
					logger.info("Non-meaningful dereferencable resource {}.", httpResource.getUri());
					this.createNotValidForwardLink(httpResource.getUri());
				}
			}
		}
	}
	
	
	private void createNotValidForwardLink(String resource){
		Model m = ModelFactory.createDefaultModel();
		
		Resource subject = m.createResource(resource);
		m.add(new StatementImpl(subject, QPRO.exceptionDescription, DQMPROB.NotValidForwardLink));
		
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
