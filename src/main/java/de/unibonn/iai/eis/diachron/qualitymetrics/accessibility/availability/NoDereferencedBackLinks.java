package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import java.util.List;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.qualitymetrics.QualityMetric;
import de.unibonn.iai.eis.diachron.vocabularies.DAQ;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;

public class NoDereferencedBackLinks implements QualityMetric {

	private final Resource METRIC_URI = DQM.DeferencibilityBackLinksMetric;
	
	public void compute(Quad quad) {
		// TODO Auto-generated method stub

	}
	public double metricValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<Triple> toDAQTriples() {
		// TODO Auto-generated method stub
		return null;
	}

	public Resource getMetricURI() {
		return this.METRIC_URI;
	}
	
}
