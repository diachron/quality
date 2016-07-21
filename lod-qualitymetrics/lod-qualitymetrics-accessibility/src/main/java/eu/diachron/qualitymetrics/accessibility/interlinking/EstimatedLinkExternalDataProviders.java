package eu.diachron.qualitymetrics.accessibility.interlinking;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.RedirectLocations;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ReservoirSampler;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ResourceBaseURIOracle;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import eu.diachron.qualitymetrics.utilities.AbstractQualityMetric;

/**
 * In this metric we identify the total number of external linked used in the dataset. An external link 
 * is identified if the object's resource URI in a triple has a PLD different than the assessed dataset's PLD.
 * 
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
public class EstimatedLinkExternalDataProviders extends AbstractQualityMetric {
	
	private final Resource METRIC_URI = DQM.LinksToExternalDataProvidersMetric;
	
	final static Logger logger = LoggerFactory.getLogger(EstimatedLinkExternalDataProviders.class);
	
	/**
	 * Parameter: default size for the reservoir 
	 */
	public int reservoirsize = 25;
	
	
	/**
	 * A set that holds all unique PLDs together with a sampled set of resources
	 */
	private Map<String, ReservoirSampler<String>> mapPLDs =  new HashMap<String, ReservoirSampler<String>>();
	
	
	/**
	 * A set that holds all unique PLDs that return RDF data
	 */
	private Set<String> setPLDsRDF = new HashSet<String>();

	
	private boolean computed = false;
	private List<Quad> _problemList = new ArrayList<Quad>();
	
	private String localPLD = null;
	
	private Set<String> ns404 = new HashSet<String>();
	private Map<String,String> resolver = new HashMap<String,String>();
	
	/**
	 * Processes a single quad making part of the dataset. Determines whether the subject and/or object of the quad 
	 * are data-level URIs, if so, extracts their pay-level domain and adds them to the set of TLD URIs.
	 * @param quad Quad to be processed as part of the computation of the metric
	 */
	public void compute(Quad quad) {
		logger.debug("Computing : {} ", quad.asTriple().toString());
		
		if (localPLD == null){
			System.out.println("Dataset URI: "+ this.getDatasetURI());
			localPLD = ResourceBaseURIOracle.extractPayLevelDomainURI(this.getDatasetURI());
		}
		
		if (!(quad.getPredicate().getURI().equals(RDF.type.getURI()))){
			if ((quad.getObject().isURI()) && (!(ResourceBaseURIOracle.extractPayLevelDomainURI(quad.getObject().getURI()).equals(localPLD)))){
				if ((quad.getObject().getURI().startsWith("http")) || (quad.getObject().getURI().startsWith("https"))){
					if ((ResourceBaseURIOracle.extractPayLevelDomainURI(quad.getObject().getURI()).equals("purl.org"))
							|| (ResourceBaseURIOracle.extractPayLevelDomainURI(quad.getObject().getURI()).equals("w3id.org"))){
						String ns = ResourceBaseURIOracle.extractNameSpace(quad.getObject().getURI()); 
						String ext = null;
						if (!(ns404.contains(ns))){
							if (resolver.containsKey(ns)) ext = resolver.get(ns);
							else {
								ext = this.getRedirection(quad.getObject().getURI());
								if (ext != null) 
									resolver.put(ns, ext);	
							}
							if (ext == null) ns404.add(ns);
		//					if (ext == null) this.addUriToSampler(quad.getObject().toString()); // do not put purl.org uris 
							if ((ext != null) && (!(ResourceBaseURIOracle.extractPayLevelDomainURI(ext).equals(localPLD)))) this.addUriToSampler(ext);
						}
					}
					else
						this.addUriToSampler(quad.getObject().toString());
				}
			}
		}
	}
	
	private void addUriToSampler(String uri) {
		String pld = ResourceBaseURIOracle.extractPayLevelDomainURI(uri);
		
		if(pld != null) {
			if (this.mapPLDs.containsKey(pld)){
				ReservoirSampler<String> res = this.mapPLDs.get(pld);
				if (res.findItem(uri) == null) res.add(uri);
				mapPLDs.put(pld, res);
			} else {
				ReservoirSampler<String> res = new ReservoirSampler<String>(reservoirsize, true);
				res.add(uri);
				mapPLDs.put(pld, res);
			}
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
			this.checkForRDFLinks();
			computed = true;
			
			statsLogger.info("EstimatedLinkExternalDataProviders. Dataset: {} - # Top Level Domains (Possible External Linked Data Providers) : {};", 
					this.getDatasetURI(), mapPLDs.size());
		}
		
		return setPLDsRDF.size();
	}
	
	private void checkForRDFLinks() {	
		ExecutorService service = Executors.newCachedThreadPool();
 		for (ReservoirSampler<String> curPldUris : this.mapPLDs.values()) {
			for (String s : curPldUris.getItems()){
				Future<Boolean> future = service.submit(new ParsableContentChecker(s));
				try {
					boolean isParsable = future.get(3, TimeUnit.SECONDS);
					if (isParsable) {
						setPLDsRDF.add(ResourceBaseURIOracle.extractPayLevelDomainURI(s));
						break; // we have a dereferenceable Linked Data source.
					} 
				} catch (InterruptedException | ExecutionException
						| TimeoutException e) {
				}
			}
		}
 		
// 		for (ReservoirSampler<String> curPldUris : this.mapPLDs.values()) {
//			for (String s : curPldUris.getItems()){
//				System.out.println(s);
//				if (isParsableContent(s)) {
//					setPLDsRDF.add(ResourceBaseURIOracle.extractPayLevelDomainURI(s));
//					break; // we have a dereferenceable Linked Data source.
//				} 
//			}
//		}
	}
	
