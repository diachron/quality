package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import java.util.List;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.qualitymetrics.DimensionNamesOntology;
import de.unibonn.iai.eis.diachron.qualitymetrics.QualityMetric;

public class NoDereferencedForwardLinks implements QualityMetric {

	public void compute(Quad quad) {
		// TODO Auto-generated method stub

	}

	public String getName() {

		return "NoDereferencedForwardLinks";
	}

	public double metricValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<Triple> toDAQTriples() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDimension() {
		return DimensionNamesOntology.ACCESIBILITY.AVAILABILITY;
	}

	public String getGroup() {
		return DimensionNamesOntology.ACCESIBILITY.GROUP_NAME;
	}

	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

}
