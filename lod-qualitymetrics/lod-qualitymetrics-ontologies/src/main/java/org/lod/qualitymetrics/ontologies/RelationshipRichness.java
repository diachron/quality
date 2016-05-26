package org.lod.qualitymetrics.ontologies;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;

public class RelationshipRichness implements QualityMetric {
	

	@Override
	public void compute(Quad quad) {
		// TODO Auto-generated method stub
		
		System.out.println(quad.getPredicate().getURI().toString());

	}

	@Override
	public Resource getAgentURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Resource getMetricURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEstimate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double metricValue() {
		// TODO Auto-generated method stub
		return 0;
	}

}