//	private boolean isParsableContent(String uri){
//		final String ns = ModelFactory.createDefaultModel().createResource(uri).getNameSpace();
//		if (this.ns404.contains(ns)) return false;
//		
//		try{
//			return (RDFDataMgr.loadModel(uri).size() > 0);
//		} catch (HttpException httpE){
//			if (httpE.getResponseCode() == 404) ns404.add(ns);
//			return false;
//		} catch (Exception e){
//			return false;
//		}
//	}

	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	public ProblemList<?> getQualityProblems() {
		ProblemList<Quad> pl = null;
		try {
			if(this._problemList != null && this._problemList.size() > 0) {
				pl = new ProblemList<Quad>(this._problemList);
			} else {
				pl = new ProblemList<Quad>();
			}
		} catch (ProblemListInitialisationException e) {
			logger.error(e.getMessage());
		}
		return pl;
	}
	
	
	private String getRedirection(String resource){
		HttpHead head = new HttpHead(resource);
		
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(1000)
				.setConnectTimeout(1000)
				.setRedirectsEnabled(true)
				.build();

		CloseableHttpClient httpClient = HttpClientBuilder
									.create()
									.setDefaultRequestConfig(requestConfig)
									.build();
		
        HttpContext context = new BasicHttpContext(); 
        CloseableHttpResponse response = null;
        
		try {
			response = httpClient.execute(head,context);
			RedirectLocations locations = (RedirectLocations) context.getAttribute(HttpClientContext.REDIRECT_LOCATIONS);
			if (locations.size() == 1) return locations.get(0).toString();
			for(URI loc : locations.getAll()){
				if ((loc.toString().contains("purl.org")) || (loc.toString().contains("w3id.org"))) continue;
				else return loc.toString();
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}		
		return null;
	}
	
	@Override
	public boolean isEstimate() {
		return true;
	}

	@Override
	public Resource getAgentURI() {
		return 	DQM.LuzzuProvenanceAgent;
	}
	
	
	class ParsableContentChecker implements Callable<Boolean>{
		
		String uri = "";
		
		public ParsableContentChecker(String uri){
			this.uri = uri;
		}

		@Override
		public Boolean call() throws Exception {
			final String ns = ResourceBaseURIOracle.extractNameSpace(uri);
			if (ns404.contains(ns)) return false;
			
			try{
				return (RDFDataMgr.loadModel(uri).size() > 0);
			} catch (HttpException httpE){
				if (httpE.getResponseCode() == 404) ns404.add(ns);
				return false;
			} catch (Exception e){
				return false;
			}
		}
	}
}