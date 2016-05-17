/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.availability;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.datatypes.StatusCode;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.semantics.DQMPROB;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ReservoirSampler;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import de.unibonn.iai.eis.luzzu.semantics.utilities.Commons;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.accessibility.availability.helper.Dereferencer;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.DiachronCacheManager;
import eu.diachron.qualitymetrics.utilities.AbstractQualityMetric;
import eu.diachron.qualitymetrics.utilities.HTTPResourceUtils;
import eu.diachron.qualitymetrics.utilities.HTTPRetriever;

/**
 * @author Jeremy Debattista
 * 
 */
public class EstimatedDereferenceabilityForwardLinks extends AbstractQualityMetric {
	//TODO:fix
	private final Resource METRIC_URI = DQM.DereferenceabilityForwardLinksMetric;
	
	final static Logger logger = LoggerFactory.getLogger(EstimatedDereferenceabilityForwardLinks.class);
		
	private HTTPRetriever httpRetreiver = new HTTPRetriever();
	
	private boolean metricCalculated = false;
	private double metricValue = 0.0;
	private double totalDerefDataWithSub = 0.0;

	
	private List<Model> _problemList = new ArrayList<Model>();
	private Queue<String> uriSet = new ConcurrentLinkedQueue<String>();
	private Queue<String> notFetchedQueue = new ConcurrentLinkedQueue<String>();

	
	/**
	 * Constants controlling the maximum number of elements in the reservoir 	 */
	private static int MAX_FQURIS = 200000;
	private ReservoirSampler<String> fqurisReservoir = new ReservoirSampler<String>(MAX_FQURIS, true);
	

	@Override
	public void compute(Quad quad) {
		
		logger.debug("Computing : {} ", quad.asTriple().toString());
		
		String predicate = quad.getPredicate().getURI();
		
		if (!(predicate.equals(RDF.type))){
			if ((quad.getSubject().isURI()) && (!(quad.getSubject().isBlank()))){
				boolean isAdded = fqurisReservoir.add(quad.getSubject().getURI());
				logger.trace("URI found on subject: {}, was added to reservoir? {}", quad.getSubject().getURI(), isAdded);
				
			}
		}
	}
	
	
	
	/**
	 * Initiates the dereferencing process of some of the URIs identified in the dataset, chosen in accordance 
	 * with a statistical sampling method, in order to compute the estimated dereferenceability of the whole dataset 
	 * @return estimated dereferencibility, computed as aforementioned
	 */
	@Override
	public double metricValue() {
		
		if(!this.metricCalculated) {
			uriSet.addAll(this.fqurisReservoir.getItems());
			double uriSize = (double) uriSet.size();

			
			//Process
			this.httpRetreiver.addListOfResourceToQueue(this.fqurisReservoir.getItems());
			this.httpRetreiver.start(true);
			
			do {
				this.checkForForwardLinking();
				uriSet.clear();
				uriSet.addAll(this.notFetchedQueue);
				this.notFetchedQueue.clear();
			} while(!this.uriSet.isEmpty());
			
			this.metricCalculated = true;
			httpRetreiver.stop();
			//End Process
			
			metricValue = (totalDerefDataWithSub == 0.0) ? 0.0 : (double) totalDerefDataWithSub / uriSize;
			
			statsLogger.info("Estimated Dereferencable Forward Links Metric: Dataset: {} - Total # Forward Links URIs {}; Total URIs : {}", 
					EnvironmentProperties.getInstance().getDatasetURI(), totalDerefDataWithSub, uriSize);
		}
		
		return this.metricValue;
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
						
						
						Model m = RDFDataMgr.loadModel(httpResource.getUri()); //load partial model
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
	
	private void createViolatingTriple(Statement stmt, String resource){
		Model m = ModelFactory.createDefaultModel();
		
		Resource subject = m.createResource(resource);
		m.add(new StatementImpl(subject, QPRO.exceptionDescription, DQMPROB.ViolatingTriple));
		
		RDFNode violatedTriple = Commons.generateRDFBlankNode();
		m.add(new StatementImpl(violatedTriple.asResource(), RDF.type, RDF.Statement));
		m.add(new StatementImpl(violatedTriple.asResource(), RDF.subject, stmt.getSubject()));
		m.add(new StatementImpl(violatedTriple.asResource(), RDF.predicate, stmt.getPredicate()));
		m.add(new StatementImpl(violatedTriple.asResource(), RDF.object, stmt.getObject()));
		
		m.add(new StatementImpl(subject, DQMPROB.hasViolatingTriple, violatedTriple));

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
