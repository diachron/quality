/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.interlinking;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.WebContent;
import org.mapdb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ResourceBaseURIOracle;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.DiachronCacheManager;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource.SerialisableHttpResponse;
import eu.diachron.qualitymetrics.utilities.CommonDataStructures;

/**
 * @author Jeremy Debattista
 * 
 * In this metric we identify the total number of external linked used in the dataset. An external link
 * is identified if the subject URI is from one data source and an object URI from ￼another data source.
 * In this metric rdf:type triples are skipped since these are not normally considered as part of the
 * Data Level Constant (or Data Level Position).
 * 
 * Based on: [1] Hogan Aidan, Umbrich Jürgen. An empirical survey of Linked Data conformance. Section 5.2, 
 * Linking, Issue VI: Use External URIs (page 20).
 */
public class LinkExternalDataProviders implements QualityMetric {
	
	/**
	 * MapDB database, used to persist the Map containing the instances found to be declared in the dataset
	 */
	private DB mapDB = MapDbFactory.createAsyncFilesystemDB();
	
	/**
	 * A set that holds all unique PLDs
	 */
	private Set<String> setResources = mapDB.createHashSet("link-external-data-providers").make();

	
	/**
	 * A set that holds all unique PLDs that return RDF data
	 */
	private Set<String> setPLDsRDF = mapDB.createHashSet("link-external-data-providers-rdf").make();
	
	final static Logger logger = LoggerFactory.getLogger(LinkExternalDataProviders.class);

	private ResourceBaseURIOracle baseURIOracle = new ResourceBaseURIOracle();
	private Queue<String> notFetchedQueue = new ConcurrentLinkedQueue<String>();
	private DiachronCacheManager dcmgr = DiachronCacheManager.getInstance();

	private final Resource METRIC_URI = DQM.LinksToExternalDataProvidersMetric;
	
	private List<Quad> _problemList = new ArrayList<Quad>();


	private boolean computed = false;
	
	@Override
	public void compute(Quad quad) {
		
		baseURIOracle.addHint(quad);
		
		if (!(quad.getPredicate().matches(RDF.type.asNode()))){
			String subject = ResourceBaseURIOracle.extractPayLevelDomainURI(quad.getSubject().toString());
			String object = ResourceBaseURIOracle.extractPayLevelDomainURI(quad.getObject().toString());

			
			if (!(subject.equals(object))){
				if (quad.getSubject().isURI()) setResources.add(quad.getSubject().toString());
				if (quad.getObject().isURI()) setResources.add(quad.getSubject().toString());
			}
		}
	}

	@Override
	public double metricValue() {
		if (!computed){
			//remove the base uri from the set because that will not be an "external link"
			String baseURI = baseURIOracle.getEstimatedResourceBaseURI();
			
			Iterator<String> iterator = setResources.iterator();
			while (iterator.hasNext()) {
			    String element = iterator.next();
			    if (element.contains(baseURI)) iterator.remove();
			}
			
			this.checkForRDFLinks();
			computed = true;
		}
		
		
		return setPLDsRDF.size();
	}

	@Override
	public Resource getMetricURI() {
		return METRIC_URI;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		ProblemList<Quad> pl = null;
		try {
			pl = new ProblemList<Quad>(this._problemList);
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
		return DQM.LuzzuProvenanceAgent;
	}
	
	private void checkForRDFLinks() {
		for(String uri : setResources){
			CachedHTTPResource httpResource = (CachedHTTPResource) dcmgr.getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, uri);			
			if (httpResource == null || httpResource.getStatusLines() == null) {
				this.notFetchedQueue.add(uri);
			} else {
				if (httpResource.isContainsRDF() != null){
					if (httpResource.isContainsRDF()) setPLDsRDF.add(httpResource.getUri());
				} else {
					if (this.is200AnRDF(httpResource)) setPLDsRDF.add(httpResource.getUri());
				}
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
								Quad q = new Quad(null, ModelFactory.createDefaultModel().createResource(resource.getUri()).asNode(), QPRO.exceptionDescription.asNode(), DQM.NoValidRDFDataForExternalLink.asNode());
								this._problemList.add(q);
								resource.setContainsRDF(false);
								return false;
							}
						}
						resource.setContainsRDF(true);
						return true;
					}
				}
			}
		}
		Quad q = new Quad(null, ModelFactory.createDefaultModel().createResource(resource.getUri()).asNode(), QPRO.exceptionDescription.asNode(), DQM.NoValidRDFDataForExternalLink.asNode());
		this._problemList.add(q);
		resource.setContainsRDF(false);
		return false;
	}
	
	private Model tryRead(String uri) {
		Model m = ModelFactory.createDefaultModel();
		try{
			m = RDFDataMgr.loadModel(uri, Lang.NTRIPLES);
		} catch (RiotException r) {
			Log.debug("Resource could not be parsed:", r.getMessage());
		}
		return m;
	}
	


}
