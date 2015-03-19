/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.availability;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ResourceBaseURIOracle;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.VOID;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource.SerialisableHttpResponse;
import eu.diachron.qualitymetrics.cache.DiachronCacheManager;
import eu.diachron.qualitymetrics.utilities.CommonDataStructures;
import eu.diachron.qualitymetrics.utilities.HTTPRetriever;

/**
 * @author Jeremy Debattista
 * 
 * Check if ALL data dumps (void:dataDump) exist, are reachable and parsable.
 *     
 */
public class RDFAccessibility implements QualityMetric {
	
	static Logger logger = Logger.getLogger(RDFAccessibility.class);
	
	protected List<Quad> problemList = new ArrayList<Quad>();

	private final Resource METRIC_URI = DQM.RDFAvailabilityMetric;
	
	private DiachronCacheManager dcmgr = DiachronCacheManager.getInstance();

	
	private double metricValue = 0.0d;
	
	private double totalDataDumps = 0.0d;
	private double workingDataDumps = 0.0d;
	private List<String> dataDumpsURIs = new ArrayList<String>();
	private HTTPRetriever httpRetreiver = new HTTPRetriever();
	
	private boolean metricCalculated = false;
	
	private ResourceBaseURIOracle oracle = new ResourceBaseURIOracle();

	public void compute(Quad quad) {
		oracle.addHint(quad);
		if (quad.getPredicate().getURI().equals(VOID.dataDump.getURI())) {
			httpRetreiver.addResourceToQueue(quad.getObject().getURI());
			dataDumpsURIs.add(quad.getObject().getURI());
			totalDataDumps++;
		}
	}

	public double metricValue() {
		if (!metricCalculated){
			this.checkForRDFDataset();
			metricCalculated = true;
		}
		metricValue = workingDataDumps / totalDataDumps;
		return metricValue;
	}


	public Resource getMetricURI() {
		return this.METRIC_URI;
	}
	
	private void checkForRDFDataset(){
		httpRetreiver.start();
		
		for (String uri : dataDumpsURIs){
			if (DiachronCacheManager.getInstance().existsInCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, uri)){
				CachedHTTPResource httpResource = (CachedHTTPResource) dcmgr.getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, uri);
				
				for(SerialisableHttpResponse response : httpResource.getResponses()){
					if ((response.getHeaders("Status").contains("200")) && (CommonDataStructures.ldContentTypes.contains(response.getHeaders("Content-Type")))){
						workingDataDumps++;
						break;
					} else {
						Resource subject = ModelFactory.createDefaultModel().createResource(uri);
						Quad q = new Quad(null, subject.asNode() , QPRO.exceptionDescription.asNode(), DQM.InvalidDataDumpURI.asNode());
						this.problemList.add(q);
					}
				}
			}
		}
	}

	public ProblemList<?> getQualityProblems() {
		ProblemList<Quad> tmpProblemList = null;
		
		if (this.metricValue() == 0){
			String resource = this.oracle.getEstimatedResourceDatasetURI();
			Resource subject = ModelFactory.createDefaultModel().createResource(resource);
			Quad q = new Quad(null, subject.asNode() , QPRO.exceptionDescription.asNode(), DQM.NoRDFAccessibility.asNode());
			this.problemList.add(q);
		}
		
		try {
			tmpProblemList = new ProblemList<Quad>(this.problemList);
		} catch (ProblemListInitialisationException problemListInitialisationException) {
			logger.debug(problemListInitialisationException.getStackTrace());
			logger.error(problemListInitialisationException.getMessage());
		}
		return tmpProblemList;
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
