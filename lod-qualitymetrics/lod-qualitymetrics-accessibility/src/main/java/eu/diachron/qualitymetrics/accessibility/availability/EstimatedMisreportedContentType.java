/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.availability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.WebContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.Pair;
import de.unibonn.iai.eis.diachron.datatypes.Tld;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ReservoirSampler;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.DiachronCacheManager;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource.SerialisableHttpResponse;
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

	// TODO check why parsing slows down at
	// TODO handle unknown host exception (people.comiles.eu,fb.comiles.eu)
	private double misReportedType=0;
	private double correctReportedType=0;
	
	private HTTPRetriever httpRetreiver = new HTTPRetriever();
	private boolean metricCalculated = false;


	final static Logger logger = LoggerFactory.getLogger(EstimatedMisreportedContentType.class);
	boolean followRedirects = true;
	
	private List<Model> _problemList = new ArrayList<Model>();
	
	/**
	 * Constants controlling the maximum number of elements in the reservoir of Top-level Domains and 
	 * Fully Qualified URIs of each TLD, respectively
	 */
	private static int MAX_TLDS = 50;
	private static int MAX_FQURIS_PER_TLD = 10000;
	private ReservoirSampler<Tld> tldsReservoir = new ReservoirSampler<Tld>(MAX_TLDS, true);



	public void compute(Quad quad) {
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
			httpRetreiver.start();

			this.checkForMisreportedContentType();
			this.metricCalculated = true;
		}
		
		double metricValue = correctReportedType / (misReportedType + correctReportedType);

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
		for(Tld tld : this.tldsReservoir.getItems()){
			List<String> uris = tld.getfqUris().getItems(); 
			httpRetreiver.addListOfResourceToQueue(uris);
			httpRetreiver.start();
			
			while(uris.size() > 0){
				String uri = uris.remove(0);
				CachedHTTPResource httpResource = (CachedHTTPResource) DiachronCacheManager.getInstance().getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, uri);
				if (httpResource == null || httpResource.getResponses() == null) {
					uris.add(uri);
					continue;
				}
				for (SerialisableHttpResponse response : httpResource.getResponses()){
					if (response.getHeaders("Status") != null && response.getHeaders("Status").contains("200")){
						String contentDisposition = response.getHeaders("Content-Disposition");
						
						if ((contentDisposition != null) && (contentDisposition.length() > 0)){
							//we can check the file it returns avoiding the loading of file
							String filename = response.getHeaders("Content-Disposition").split(";")[1].replace("filename=\"", "").replace("\"", "");
							Lang language = RDFLanguages.filenameToLang(filename); // if any other type of file but not a LOD compatible, it will return null and we skip
							if (language == null) continue;
							if (response.getHeaders("Content-Type").equals(WebContent.mapLangToContentType(language))) {
								httpResource.setContainsRDF(true);
								correctReportedType++;
							}
							else {
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
							}
							else if (tryP.getSecondElement() == null){
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
	
	private Pair<Boolean, Lang> tryParse(CachedHTTPResource httpResource, SerialisableHttpResponse response){
		for (SerialisableHttpResponse res : httpResource.getResponses()){
			try {
				Lang lang = RDFLanguages.contentTypeToLang(res.getHeaders("Content-Type"));
				RDFDataMgr.loadModel(httpResource.getUri(), lang);
				return new Pair<Boolean, Lang>(true, lang);
			} catch (RiotException e){
				Collection<Lang> langs = this.createLangSet(RDFLanguages.contentTypeToLang(res.getHeaders("Content-Type")));
				for(Lang l : langs){
					try{
						RDFDataMgr.loadModel(httpResource.getUri(), l);
						return new Pair<Boolean, Lang>(false, l);
					}catch (RiotException e1){
						logger.debug("Could not load the model as " + l);
					}
				}
			}
		}
		return new Pair<Boolean, Lang>(false, null);
	}
	
	private Collection<Lang> createLangSet(Lang skipLang){
		Collection<Lang> retSet = RDFLanguages.getRegisteredLanguages();
		retSet.remove(skipLang);
		return retSet;
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
