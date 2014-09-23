package eu.diachron.qualitymetrics.accessibility.availability;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.http.HttpResponse;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.DiachronCacheManager;
import eu.diachron.qualitymetrics.utilities.HTTPRetreiver;
import eu.diachron.semantics.vocabulary.DQM;

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
	
	private HTTPRetreiver httpRetreiver = new HTTPRetreiver();
	private boolean metricCalculated = false;
	private Set<String> uriSet = Collections.synchronizedSet(new HashSet<String>());


	static Logger logger = Logger.getLogger(MisreportedContentType.class);
	boolean followRedirects = true;
	String contentNegotiation = "application/rdf+xml";


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
			try {
				httpRetreiver.start();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			this.checkForMisreportedContentType();
			this.metricCalculated = true;
		}
		
		double metricValue = correctReportedType / (misReportedType + correctReportedType);

		return metricValue;
	}

	
	
	private void checkForMisreportedContentType(){
		for(String uri : uriSet){
			CachedHTTPResource httpResource = (CachedHTTPResource) DiachronCacheManager.getInstance().getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, uri);
			if (httpResource.getResponses() == null) continue;
			for (HttpResponse response : httpResource.getResponses()){
				if (response.getStatusLine().getStatusCode() == 200){
					if (response.getHeaders("Content-Disposition").length > 0){
						//therefore some file can be downloaded
						String filename = response.getHeaders("Content-Disposition")[0].getValue().replace("filename=\"", "").replace("\"", "");
						Lang language = RDFLanguages.filenameToLang(filename); // if any other type of file but not a LOD compatible, it will return null and we skip
						if (language == null) continue;
						if (response.getEntity().getContentType().getValue().equals(WebContent.mapLangToContentType(language))) correctReportedType++;
						else {
							System.out.println(uri + " - " + response.getEntity().getContentType().getValue() + " - " + filename);
							misReportedType++;
						}
						break;
					}
				}
			}
			
		}
	}
	
	public ProblemList<?> getQualityProblems() {
		// TODO Auto-generated method stub
		return null;
	}

}
