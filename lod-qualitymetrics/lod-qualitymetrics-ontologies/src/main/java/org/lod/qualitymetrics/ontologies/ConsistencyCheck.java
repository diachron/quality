package org.lod.qualitymetrics.ontologies;

import java.util.Iterator;

import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.ValidityReport.Report;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.semantics.utilities.Commons;

public class ConsistencyCheck implements QualityMetric {

	private OntModel model = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);

	@Override
	public void compute(Quad quad) {
		// TODO Auto-generated method stub
		model.add(Commons.asRDFNode(quad.getSubject()).asResource(), model.createProperty(quad.getPredicate().getURI()),
				Commons.asRDFNode(quad.getObject()));
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
		return model.validate().getReports().hasNext() ? 1.0d : 0.0d;
	}
	
	public Iterator<Report> getReports() {
		// TODO Auto-generated method stub
		return model.validate().getReports();
	}


}
