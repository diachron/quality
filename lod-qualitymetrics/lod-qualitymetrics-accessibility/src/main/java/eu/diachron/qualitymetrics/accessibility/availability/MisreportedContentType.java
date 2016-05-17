package eu.diachron.qualitymetrics.accessibility.availability;

import java.util.ArrayList;
import java.util.Collections;
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
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.semantics.DQMPROB;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.accessibility.availability.helper.Dereferencer;
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
 *         In "Misreported Content Type" metric we check if RDF/XML content is
 *         returned with a reported type other than application/rdf+xml
 * 
 *         Approach: Check the content type returned by the URI and check if we
 *         can parse it. If it is parsible and not of application/rdf+xml then
 *         it is a misreported content type.
 * 
 */
public class MisreportedContentType extends AbstractQualityMetric {

	private final Resource METRIC_URI = DQM.MisreportedContentTypesMetric;

	private double misReportedType=0;
	private double correctReportedType=0;
	
	private HTTPRetriever httpRetreiver = new HTTPRetriever();
	private boolean metricCalculated = false;
	private List<String> uriSet = Collections.synchronizedList(new ArrayList<String>());
	

	static Logger logger = LoggerFactory.getLogger(MisreportedContentType.class);
	boolean followRedirects = true;
	
	private List<Model> _problemList = new ArrayList<Model>();


	public void compute(Quad quad) {
		logger.debug("Computing : {} ", quad.asTriple().toString());
		
		String subject = quad.getSubject().toString();
		if (httpRetreiver.isPossibleURL(subject)){
			httpRetreiver.addResourceToQueue(subject);
			
			if(!uriSet.contains(subject)) {
				uriSet.add(subject);
			}
		}
		
		String object = quad.getObject().toString();
		if (httpRetreiver.isPossibleURL(object)){
			httpRetreiver.addResourceToQueue(object);
			
			if(!uriSet.contains(object)) {
				uriSet.add(object);
			}
		}
	}

	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	public double metricValue() {
		if (!this.metricCalculated){
			httpRetreiver.start(true);

			this.checkForMisreportedContentType();
			this.metricCalculated = true;
			httpRetreiver.stop();
		}
		
		double metricValue = 0.0;
						
		if((misReportedType + correctReportedType) != 0.0) {
			metricValue = correctReportedType / (misReportedType + correctReportedType);
		}
		
		statsLogger.info("MisreportedContentType. Dataset: {} - # Correct : {}; # Misreported : {};", 
				EnvironmentProperties.getInstance().getDatasetURI(), correctReportedType, misReportedType);

		return metricValue;
	}
	
	private void checkForMisreportedContentType(){		
		httpRetreiver.addListOfResourceToQueue(uriSet);
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
		m.add(new StatementImpl(subject, DQMPROB.expectedContentType, m.createLiteral(expectedContentType)));
		m.add(new StatementImpl(subject, DQMPROB.actualContentType, m.createLiteral(actualContentType)));
		
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
