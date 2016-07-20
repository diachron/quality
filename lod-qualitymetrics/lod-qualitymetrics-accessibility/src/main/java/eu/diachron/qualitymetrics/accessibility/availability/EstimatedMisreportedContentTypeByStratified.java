/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.availability;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.WebContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.datatypes.Tld;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.semantics.DQMPROB;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ReservoirSampler;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.accessibility.availability.helper.ModelParser;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource.SerialisableHttpResponse;
import eu.diachron.qualitymetrics.cache.DiachronCacheManager;
import eu.diachron.qualitymetrics.utilities.AbstractQualityMetric;
import eu.diachron.qualitymetrics.utilities.HTTPResourceUtils;
import eu.diachron.qualitymetrics.utilities.HTTPRetriever;

/**
 * @author Jeremy Debattista
 * 
 * In "Misreported Content Type" metric we check if semantic content is
 * returned with a reported type other than its expected serialisation format type
 * 
 * In the estimated version of this metric, we make use of reservoir 
 * sampling in order to sample a set of TDL and FQU (fully qualified 
 * uri), similar to the approach taken for EstimatedDereferenceability
 * 
 */
public class EstimatedMisreportedContentTypeByStratified extends AbstractQualityMetric{
	private final Resource METRIC_URI = DQM.MisreportedContentTypesMetric;

	

	private HTTPRetriever httpRetriever = new HTTPRetriever();


	private static Logger logger = LoggerFactory.getLogger(EstimatedMisreportedContentTypeByStratified.class);
	boolean followRedirects = true;
	
	private List<Model> _problemList = new ArrayList<Model>();
	
	/**
	 * Constants controlling the maximum number of elements in the reservoir of Top-level Domains and 
	 * Fully Qualified URIs of each TLD, respectively
	 */
	public int MAX_TLDS = 50;
	public int MAX_FQURIS_PER_TLD = 3000;
	private ReservoirSampler<Tld> tldsReservoir = new ReservoirSampler<Tld>(MAX_TLDS, true);
	
	
	/**
	 * Stratified Sampling parameters
	 */
	private static double POPULATION_PERCENTAGE = 0.2d;
	private Map<String,Long> tldCount = new ConcurrentHashMap<String,Long>(); 
	private Long totalURIs = 0l;
	private Long nonSemanticResources = 0l;


	public void compute(Quad quad) {
		logger.debug("Computing : {} ", quad.asTriple().toString());
		
		if (!(quad.getPredicate().getURI().equals(RDF.type.getURI()))){ 
			
			String subject = quad.getSubject().toString();
			if (httpRetriever.isPossibleURL(subject)) {
				logger.trace("URI found on subject: {}", subject);
				addURIToReservoir(subject);
				String uriTLD = httpRetriever.extractTopLevelDomainURI(subject);
				totalURIs++;
				if (tldCount.containsKey(uriTLD)){
					Long cur = tldCount.get(uriTLD) + 1;
					tldCount.put(uriTLD, cur);
				} else {
					tldCount.put(uriTLD, 0l);
				}
			}

			String object = quad.getObject().toString();
			if (httpRetriever.isPossibleURL(object)) {
				logger.trace("URI found on object: {}", object);
				addURIToReservoir(object);
				String uriTLD = httpRetriever.extractTopLevelDomainURI(object);
				totalURIs++;
				if (tldCount.containsKey(uriTLD)){
					Long cur = tldCount.get(uriTLD) + 1;
					tldCount.put(uriTLD, cur);
				} else {
					tldCount.put(uriTLD, 0l);
				}
			}
		}
	}

	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	public double metricValue() {
		// Collect the list of URIs of the TLDs, to be dereferenced
		List<String> lstUrisToDeref = new ArrayList<String>(MAX_FQURIS_PER_TLD);			
		
		for(Tld tld : this.tldsReservoir.getItems()){
			//Work out ratio for the number of maximum TLDs in Reservior
			double totalRatio = ((double) tldCount.get(tld.getUri())) * POPULATION_PERCENTAGE;  // ratio between the total number of URIs of a TLD in a dataset against the overall total number of URIs
			double representativeRatio = totalRatio / ((double) totalURIs * POPULATION_PERCENTAGE); // the ratio of the URIs of a TLD against the population sample for all URIs in a dataset
			long maxRepresentativeSample = Math.round(representativeRatio * (double) MAX_FQURIS_PER_TLD); // how big should the final reservior for a TLD be wrt the representative ratio
			
			// Re-sample the sample to have the final representative sample
			if (maxRepresentativeSample > 0){
				ReservoirSampler<String> _tmpRes = new ReservoirSampler<String>((int)maxRepresentativeSample, true);
			
				for(String uri : tld.getfqUris().getItems()){
					_tmpRes.add(uri);
				}
				
				lstUrisToDeref.addAll(_tmpRes.getItems());
			}
		}
		double metricValue = 0.0;
		
		
		
		double totalCorrectReportedTypes = this.checkForMisreportedContentType(lstUrisToDeref);
		metricValue = (double)totalCorrectReportedTypes / ((double)lstUrisToDeref.size() - (double)this.nonSemanticResources);
		
		statsLogger.info("EstimatedMisreportedContentType. Dataset: {} - # Correct : {}; Total : {};",  this.getDatasetURI(), totalCorrectReportedTypes, lstUrisToDeref.size());

		return metricValue;
	}

