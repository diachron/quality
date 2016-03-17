package de.unibonn.iai.eis.diachron.ebi;

import java.util.HashMap;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.OWL;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.semantics.EBIQM;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;

/**
 * @author Jeremy Debattista 
 * @category EBI
 *
 * This checks that an ontology has only a single instance of <owl:versionInfo>.
 * 
 * This is a specific property required for the EBI usecase.
 */
public class OntologyVersioningConciseness implements QualityMetric {

	private final Resource METRIC_URI = EBIQM.OntologyVersionConcisenessMetric;
	private HashMap<Node, Integer> ontologyInstances = new HashMap<Node, Integer>();
	
	private final String ONTOLOGY_VERSION = OWL.versionInfo.getURI();
	
	
	/**
	 * This method will check if the quad defines the <owl:versionInfo> property
	 */
	public void compute(Quad quad) {
		// <owl:ontologyVersion> should be defined only on something of type owl:Ontology
		if ((quad.getObject().isURI()) && (quad.getObject().getURI().equals(OWL.Ontology.getURI().toString()))){
			if (ontologyInstances.containsKey(quad.getSubject())){
				int instance = ontologyInstances.get(quad.getSubject()) + 1;
				ontologyInstances.put(quad.getSubject(), instance);
			} else {
				ontologyInstances.put(quad.getSubject(), 0);
			}
		}
		
		if (quad.getPredicate().getURI().equals(this.ONTOLOGY_VERSION)){
			if (ontologyInstances.containsKey(quad.getSubject())){
				// We have an ontologyVersion property defined in the ontology
				int instance = ontologyInstances.get(quad.getSubject()) + 1;
				ontologyInstances.put(quad.getSubject(), instance);
			} 
		}
	}

	/**
	 * The metric value will return the number of owl:ontologyVersion instances per ontology
	 * TODO: this is incorrect (23/7/2014) - should return 1 if only one version exists, 0 if more (maybe true/false is better?)
	 */
	
	public double metricValue() {
		
		int ontologies = 0;
		int instances = 0;
		for (Node n : this.ontologyInstances.keySet()){
			ontologies++;
			instances += this.ontologyInstances.get(n);
		}
	
		if (ontologies == 0) return 0;
		return instances / ontologies;
	}

	
	public Resource getMetricURI() {
		return METRIC_URI;
	}

	
	public ProblemList<?> getQualityProblems() {
		return null;
	}
	
	@Override
	public boolean isEstimate() {
		return false;
	}

	@Override
	public Resource getAgentURI() {
		return 	DQM.LuzzuProvenanceAgent;
	}

}
