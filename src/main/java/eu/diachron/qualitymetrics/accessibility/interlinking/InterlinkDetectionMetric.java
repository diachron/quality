/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.interlinking;


import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.commons.graphs.MapDBGraph;
import de.unibonn.iai.eis.luzzu.assessment.ComplexQualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.qualitymetrics.accessibility.interlinking.helper.ActualClusteringCoefficientMeasure;
import eu.diachron.qualitymetrics.accessibility.interlinking.helper.EstimateClusteringCoefficientMeasure;
import eu.diachron.semantics.vocabulary.DQM;

/**
 * @author Jeremy Debattista
 * 
 */
public class InterlinkDetectionMetric implements ComplexQualityMetric {

	private MapDBGraph graph = new MapDBGraph();
	
	private boolean afterExecuted = false;
	
	private double metricValue = 0.0; //In order to calculate the metric value, we get the IDEAL value of all other sub-metrics and multiply it by a 0.2 weight
	
	private final Resource METRIC_URI = DQM.InterlinkDetectionMetric;
	
	private final boolean estimation = false;
	
	public void compute(Quad quad) {
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
				object = quad.getObject().getLiteralValue().toString();
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

	public ProblemList<?> getQualityProblems() {
		return null;
	}

	public double metricValue() {
		if (!this.afterExecuted) 
			this.after();

		return this.metricValue;
	}

	public void before(Object... arg0) {
		this.estimation = Boolean.parseBoolean(arg0.toString());
	}
	
	// Post-Processing
	public void after(Object... arg0) {
		this.afterExecuted = true;
		
		if (this.estimation){
			EstimateClusteringCoefficientMeasure ccm = new EstimateClusteringCoefficientMeasure(graph);
			metricValue += ccm.getIdealMeasure();
		} else {
			ActualClusteringCoefficientMeasure ccm = new ActualClusteringCoefficientMeasure(graph);
			metricValue += ccm.getIdealMeasure();
		}
		
	}
	
}