	private void addURIToReservoir(String uri) {
		// Extract the top-level domain (a.k.a pay level domain) and look for it in the reservoir 
		String uriTLD = httpRetriever.extractTopLevelDomainURI(uri);
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
	
	private long checkForMisreportedContentType(List<String> uriSet) {
		// Start the dereferenciation process, which will be run in parallel
		httpRetriever.addListOfResourceToQueue(uriSet);
		httpRetriever.start(true);
		
		List<String> lstURIs = new ArrayList<String>(uriSet);
		long totalCorrect = 0;
				
		// Dereference each and every one of the URIs contained in the specified set
		while(lstURIs.size() > 0) {
			// Remove the URI at the head of the queue of URIs to be dereferenced                

			String headUri = lstURIs.remove(0);
			
			// First, search for the URI in the cache
			CachedHTTPResource httpResource = (CachedHTTPResource)DiachronCacheManager.getInstance().getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, headUri);
			
			if (httpResource == null || httpResource.getStatusLines() == null) {
				// URIs not found in the cache, is still to be fetched via HTTP, add it to the end of the list
				lstURIs.add(headUri);
			} else {
				// URI found in the cache (which means that was fetched at some point), check if successfully dereferenced
				SerialisableHttpResponse res = HTTPResourceUtils.getSemanticResponse(httpResource);
				if (res != null){
					String ct = res.getHeaders("Content-Type").split(";")[0];
					Lang lang = WebContent.contentTypeToLang(ct);
					
					//should the resource be dereferencable?
					if (lang != null){
						//the resource might be a semantic resource
						if (ModelParser.hasRDFContent(httpResource, lang)){
							totalCorrect++;
						} else {
							String expectedCT = HTTPResourceUtils.determineActualContentType(httpResource) ;
							this.createProblemModel(httpResource.getUri(), expectedCT, ct);
						}
					} else {
						System.out.println("lang is null");
						String expectedCT = HTTPResourceUtils.determineActualContentType(httpResource) ;
						this.createProblemModel(httpResource.getUri(), expectedCT, "none");
					}
				} else {
					if (HTTPResourceUtils.isTextXML(httpResource)){
						Lang lang = Lang.RDFXML;
						if (ModelParser.hasRDFContent(httpResource, lang)){
							this.createProblemModel(httpResource.getUri(),  "application/rdf+xml", "text/xml");
						}
						else {
							this.nonSemanticResources++;
						}
					} else {
						this.nonSemanticResources++;
					}
				}
			}
		}
		
		return totalCorrect;
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
	

	private void createProblemModel(String resource, String expectedContentType, String actualContentType){
		Model m = ModelFactory.createDefaultModel();
		
		Resource subject = m.createResource(resource);
		m.add(new StatementImpl(subject, QPRO.exceptionDescription, DQMPROB.MisreportedTypeException));
		if ((expectedContentType == null) || (expectedContentType.equals("")))
			m.add(new StatementImpl(subject, DQMPROB.expectedContentType, m.createLiteral("Unknown Expected Content Type")));
		else m.add(new StatementImpl(subject, DQMPROB.expectedContentType, m.createLiteral(expectedContentType)));
		if ((actualContentType == null) || (actualContentType.equals("")))
			m.add(new StatementImpl(subject, DQMPROB.actualContentType, m.createLiteral("Unknown Content Type")));
		else 
			m.add(new StatementImpl(subject, DQMPROB.actualContentType, m.createLiteral(actualContentType)));
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
