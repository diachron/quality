/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.interlinking;


import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.commons.graphs.MapDBGraph;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.qualitymetrics.accessibility.interlinking.helper.EstimateClusteringCoefficientMeasure;
import eu.diachron.semantics.vocabulary.DQM;

/**
 * @author Jeremy Debattista
 * 
 */
public class EstimatedClusteringCoefficiency implements QualityMetric {

	private MapDBGraph graph = new MapDBGraph();
	
	private final Resource METRIC_URI = DQM.InterlinkDetectionMetric;
	
	
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
		EstimateClusteringCoefficientMeasure ccm = new EstimateClusteringCoefficientMeasure(graph);

		return ccm.getIdealMeasure();
	}

}
