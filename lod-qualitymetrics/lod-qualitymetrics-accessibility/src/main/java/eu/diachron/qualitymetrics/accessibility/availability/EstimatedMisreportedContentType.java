/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.availability;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.StatusCode;
import de.unibonn.iai.eis.diachron.datatypes.Tld;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.semantics.DQMPROB;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ReservoirSampler;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.accessibility.availability.helper.Dereferencer;
import eu.diachron.qualitymetrics.accessibility.availability.helper.ModelParser;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.DiachronCacheManager;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource.SerialisableHttpResponse;
import eu.diachron.qualitymetrics.utilities.HTTPResourceUtils;
import eu.diachron.qualitymetrics.utilities.HTTPRetriever;

/**
 * @author Jeremy Debattista
 * 
 * In "Misreported Content Type" metric we check if RDF/XML content is
 * returned with a reported type other than application/rdf+xml
 * 
 * In the estimated version of this metric, we make use of reservoir 
 * sampling in order to sample a set of TDL and FQU (fully qualified 
 * uri), similar to the approach taken for EstimatedDereferenceability
 * 
 */
public class EstimatedMisreportedContentType implements QualityMetric{
	private final Resource METRIC_URI = DQM.MisreportedContentTypesMetric;

	private double misReportedType=0;
	private double correctReportedType=0;
	private double notOkResponses=0;
	

	private HTTPRetriever httpRetreiver = new HTTPRetriever();
	private boolean metricCalculated = false;


	private static Logger logger = LoggerFactory.getLogger(EstimatedMisreportedContentType.class);
	boolean followRedirects = true;
	
	private List<Model> _problemList = new ArrayList<Model>();
	
	/**
	 * Constants controlling the maximum number of elements in the reservoir of Top-level Domains and 
	 * Fully Qualified URIs of each TLD, respectively
	 */
	private static int MAX_TLDS = 10;
	private static int MAX_FQURIS_PER_TLD = 250;
	private ReservoirSampler<Tld> tldsReservoir = new ReservoirSampler<Tld>(MAX_TLDS, true);
	private List<String> uriSet = new ArrayList<String>();


	public void compute(Quad quad) {
		logger.debug("Computing : {} ", quad.asTriple().toString());
		
		String subject = quad.getSubject().toString();
		if (httpRetreiver.isPossibleURL(subject)){
			addURIToReservoir(subject);
		}
		
		String object = quad.getObject().toString();
		if (httpRetreiver.isPossibleURL(object)){
			this.addURIToReservoir(object);
		}
	}

	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	public double metricValue() {
		if (!this.metricCalculated){
			for(Tld tld : this.tldsReservoir.getItems()){
				uriSet.addAll(tld.getfqUris().getItems()); 
			}
			
			httpRetreiver.addListOfResourceToQueue(uriSet);
			
			httpRetreiver.start(true);

			this.checkForMisreportedContentType();
			this.metricCalculated = true;
			httpRetreiver.stop();
		}
		
		double metricValue = 0.0;
		logger.debug(String.format("Computing metric. Correct: %.0f. Misreported: %.0f. Not OK: %.0f", correctReportedType, misReportedType, notOkResponses));
		
		statsLogger.info("EstimatedMisreportedContentType. Dataset: {} - # Correct : {}; # Misreported : {}. # Not OK : {};", 
				EnvironmentProperties.getInstance().getDatasetURI(), correctReportedType, misReportedType, notOkResponses);
		
		if((misReportedType + correctReportedType) != 0.0) {
			metricValue = correctReportedType / (misReportedType + correctReportedType);
		}

		return metricValue;
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
	
	
	private void checkForMisreportedContentType(){
		while(uriSet.size() > 0){
			// Get the next URI to be processed and remove it from the set (i.e. the uriSet is used as a queue, the next element is poped)
			String uri = uriSet.remove(0);	
			
			// Check if the URI has already been dereferenced, in which case, it would be part of the Cache
			CachedHTTPResource httpResource = (CachedHTTPResource) DiachronCacheManager.getInstance().getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, uri);
			
			if (httpResource == null || (httpResource.getResponses() == null && httpResource.getDereferencabilityStatusCode() != StatusCode.BAD)) {
				// If the URI is not part of the cache, add it back in to the uriSet, so that it's tried to be dereferenced by the HTTP Retriever
				if(!uriSet.contains(uri)) {
					uriSet.add(uri);
				}
				continue;
			}
			
			if (Dereferencer.hasOKStatus(httpResource)){
				logger.info("Checking "+httpResource.getUri()+ " for misreported content type");
				
				SerialisableHttpResponse res = HTTPResourceUtils.getSemanticResponse(httpResource);
				if (res != null){
					String ct = res.getHeaders("Content-Type");
					Lang lang = WebContent.contentTypeToLang(ct);
					
					//should the resource be dereferencable?
					if (lang != null){
						//the resource might be a semantic resource
						if (ModelParser.hasRDFContent(httpResource, lang)){
							correctReportedType++;
						} else {
							misReportedType++;
							
							String actualCT = HTTPResourceUtils.determineActualContentType(httpResource) ;
							this.createProblemModel(httpResource.getUri(), ct, actualCT);
						}
					}
				} else {
					logger.info("No semantic content type for {}. Trying to parse the content.", httpResource.getUri());
					SerialisableHttpResponse possible = HTTPResourceUtils.getPossibleSemanticResponse(httpResource); //we are doing this to get more statistical detail for the problem report
					if (possible != null){
						String location = HTTPResourceUtils.getResourceLocation(possible);
						if (location != null){
							Lang language = RDFLanguages.filenameToLang(location); // if the attachment has an non semantic file type, it is skipped
							if (language != null){
								misReportedType++;
								
								String actualCT = HTTPResourceUtils.determineActualContentType(httpResource);
								this.createProblemModel(httpResource.getUri(), possible.getHeaders("Content-Type"), actualCT);
							} else 
								logger.info("Not possible to parse {}. Not a recognised file extension", location);	
						}
					}
					else logger.info("Not possible to parse {}.", httpResource.getUri());
				}
			}
		}
			
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
