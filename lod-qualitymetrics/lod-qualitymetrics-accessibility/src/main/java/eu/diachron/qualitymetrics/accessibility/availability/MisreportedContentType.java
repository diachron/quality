package eu.diachron.qualitymetrics.accessibility.availability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.sparql.core.Quad;

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
	
	private HTTPRetriever httpRetreiver = new HTTPRetriever();
	private boolean metricCalculated = false;
	private List<String> uriSet = Collections.synchronizedList(new ArrayList<String>());


	static Logger logger = Logger.getLogger(MisreportedContentType.class);
	boolean followRedirects = true;
	
	private List<Model> _problemList = new ArrayList<Model>();


	public void compute(Quad quad) {
		String subject = quad.getSubject().toString();
		if (httpRetreiver.isPossibleURL(subject)){
			httpRetreiver.addResourceToQueue(subject);
			uriSet.add(subject);
		}
		
		String object = quad.getObject().toString();
		if (httpRetreiver.isPossibleURL(object)){
			httpRetreiver.addResourceToQueue(object);
			uriSet.add(object);
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
		
		double metricValue = correctReportedType / (misReportedType + correctReportedType);

		return metricValue;
	}
	
	private void checkForMisreportedContentType(){
		while(uriSet.size() > 0){
			String uri = uriSet.remove(0);
			CachedHTTPResource httpResource = (CachedHTTPResource) DiachronCacheManager.getInstance().getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, uri);
			if (httpResource.getResponses() == null) {
				uriSet.add(uri);
				continue;
			}
			for (SerialisableHttpResponse response : httpResource.getResponses()){
				if (response.getHeaders("Status").contains("200")){
					//maybe use RDFDataMgr.determineLang(target, ctStr, hintLang)
					if (response.getHeaders("Content-Disposition").length() > 0){
						//therefore some file can be downloaded
						String filename = response.getHeaders("Content-Disposition").split(";")[1].replace("filename=\"", "").replace("\"", "");
						Lang language = RDFLanguages.filenameToLang(filename); // if any other type of file but not a LOD compatible, it will return null and we skip
						if (language == null) continue;
						if (response.getHeaders("Content-Type").equals(WebContent.mapLangToContentType(language))) correctReportedType++;
						else {
							misReportedType++;
							this.createProblemModel(uri, response.getHeaders("Content-Type"), WebContent.mapLangToContentType(language));
						}
						break;
					}
				}
			}
			
		}
	}
	
	public ProblemList<?> getQualityProblems() {
		ProblemList<Model> pl = null;
		try {
			pl = new ProblemList<Model>(this._problemList);
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

}
