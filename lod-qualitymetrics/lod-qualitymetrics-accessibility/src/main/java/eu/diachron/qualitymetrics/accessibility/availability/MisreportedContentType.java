package eu.diachron.qualitymetrics.accessibility.availability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.StatusLine;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.WebContent;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.Pair;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource.SerialisableHttpResponse;
import eu.diachron.qualitymetrics.cache.DiachronCacheManager;
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
public class MisreportedContentType implements QualityMetric {

	private final Resource METRIC_URI = DQM.MisreportedContentTypesMetric;

	// TODO check why parsing slows down at
	// TODO handle unknown host exception (people.comiles.eu,fb.comiles.eu)
	private double misReportedType=0;
	private double correctReportedType=0;
	private double notOkResponses=0;
	
	private HTTPRetriever httpRetreiver = new HTTPRetriever();
	private boolean metricCalculated = false;
	private List<String> uriSet = Collections.synchronizedList(new ArrayList<String>());
	
	/**
	 * Regular expression matching filenames as provided in the Content-Disposition header of HTTP responses.
	 * Note that Pattern instances are thread-safe and are intended to create a new Matcher instance upon each usage
	 */
	private static final Pattern ptnFileName = Pattern.compile(".*filename=([^;\\s]+).*");

	static Logger logger = Logger.getLogger(MisreportedContentType.class);
	boolean followRedirects = true;
	
	private List<Model> _problemList = new ArrayList<Model>();


	public void compute(Quad quad) {
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
			httpRetreiver.start();

			this.checkForMisreportedContentType();
			this.metricCalculated = true;
			httpRetreiver.stop();
		}
		
		double metricValue = 0.0;
		logger.debug(String.format("Computing metric. Correct: %.0f. Misreported: %.0f. Not OK: %.0f", correctReportedType, misReportedType, notOkResponses));
		if((misReportedType + correctReportedType) != 0.0) {
			metricValue = correctReportedType / (misReportedType + correctReportedType);
		}

		return metricValue;
	}
	
	private void checkForMisreportedContentType(){		
		while(uriSet.size() > 0){
			// Get the next URI to be processed and remove it from the set (i.e. the uriSet is used as a queue, the next element is poped)
			String uri = uriSet.remove(0);	
			
			// Check if the URI has already been dereferenced, in which case, it would be part of the Cache
			CachedHTTPResource httpResource = (CachedHTTPResource) DiachronCacheManager.getInstance().getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, uri);
			
			if (httpResource == null || httpResource.getResponses() == null) {
				// If the URI is not part of the cache, add it back in to the uriSet, so that it's tried to be dereferenced by the HTTP Retriever
				if(!uriSet.contains(uri)) {
					uriSet.add(uri);
				}
				continue;
			}
			
			if(hasResponseOK(httpResource)) {
				// URI found in the cache, validate its content-type 
				for (SerialisableHttpResponse response : httpResource.getResponses()){
					String contentDisposition = response.getHeaders("Content-Disposition");
					
					if ((contentDisposition != null) && (contentDisposition.length() > 0)){
						//we can check the file it returns avoiding the loading of file
						Matcher fileNameMatcher = ptnFileName.matcher(response.getHeaders("Content-Disposition"));
						// If the filename is not found in the Content-Disposition header, give up on this response (continue)
						if(!fileNameMatcher.matches()) continue;
						
						String filename = fileNameMatcher.group(1);	// After matching the regular expression, the first capture group will fetch the filename value
						Lang language = RDFLanguages.filenameToLang(filename); // if any other type of file but not a LOD compatible, it will return null and we skip
						
						// If the language could not be determined from the filename, give up on this response (continue)
						if (language == null) continue;
						if (response.getHeaders("Content-Type").equals(WebContent.mapLangToContentType(language))) { 
							httpResource.setContainsRDF(true);
							correctReportedType++;
						} else {
							httpResource.setContainsRDF(false);
							misReportedType++;
							this.createProblemModel(uri, response.getHeaders("Content-Type"), WebContent.mapLangToContentType(language));
						}
						break;
					} else {
						Pair<Boolean, Lang> tryP = this.tryParse(httpResource, response);
						if (tryP.getFirstElement() == true) {
							httpResource.setContainsRDF(true);
							correctReportedType++;
						} else if (tryP.getSecondElement() == null){
							httpResource.setContainsRDF(false);
							misReportedType++;
							this.createProblemModel(uri, response.getHeaders("Content-Type"), "null");
						} else {
							httpResource.setContainsRDF(false);
							misReportedType++;
							this.createProblemModel(uri, response.getHeaders("Content-Type"), tryP.getSecondElement().getName());
						}
					}
				}
			} else {
				logger.debug("URI " + ((httpResource != null)?(httpResource.getUri()):("-")) + " deferenced but response was not 200 OK");
				notOkResponses++;
			}
		}
	}
	
	private boolean hasResponseOK(CachedHTTPResource resource) {
		List<StatusLine> lstStatusLines = resource.getStatusLines();
		
		if(lstStatusLines != null) {
			synchronized(lstStatusLines) {
				return lstStatusLines.toString().contains("200 OK");
			}
		}
		return false;
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
		m.add(new StatementImpl(subject, QPRO.exceptionDescription, DQM.MisreportedTypeException));
		m.add(new StatementImpl(subject, DQM.expectedContentType, m.createLiteral(expectedContentType)));
		m.add(new StatementImpl(subject, DQM.actualContentType, m.createLiteral(actualContentType)));
		
		this._problemList.add(m);
	}
	
	private Pair<Boolean, Lang> tryParse(CachedHTTPResource httpResource, SerialisableHttpResponse response){
		for (SerialisableHttpResponse res : httpResource.getResponses()){
			Lang lang = null;
			try {
				lang = RDFLanguages.contentTypeToLang(res.getHeaders("Content-Type"));
				RDFDataMgr.loadModel(httpResource.getUri(), lang);
				return new Pair<Boolean, Lang>(true, lang);
			} catch (RiotException e){
				// Collection<Lang> langs = this.createLangSet(RDFLanguages.contentTypeToLang(res.getHeaders("Content-Type")));
				Collection<Lang> langs = RDFLanguages.getRegisteredLanguages(); 
				for(Lang l : langs){
					try{
						if(!l.equals(lang)) {
							RDFDataMgr.loadModel(httpResource.getUri(), l);
							return new Pair<Boolean, Lang>(false, l);
						}
					}catch (RiotException e1){
						logger.debug("Could not load the model of " + ((httpResource != null)?(httpResource.getUri()):("NULL")) + " as " + l + 
								" for lang: " + ((lang != null)?(lang):("NULL")));
					}
				}
			}
		}
		return new Pair<Boolean, Lang>(false, null);
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
