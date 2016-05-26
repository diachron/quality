package org.lod.qualitymetrics.ontologies;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;

public class InheritanceRichness implements QualityMetric {
	
	private double classCounter = 0;
	
	private double subClassCounter = 0;

	@Override
	public void compute(Quad quad) {

		//if (quad.getObject().isURI())
		// System.out.println(quad.getSubject().getURI()+" --- "+quad.getPredicate().getURI()+" --- "+quad.getObject().toString());

		if (quad.getObject().isURI() && (quad.getObject().getURI().equals(RDFS.Class.getURI()))){
			classCounter++;
		}
		else if (quad.getPredicate().getURI().equals(RDFS.subClassOf.getURI())){
			subClassCounter++;
		} 
	
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
		
		System.out.println("Classes"+classCounter);

		System.out.println("SubClasses"+subClassCounter);

		return (subClassCounter/classCounter);
	}

}
