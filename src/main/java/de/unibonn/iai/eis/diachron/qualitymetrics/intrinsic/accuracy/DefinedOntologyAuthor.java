package de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.accuracy;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.OWL;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;

/**
 * @author jeremy
 *
 * Checks whether the creator <efo:creator> is defined in the ontology.
 * 
 * This is only required for the EBI use cases.
 */
public class DefinedOntologyAuthor extends AbstractQualityMetric {

	private final Resource METRIC_URI = DQM.DefinedOntologyAuthorMetric;
	
	private boolean definedCreator = false;
	private Set<String> ontologies = new HashSet<String>();
	
	private final String CREATOR_TERM = "http://www.ebi.ac.uk/efo/creator";
	
	@Override
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
	@Override
	public double metricValue() {
		return (definedCreator) ? 1 : 0;
	}

	@Override
	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		return null;
	}

}
