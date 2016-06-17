/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.interlinking;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.mapdb.MapDBGraph;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import eu.diachron.qualitymetrics.accessibility.interlinking.helper.CentralityMeasure;
import eu.diachron.qualitymetrics.accessibility.interlinking.helper.ClusteringCoefficientMeasure;
import eu.diachron.qualitymetrics.accessibility.interlinking.helper.DegreeMeasure;
import eu.diachron.qualitymetrics.accessibility.interlinking.helper.DescriptiveRichnessMeasure;
import eu.diachron.qualitymetrics.accessibility.interlinking.helper.SameAsMeasure;
import eu.diachron.qualitymetrics.utilities.AbstractComplexQualityMetric;

/**
 * @author Jeremy Debattista
 * 
 */
public class InterlinkDetectionMetric extends AbstractComplexQualityMetric {
	
	final static Logger logger = LoggerFactory.getLogger(InterlinkDetectionMetric.class);

	private MapDBGraph graph = new MapDBGraph();
	
	private boolean afterExecuted = false;
	
	private double metricValue = 0.0; //In order to calculate the metric value, we get the IDEAL value of all other sub-metrics and multiply it by a 0.2 weight
	
	private final Resource METRIC_URI = DQM.InterlinkDetectionMetric;
	
	private List<Quad> _problemList = new ArrayList<Quad>();
	
	public void compute(Quad quad) {
		logger.debug("Computing : {} ", quad.asTriple().toString());
		
		String subject = "";
		if (!quad.getSubject().isBlank()){
			subject = quad.getSubject().getURI();
		} else {
			subject = quad.getSubject().getBlankNodeLabel();
		}
		
		String object = "";
		if (!quad.getObject().isBlank()){
			if (quad.getObject().isURI()){
				object = quad.getObject().getURI();
			} else {
				object = quad.getObject().toString();
			}
		} else {
			object = quad.getObject().getBlankNodeLabel();
		}
		
		String predicate = quad.getPredicate().getURI();
		graph.addConnectedNodes(subject, object, predicate);
	}
	
	public Resource getMetricURI() {
		return this.METRIC_URI;
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

	public double metricValue() {
		if (!this.afterExecuted) 
			this.after();

		return this.metricValue;
	}

	public void before(Object... arg0) {
	}
	
	// Post-Processing
	public void after(Object... arg0) {
		this.afterExecuted = true;
		
		//1. DegreeMeasure
		DegreeMeasure dm = new DegreeMeasure(graph);
		metricValue += dm.getIdealMeasure() * 0.2;
		
		//2. Clustering Coefficient
		ClusteringCoefficientMeasure ccm = new ClusteringCoefficientMeasure(graph);
		metricValue += ccm.getIdealMeasure() * 0.2;
		
		//3. Centrality
		CentralityMeasure cm = new CentralityMeasure(graph);
		metricValue += cm.getIdealMeasure() * 0.2;
		
		//4. OpenSameAs
		//	for this we do a ratio of the number of same as triples against the number of open sameas - ideally we have 0..
		SameAsMeasure sam = new SameAsMeasure(graph);
		metricValue += (1.0 - sam.getIdealMeasure()) * 0.2;
		
		//5. Description Richness
		DescriptiveRichnessMeasure drm = new DescriptiveRichnessMeasure(graph);
		metricValue += drm.getIdealMeasure() * 0.2;
		
		statsLogger.info("InterlinkDetectionMetric. Dataset: {} - Degree Measure : {}; Clustering Coef. : {}; " +
				"Centrality : {}; Open Same As : {}; Description Richness : {};", 
				EnvironmentProperties.getInstance().getDatasetURI(), dm.getIdealMeasure(), ccm.getIdealMeasure(), 
				cm.getIdealMeasure(), sam.getIdealMeasure(), drm.getIdealMeasure());
	}


	@Override
	public boolean isEstimate() {
		return true;
	}

	@Override
	public Resource getAgentURI() {
		return DQM.LuzzuProvenanceAgent;
	}
	
}
