package de.unibonn.iai.eis.diachron.qualitymetrics.representational.understandability;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;

/**
 * @author jeremy
 *
 * Provides a measure calculating the number of blank nodes used in a dataset.
 * The lower the number of blank nodes is, the higher the quality is.
 * This is part of the Understandability dimension.
 * 
 * This is required for the EBI use case.
 */
public class LowBlankNodeUsage extends AbstractQualityMetric {

	private final Resource METRIC_URI = DQM.LowBlankNodesUsageMetric;

	private int entities = 0;
	private int blankNodes = 0;
	
	/**
	 * Simply checks if the subject or the object of a quad is a blank node.
	 * TODO: fix, since we are streaming triple by triple - the same blank node might be added more than once!
	 */
	@Override
	public void compute(Quad quad) {
		this.entities+=2;
		if (quad.getSubject().isBlank()) this.blankNodes++;
		if (quad.getObject().isBlank()) this.blankNodes++;
	}

	@Override
	public double metricValue() {
		return (this.entities - this.blankNodes) / this.entities;
	}

	@Override
	public Resource getMetricURI() {
		return METRIC_URI;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		// TODO Auto-generated method stub
		return null;
	}

}
