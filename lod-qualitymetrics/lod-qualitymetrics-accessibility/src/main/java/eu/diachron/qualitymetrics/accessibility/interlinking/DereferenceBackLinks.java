/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.interlinking;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
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
import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.semantics.DQMPROB;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.accessibility.availability.DereferenceabilityForwardLinks;
import eu.diachron.qualitymetrics.accessibility.availability.helper.Dereferencer;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.DiachronCacheManager;
import eu.diachron.qualitymetrics.utilities.HTTPResourceUtils;
import eu.diachron.qualitymetrics.utilities.HTTPRetriever;

/**
 * @author Jeremy Debattista
 * 
 * This metric measures the extent to which a resource includes all triples from the dataset that have 
 * the resource's URI as the object. This allows browsers and crawlers to traverse links in either direction, 
 * as specified by Hogan et al. To do so, it computes the ratio between the number of objects that are 
 * "back-links" a.k.a "inlinks" (are part of the resource's URI) and the total number of objects in the resource.
 * 
 * In short, this metric checks if an object resource is adequatly described as a Semantic Resource,
 * thus a valid backlink
 * 
 * Based on: Hogan Aidan, Umbrich Jürgen. An empirical survey of Linked Data conformance.
 * 
 */
public class DereferenceBackLinks implements QualityMetric {
	
	private final Resource METRIC_URI = DQM.DereferenceabilityBackLinksMetric;
	
	final static Logger logger = LoggerFactory.getLogger(DereferenceabilityForwardLinks.class);
		
	private HTTPRetriever httpRetreiver = new HTTPRetriever();

	
	private boolean metricCalculated = false;
	private double metricValue = 0.0;
	
	private double totalValidBacklinks = 0.0;
	private Queue<String> notFetchedQueue = new ConcurrentLinkedQueue<String>();

	
	private List<Model> _problemList = new ArrayList<Model>();
	private NavigableSet<String> uriSet = MapDbFactory.createAsyncFilesystemDB().createTreeSet("uri-set").make();

	
	
	@Override
	public void compute(Quad quad) {
		logger.debug("Computing : {} ", quad.asTriple().toString());
		
		String predicate = quad.getPredicate().getURI();
		
		if (!(predicate.equals(RDF.type))){
			if ((quad.getObject().isURI()) && (!(quad.getObject().isBlank()))){
				httpRetreiver.addResourceToQueue(quad.getObject().getURI());
				uriSet.add(quad.getObject().getURI());
			}
		}
	}

	@Override
	public double metricValue() {
		if (!this.metricCalculated){
			Double uriSize = (double) uriSet.size();
			httpRetreiver.addListOfResourceToQueue(new ArrayList<String>(uriSet));
			httpRetreiver.start(true);

			do {
				this.checkForBackLinking();
				uriSet.clear();
				uriSet.addAll(this.notFetchedQueue);
				this.notFetchedQueue.clear();
			} while(!this.uriSet.isEmpty());
			
			this.metricCalculated = true;
			httpRetreiver.stop();
			
			metricValue = (totalValidBacklinks == 0.0) ? 0.0 : (double) totalValidBacklinks / uriSize;
			
			statsLogger.info("Dereferencable Forward Links Metric: Dataset: {} - Total # Back Links URIs {}; Total URIs : {}", 
					EnvironmentProperties.getInstance().getDatasetURI(), totalValidBacklinks, uriSize);
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
	
	
	/**
	 * The object resource of an assessed triple is dereferenced. For a backlink to be valid
	 * the deferenced representation should have a triple, where the object is found in the subject
	 * position of this triple whilst the subject of the original assessed triple is found in the
	 * object position.
	 */
	int noRequests = 0;
	private void checkForBackLinking(){
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
						
						Model m = null;
						try{
							m = RDFDataMgr.loadModel(httpResource.getUri());
							noRequests++;
						} catch (Exception e){
							System.out.println(httpResource.getUri() + " - noTriedReq: "+noRequests);
							this.notFetchedQueue.add(uri);
							continue;
						}
						Resource r = m.createResource(httpResource.getUri());
						List<Statement> stmtList = m.listStatements(r, (Property) null, (RDFNode) null).toList();
						
						if (stmtList.size() > 1){
							//ok
							logger.info("A description exists for resource {}.", httpResource.getUri());

							totalValidBacklinks++;
						} else {
							//not ok
							this.createNotValidBacklink(httpResource.getUri());
						}
						
					}
				} else {
					logger.info("Non-meaningful dereferencable resource {}.", httpResource.getUri());
					this.createNotValidBacklink(httpResource.getUri());
				}
			}
		}
	}
	
	private void createNotValidBacklink(String resource){
		Model m = ModelFactory.createDefaultModel();
		
		Resource subject = m.createResource(resource);
		m.add(new StatementImpl(subject, QPRO.exceptionDescription, DQMPROB.NotValidDereferenceableBackLink));
		
		this._problemList.add(m);
	}
}
