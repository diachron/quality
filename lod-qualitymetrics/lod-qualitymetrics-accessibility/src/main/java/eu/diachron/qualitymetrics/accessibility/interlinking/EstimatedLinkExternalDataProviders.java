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
import org.mapdb.HTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ReservoirSampler;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ResourceBaseURIOracle;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.DiachronCacheManager;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource.SerialisableHttpResponse;
import eu.diachron.qualitymetrics.utilities.CommonDataStructures;
import eu.diachron.qualitymetrics.utilities.HTTPRetriever;

/**
 * In this metric we identify the total number of external linked used in the dataset. An external link
 * is identified if the subject URI is from one data source and an object URI from ￼another data source.
 * The data source should return RDF data to be considered as 'linked'.
 * In this metric rdf:type triples are skipped since these are not normally considered as part of the
 * Data Level Constant (or Data Level Position). 
 * The value returned by this metric is the number of valid external links a dataset has (i.e. the number
 * of resource links not the number of links to datasets)
 *  
 * In the estimated version of this metric, each PLD found will be tested for RDF data by a sample
 * of the resources used for linking. (See reservoirsize)
 * 
 * Based on: [1] Hogan Aidan, Umbrich Jürgen. An empirical survey of Linked Data conformance. Section 5.2, 
 * Linking, Issue VI: Use External URIs (page 26).
 * @author Santiago Londoño 
 */
public class EstimatedLinkExternalDataProviders implements QualityMetric {
	
	private final Resource METRIC_URI = DQM.LinksToExternalDataProvidersMetric;
	
	final static Logger logger = LoggerFactory.getLogger(EstimatedLinkExternalDataProviders.class);
	
	/**
	 * Parameter: default size for the reservoir 
	 */
	private static int reservoirsize = 5;
		
	/**
	 * MapDB database, used to persist the Map containing the instances found to be declared in the dataset
	 */
	private DB mapDB = MapDbFactory.createAsyncFilesystemDB();
	
	/**
	 * A set that holds all unique PLDs together with a sampled set of resources
	 */
	private HTreeMap<String, ReservoirSampler<String>> mapPLDs =  mapDB.createHashMap("estimated-link-external-data-providers").make();
	
	/**
	 * A set that holds all unique PLDs that return RDF data
	 */
	private Set<String> setPLDsRDF = mapDB.createHashSet("link-external-data-providers-rdf").make();

	/**
     * Object used to determine the base URI of the resource based on its contents
     */
	
	private boolean computed = false;
	private Queue<String> notFetchedQueue = new ConcurrentLinkedQueue<String>();
	private DiachronCacheManager dcmgr = DiachronCacheManager.getInstance();
	private HTTPRetriever httpRetriever = new HTTPRetriever();
	private List<Quad> _problemList = new ArrayList<Quad>();


	
	/**
	 * Processes a single quad making part of the dataset. Determines whether the subject and/or object of the quad 
	 * are data-level URIs, if so, extracts their pay-level domain and adds them to the set of TLD URIs.
	 * @param quad Quad to be processed as part of the computation of the metric
	 */
	public void compute(Quad quad) {
		if (!(quad.getPredicate().matches(RDF.type.asNode()))){
			String subjectPLD = "";
			String objectPLD = "";
			
			if (quad.getSubject().isURI()) subjectPLD = ResourceBaseURIOracle.extractPayLevelDomainURI(quad.getSubject().toString());
			if (quad.getObject().isURI()) objectPLD = ResourceBaseURIOracle.extractPayLevelDomainURI(quad.getObject().toString());
			
			if (!(subjectPLD.equals(objectPLD))){
				if (quad.getSubject().isURI()) this.addUriToSampler(quad.getSubject().toString());
				if (quad.getObject().isURI()) this.addUriToSampler(quad.getSubject().toString());
			}
		}
	}
	
