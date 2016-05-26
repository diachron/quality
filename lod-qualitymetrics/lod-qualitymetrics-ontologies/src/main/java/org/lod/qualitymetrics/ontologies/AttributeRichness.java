package org.lod.qualitymetrics.ontologies;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.semantics.utilities.Commons;

public class AttributeRichness implements QualityMetric {
	
	private double classCounter = 0;
	
	private double attributeCounter = 0;
	
	private OntModel model = ModelFactory.createOntologyModel();

	@Override
	public void compute(Quad quad) {
		// TODO Auto-generated method stub
		
		//System.out.println(quad.getSubject().getURI()+" --- "+quad.getPredicate().getURI()+" --- "+quad.getObject().toString());

//		if (quad.getObject().isURI() && (quad.getObject().getURI().equals(RDFS.Class.getURI()))){
//			classCounter++;
//		}
//		else if (quad.getObject().isURI() && (quad.getObject().getURI().equals(RDF.Property.getURI()))){
//			attributeCounter++;
//		} 
//		else if (quad.getObject().isURI() && (quad.getObject().getURI().equals(RDF.Property.getURI()))){
//			attributeCounter++;
//		} 
		
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
		
		classCounter = model.listClasses().toList().size();
		
		attributeCounter = model.listAllOntProperties().toList().size();	
				
		return attributeCounter/classCounter;
	}

}
