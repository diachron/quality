package de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.conciseness;

import java.util.HashMap;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.OWL;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;

/**
 * @author jeremy
 *
 * This checks that an ontology has only a single instance of <owl:ontologyVersion>.
 * <owl:ontologyVersion> property is not defined in OWL but it is a property defined
 * by EBI, where the domain is an owl:Ontology and the range is an xsd:string.
 * 
 * This is a specific property required for the EBI usecase.
 */
public class OntologyVersioningConciseness extends AbstractQualityMetric {

	private final Resource METRIC_URI = DQM.OntologyVersionConcisenessMetric;
	private HashMap<Resource, Integer> ontologyInstances = new HashMap<Resource, Integer>();
	
	private final String ONTOLOGY_VERSION = "http://www.w3.org/2002/07/owl#ontologyVersion";
	
	
	/**
	 * This method will check if the quad defines the <owl:ontologyVersion> property
	 */
	@Override
	public void compute(Quad quad) {
		// <owl:ontologyVersion> should be defined only on something of type owl:Ontology
		if (quad.getObject().getURI().equals(OWL.Ontology.getURI())){
			if (ontologyInstances.containsKey(quad.getSubject())){
				int instance = ontologyInstances.get(quad.getSubject()) + 1;
				ontologyInstances.put((Resource) quad.getSubject(), instance);
			} else {
				ontologyInstances.put((Resource) quad.getSubject(), 0);
			}
		}
		
		if (quad.getPredicate().getURI().equals(this.ONTOLOGY_VERSION)){
			if (ontologyInstances.containsKey(quad.getSubject())){
				// We have an ontologyVersion property defined in the ontology
				int instance = ontologyInstances.get(quad.getSubject()) + 1;
				ontologyInstances.put((Resource) quad.getSubject(), instance);
			}
		}
	}

	/**
	 * The metric value will return the number of owl:ontologyVersion instances per ontology
	 */
	@Override
	public double metricValue() {
		
		int ontologies = 0;
		int instances = 0;
		for (Resource r : this.ontologyInstances.keySet()){
			ontologies = ontologies++;
			instances = this.ontologyInstances.get(r);
		}
		
		return (ontologies / instances);
	}

	@Override
	public Resource getMetricURI() {
		return METRIC_URI;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		return null;
	}

}