	private void addUriToSampler(String uri) {
		String pld = ResourceBaseURIOracle.extractPayLevelDomainURI(uri);
		
		if (this.mapPLDs.containsKey(pld)){
			ReservoirSampler<String> res = this.mapPLDs.get(pld);
			res.add(uri);
			mapPLDs.put(pld, res);
		} else {
			ReservoirSampler<String> res = new ReservoirSampler<String>(reservoirsize, true);
			res.add(uri);
			mapPLDs.put(pld, res);
		}
		
	}

	/**
	 * Compute the value of the metric as the ratio between the number of different TLDs found among the data-level 
	 * constants of the resource that are different of the resource's TLD and the total number of 
	 * data-level constant URIs found in the resource.
	 * @return value of the existence of links to external data providers metric computed on the current resource
	 */	
	public double metricValue() {
		if (!computed){
			//remove the base uri from the set because that will not be an "external link"
			String baseURI = EnvironmentProperties.getInstance().getDatasetURI();
			
			Iterator<String> iterator = mapPLDs.keySet().iterator();
			while (iterator.hasNext()) {
			    String element = iterator.next();
			    if (element.contains(baseURI)) iterator.remove();
			}
			
			this.checkForRDFLinks();
			computed = true;
		}
		
		
		return mapPLDs.size();
	}
	
	private void checkForRDFLinks() {
		HTreeMap<String, Integer> mapPLDtotres =  mapDB.createHashMap("tempMapToRes").make();
		HTreeMap<String, Integer> mapPLDtotresRDF =  mapDB.createHashMap("tempMapToResRDF").make();

		for(String key : this.mapPLDs.keySet()){
			ReservoirSampler<String> resources = this.mapPLDs.get(key);
			List<String> uriSet = resources.getItems(); 
			mapPLDtotres.put(key, uriSet.size());
			httpRetriever.addListOfResourceToQueue(uriSet);
			this.notFetchedQueue.addAll(uriSet);
			httpRetriever.start();
		}
		
		while (this.notFetchedQueue.size() > 0){
			String uri = this.notFetchedQueue.poll();
			CachedHTTPResource httpResource = (CachedHTTPResource) dcmgr.getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, uri);			
			if (httpResource == null || httpResource.getStatusLines() == null) {
				this.notFetchedQueue.add(uri);
			} else {
				if (httpResource.isContainsRDF() != null){
					if (httpResource.isContainsRDF()) {
						String pld = ResourceBaseURIOracle.extractPayLevelDomainURI(httpResource.getUri());
						if (mapPLDtotresRDF.containsKey(pld)) mapPLDtotresRDF.put(pld, mapPLDtotresRDF.get(pld) + 1);
						else mapPLDtotresRDF.put(pld, 1);
					}
				} else {
					if (this.is200AnRDF(httpResource)){
						String pld = ResourceBaseURIOracle.extractPayLevelDomainURI(httpResource.getUri());
						if (mapPLDtotresRDF.containsKey(pld)) mapPLDtotresRDF.put(pld, mapPLDtotresRDF.get(pld) + 1);
						else mapPLDtotresRDF.put(pld, 1);
					}
				}
			}
		}
		
		// if more than 50% of the resources in the sampler return RDF, then 
		// we assume that PLD domain return RDF data thus adding it to setPLDsRDF
		for(String plds : mapPLDtotres.keySet()){
			Integer iOri = mapPLDtotres.get(plds);
			Integer iRes = mapPLDtotresRDF.get(plds);
			
			if(iOri != null && iRes != null) {
				double ori = iOri.doubleValue();
				double res = iRes.doubleValue();
				double perc = ((res * 100) / ori);
	
				if (perc > 50.0) setPLDsRDF.add(plds);
				else {
					Quad q = new Quad(null, ModelFactory.createDefaultModel().createResource(plds).asNode(), QPRO.exceptionDescription.asNode(), DQM.LowPercentageOfValidPLDResources.asNode());
					this._problemList.add(q);
				}
			} else {
				logger.warn("Computation of percentage for PLD: {} aborted. ORI and/or RES could not be retrieved: ORI: {}, RES: {}", plds, iOri, iRes);
			}
		}
	}

	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

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
		return true;
	}

	@Override
	public Resource getAgentURI() {
		return 	DQM.LuzzuProvenanceAgent;
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