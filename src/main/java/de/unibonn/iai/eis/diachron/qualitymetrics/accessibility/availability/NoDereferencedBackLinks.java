package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import java.util.List;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.qualitymetrics.QualityMetric;

public class NoDereferencedBackLinks implements QualityMetric{

	@Override
	public void compute(Quad quad) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return "NoDereferencedBackLinks";
	}

	@Override
	public double metricValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Triple> toDAQTriples() {
		// TODO Auto-generated method stub
		return null;
	}

}
