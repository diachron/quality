/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.interlinking;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.StatusLine;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.datatypes.Pair;
import de.unibonn.iai.eis.diachron.datatypes.StatusCode;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.semantics.utilities.Commons;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.accessibility.availability.DereferenceabilityForwardLinks;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.DiachronCacheManager;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource.SerialisableHttpResponse;
import eu.diachron.qualitymetrics.utilities.CommonDataStructures;
import eu.diachron.qualitymetrics.utilities.HTTPRetriever;

/**
 * @author Jeremy Debattista
 * 
 * This metric measures the extent to which a resource includes all triples from the dataset that have 
 * the resource's URI as the object. This allows browsers and crawlers to traverse links in either direction, 
 * as specified by Hogan et al. To do so, it computes the ratio between the number of objects that are 
 * "back-links" a.k.a "inlinks" (are part of the resource's URI) and the total number of objects in the resource.
 * 
 * Based on: Hogan Aidan, Umbrich Jürgen. An empirical survey of Linked Data conformance.
 * 
 */
public class DereferenceBackLinks implements QualityMetric {

	
	private final Resource METRIC_URI = DQM.DereferenceabilityBackLinksMetric;
	
	final static Logger logger = LoggerFactory.getLogger(DereferenceabilityForwardLinks.class);
		
	private HTTPRetriever httpRetreiver = new HTTPRetriever();

	private Map<Pair<String,String>, Double> di_p = new ConcurrentHashMap<Pair<String,String>, Double>();
	
	private boolean metricCalculated = false;
	private double metricValue = 0.0;
	
	private List<Model> _problemList = new ArrayList<Model>();
	
	
	
	@Override
	public void compute(Quad quad) {
		if (!(quad.getPredicate().matches(RDF.type.asNode()))){
			String subject = quad.getSubject().toString();
			String object = quad.getObject().toString();
			if (httpRetreiver.isPossibleURL(object)){
				httpRetreiver.addResourceToQueue(object);
				di_p.put(new Pair<String,String>(subject,object), 0.0);
			}	
		}
	}

	@Override
	public double metricValue() {
		if (!this.metricCalculated){
			httpRetreiver.start();

			this.checkForBackwardLinking();
			this.metricCalculated = true;
			httpRetreiver.stop();
			
			double sum = 0.0;
			
			for(Double d : di_p.values()){
				sum += d;
			}
			
			metricValue = (sum == 0.0) ? 0.0 : sum / di_p.keySet().size();
		}
		
		return metricValue;
	}

	@Override
	public Resource getMetricURI() {
		return METRIC_URI;
	}

	@Override
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

	@Override
	public boolean isEstimate() {
		return false;
	}

	@Override
	public Resource getAgentURI() {
		return 	DQM.LuzzuProvenanceAgent;
	}
	
	
	//Private Methods
	/**
	 * The object resource of an assessed triple is dereferenced. For a backlink to be valid
	 * the deferenced representation should have a triple, where the object is found in the subject
	 * position of this triple whilst the subject of the original assessed triple is found in the
	 * object position.
	 */
	private void checkForBackwardLinking(){
		List<Pair<String,String>> uriSet = new ArrayList<Pair<String,String>>(di_p.keySet());
		while(uriSet.size() > 0){
			Pair<String,String> p_uri = uriSet.remove(0);
			CachedHTTPResource httpResource = (CachedHTTPResource) DiachronCacheManager.getInstance().getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, p_uri.getSecondElement());
			if (httpResource.getResponses() == null) {
				uriSet.add(p_uri);
				continue;
			}
//			if (this.isDereferenceable(httpResource)){
//				Model m = this.getMeaningfulData(httpResource);
//				if (m.size() > 0){
//					
//					List<Statement> allStatements = m.listStatements(null, null,  m.createResource(p_uri.getFirstElement())).toList();
//					
//					if (allStatements.size() > 0){
//						di_p.put(p_uri, 1.0);
//					} else {
//						//no backlink found for p_uri.getFirstElement in p_uri.getSecondElement
//						this.createBackLinkViolation(p_uri.getFirstElement(), p_uri.getSecondElement());
//					}
//					
//					
//				} else {
//					// report problem Not Valid Dereferenced Backlink
//					this.createNotValidDereferenceableBacklinkLink(p_uri.getSecondElement());
//				}
//			}
		}
	}
	
	private void createNotValidDereferenceableBacklinkLink(String resource){
		Model m = ModelFactory.createDefaultModel();
		
		Resource subject = m.createResource(resource);
		m.add(new StatementImpl(subject, QPRO.exceptionDescription, DQM.NotValidDereferenceableBackLink));
		
		this._problemList.add(m);
	}
	
	private void createBackLinkViolation(String subjectURI, String resource){
		Model m = ModelFactory.createDefaultModel();
		
		Resource subject = m.createResource(resource);
		m.add(new StatementImpl(subject, QPRO.exceptionDescription, DQM.NoBackLink));
		
		RDFNode violatedTriple = Commons.generateRDFBlankNode();
		m.add(new StatementImpl(violatedTriple.asResource(), RDF.subject, m.createResource(subjectURI)));
		
		m.add(new StatementImpl(subject, DQM.hasViolatingTriple, violatedTriple));

		this._problemList.add(m);
	}
	
}
