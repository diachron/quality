/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.interlinking;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.RedirectLocations;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.jena.riot.RDFDataMgr;
import org.mapdb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ResourceBaseURIOracle;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import eu.diachron.qualitymetrics.utilities.AbstractQualityMetric;

/**
 * @author Jeremy Debattista
 * 
 * In this metric we identify the total number of external linked used in the dataset. An external link
 * is identified if the subject URI is from one data source and an object URI from ￼another data source.
 * The data source should return RDF data to be considered as 'linked'.
 * In this metric rdf:type triples are skipped since these are not normally considered as part of the
 * Data Level Constant (or Data Level Position).
 * The value returned by this metric is the number of valid external links a dataset has (i.e. the number
 * of resource links not the number of links to datasets)
 * 
 * Based on: [1] Hogan Aidan, Umbrich Jürgen. An empirical survey of Linked Data conformance. Section 5.2, 
 * Linking, Issue VI: Use External URIs (page 20).
 */
public class LinkExternalDataProviders extends AbstractQualityMetric {
	
	/**
	 * MapDB database, used to persist the Map containing the instances found to be declared in the dataset
	 */
	private DB mapDB = MapDbFactory.createAsyncFilesystemDB();
	
	/**
	 * A set that holds all unique resources
	 */
	private Set<String> setResources = MapDbFactory.createHashSet(mapDB, UUID.randomUUID().toString());

	
	/**
	 * A set that holds all unique PLDs that return RDF data
	 */
	private Set<String> setPLDsRDF = new HashSet<String>();
	
	final static Logger logger = LoggerFactory.getLogger(LinkExternalDataProviders.class);


	private final Resource METRIC_URI = DQM.LinksToExternalDataProvidersMetric;
	
	private List<Quad> _problemList = new ArrayList<Quad>();

	private boolean computed = false;
	
	private String localPLD = null;

	private Map<String,String> resolver = new HashMap<String,String>();

	
	@Override
	public void compute(Quad quad) {
		logger.debug("Computing : {} ", quad.asTriple().toString());
		
		if (localPLD == null)
			localPLD = ResourceBaseURIOracle.extractPayLevelDomainURI(this.getDatasetURI());
		
		if (!(quad.getPredicate().getURI().equals(RDF.type.getURI()))){
			if ((quad.getObject().isURI()) && (!(ResourceBaseURIOracle.extractPayLevelDomainURI(quad.getObject().getURI()).equals(localPLD)))){
			}
		}
		
		if (!(quad.getPredicate().getURI().equals(RDF.type.getURI()))){
			if ((quad.getObject().isURI()) && (!(ResourceBaseURIOracle.extractPayLevelDomainURI(quad.getObject().getURI()).equals(localPLD)))){
				if ((quad.getObject().getURI().startsWith("http")) || (quad.getObject().getURI().startsWith("https"))){
					if ((ResourceBaseURIOracle.extractPayLevelDomainURI(quad.getObject().getURI()).equals("purl.org"))
							|| (ResourceBaseURIOracle.extractPayLevelDomainURI(quad.getObject().getURI()).equals("w3id.org"))){
						String ns = quad.getObject().getNameSpace();
						String ext = null;
						if (resolver.containsKey(ns)) ext = resolver.get(ns);
						else {
							ext = this.getRedirection(quad.getObject().getURI());
							if (ext != null) 
								resolver.put(ns, ext);	
						}
	//					if (ext == null) this.addUriToSampler(quad.getObject().toString()); // do not put purl.org uris 
						if (!(ResourceBaseURIOracle.extractPayLevelDomainURI(ext).equals(localPLD))) setResources.add(ext);
					}
					else
						setResources.add(quad.getObject().toString());
				}
			}
		}
	}
	@Override
	public double metricValue() {
		if (!computed){
			this.checkForRDFLinks();
			computed = true;
		}
		
		statsLogger.info("LinkExternalDataProviders. Dataset: {} - # Top Level Domains : {};", 
				this.getDatasetURI(), setPLDsRDF.size());
		
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

	@Override
	public boolean isEstimate() {
		return false;
	}

	@Override
	public Resource getAgentURI() {
		return DQM.LuzzuProvenanceAgent;
	}
	
	private void checkForRDFLinks() {
		for (String s : setResources){
			if (setPLDsRDF.contains(ResourceBaseURIOracle.extractPayLevelDomainURI(s))) continue;
			if (isParsableContent(s)) setPLDsRDF.add(ResourceBaseURIOracle.extractPayLevelDomainURI(s));
		}
	}
	
	private String getRedirection(String resource){
		HttpHead head = new HttpHead(resource);
		
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(1000)
				.setConnectTimeout(1000)
				.build();

		CloseableHttpClient httpClient = HttpClientBuilder
									.create()
									.setDefaultRequestConfig(requestConfig)
									.build();
		
        HttpContext context = new BasicHttpContext(); 

		try {
			httpClient.execute(head,context);
			RedirectLocations locations = (RedirectLocations) context.getAttribute(HttpClientContext.REDIRECT_LOCATIONS);
			if (locations.size() == 1) return locations.get(0).toString();
			for(URI loc : locations.getAll()){
				if ((loc.toString().contains("purl.org")) || (loc.toString().contains("w3id.org"))) continue;
				else return loc.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return null;
	}
	
	private boolean isParsableContent(String uri){
		try{
			return (RDFDataMgr.loadModel(uri).size() > 0);
		} catch (Exception e){
			return false;
		}
	}

}
