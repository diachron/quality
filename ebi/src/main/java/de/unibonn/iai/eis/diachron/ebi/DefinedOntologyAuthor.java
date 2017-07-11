package de.unibonn.iai.eis.diachron.ebi;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.OWL;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.semantics.EBIQM;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;

/**
 * @author Jeremy Debattista
 * @category EBI
 *
 * Checks whether the creator <efo:creator> is defined in the ontology.
 * 
 */
public class DefinedOntologyAuthor extends AbstractQualityMetric {

	private final Resource METRIC_URI = EBIQM.DefinedOntologyAuthorMetric;
	
	private boolean definedCreator = false;
	private Set<String> ontologies = new HashSet<String>();
	
	private final String CREATOR_TERM = "http://www.ebi.ac.uk/efo/creator";
	
	
	//TODO: fix since "ordering" is not guaranteed
	public void compute(Quad quad) {
		if ((quad.getObject().isURI()) && (quad.getObject().getURI().equals(OWL.Ontology.getURI()))) ontologies.add(quad.getSubject().getURI());
		
		if (quad.getPredicate().getURI().equals(CREATOR_TERM))
			if (this.ontologies.contains(quad.getSubject().getURI())) this.definedCreator = true;
	}

	/**
	 * The metric value is calculated by checking if there is at least 1 synonym term for every class entity 
	 * @return metric value
	 */
	
	public double metricValue() {
		return (definedCreator) ? 1 : 0;
	}

	
	public Resource getMetricURI() {
		return this.METRIC_URI;
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
