/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.interlinking;


import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.assessment.ComplexQualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DirectedGraph;
import eu.diachron.qualitymetrics.accessibility.interlinking.helper.CentralityMeasure;
import eu.diachron.qualitymetrics.accessibility.interlinking.helper.ClusteringCoefficientMeasure;
import eu.diachron.qualitymetrics.accessibility.interlinking.helper.DegreeMeasure;
import eu.diachron.qualitymetrics.accessibility.interlinking.helper.DescriptiveRichnessMeasure;
import eu.diachron.qualitymetrics.accessibility.interlinking.helper.RDFEdge;
import eu.diachron.qualitymetrics.accessibility.interlinking.helper.SameAsMeasure;
import eu.diachron.semantics.vocabulary.DQM;

/**
 * @author Jeremy Debattista
 * 
 */
public class InterlinkDetectionMetric implements ComplexQualityMetric {

	private DirectedGraph<String, RDFEdge> _graph = new DelegateForest<String, RDFEdge>();
	private boolean afterExecuted = false;
	
	private double metricValue = 0.0; //In order to calculate the metric value, we get the IDEAL value of all other sub-metrics and multiply it by a 0.2 weight
	
	private final Resource METRIC_URI = DQM.InterlinkDetectionMetric;
	
	public void compute(Quad quad) {
		String subject = "";
		if (!quad.getSubject().isBlank()){
			subject = quad.getSubject().getURI();
			if (!_graph.containsVertex(subject)) _graph.addVertex(subject);
		}
		
		// Should we include literals as nodes???
		String object = "";
		if (!quad.getObject().isBlank()){
			if (quad.getObject().isURI()){
				object = quad.getObject().getURI();
				if (!_graph.containsVertex(object)) _graph.addVertex(object);
			} 
//				else {
//				object = quad.getObject().getLiteralValue().toString();
//			}
			
		}
		
		String predicate = quad.getPredicate().getURI();
		if (!object.equals("")){
			RDFEdge edge = new RDFEdge(predicate);
			_graph.addEdge(edge, subject, object);
		}
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
	}
	
	// Post-Processing
	public void after(Object... arg0) {
		this.afterExecuted = true;
		
		
		//1. DegreeMeasure
		DegreeMeasure dm = new DegreeMeasure(_graph);
		metricValue += dm.getIdealMeasure() * 0.2;
		
		//2. Clustering Coefficient
		ClusteringCoefficientMeasure ccm = new ClusteringCoefficientMeasure(_graph);
		metricValue += ccm.getIdealMeasure() * 0.2;
		
		//3. Centrality
		CentralityMeasure cm = new CentralityMeasure(_graph);
		metricValue += cm.getIdealMeasure() * 0.2;
		
		//4. OpenSameAs
		//	for this we do a ratio of the number of same as triples against the number of open sameas - ideally we have 0..
		SameAsMeasure sam = new SameAsMeasure(_graph);
		metricValue += (1.0 - sam.getIdealMeasure()) * 0.2;
		
		//5. Description Richness
		DescriptiveRichnessMeasure drm = new DescriptiveRichnessMeasure(_graph);
		metricValue += drm.getIdealMeasure() * 0.2;
	}
}
