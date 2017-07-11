package de.unibonn.iai.eis.diachron.ebi;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.semantics.EBIQM;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;

/**
 * @author Jeremy Debattista
 * @category EBI
 *
 * Similar in a way to the Usage of owl:DepricatedClass and owl:DepricatedProperty,
 * in this metric we measure the number of classes and properties in an ontology
 * which are marked as depricated. If an ontology is making lots of obsolete concepts
 * between different versions, then this is an indicator that the ontology is going through 
 * a lot of changes, and is potentially in a state of poor quality.
 * 
 */
public class ObsoleteConceptsInOntology extends AbstractQualityMetric {
	
	private final Resource METRIC_URI = EBIQM.ObsoleteConceptsInOntologyMetric;

	private Set<Node> concepts = new HashSet<Node>();
	private int deprConcept = 0;
	
	private final String OBSOLETE_CLASS = "http://www.geneontology.org/formats/oboInOwl#ObsoleteClass";
	private final String OBSOLETE_PROPERTY = "http://www.geneontology.org/formats/oboInOwl#ObsoleteProperty";
	
	/**
	 * In this method we will check for owl:DeprecatedClass and owl:DeprecatedProperty in the quad
	 */
	public void compute(Quad quad) {
		if  (quad.getPredicate().getURI().equals(RDFS.subClassOf.getURI()) && 
				(((quad.getObject().isURI()) && (quad.getObject().getURI().equals(OBSOLETE_CLASS))) ||
				((quad.getObject().isURI()) &&(quad.getObject().getURI().equals(OBSOLETE_PROPERTY))))){
			if (!(concepts.contains(quad.getSubject()))) concepts.add(quad.getSubject());
			deprConcept++;
		}
		if ((quad.getObject().isURI()) && (quad.getObject().getURI().equals(OWL.Class.getURI()))){
			if (!(concepts.contains(quad.getSubject()))) concepts.add(quad.getSubject());
		}
	}

	/**
	 * The value of this metric is computed by measuring the percentage of deprecated concepts 
	 * & properties against the total number of concepts & properties in the ontology
	 */
	public double metricValue() {
		return (double) deprConcept / (double) concepts.size() ;
	}

	
	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	
	public ProblemList<?> getQualityProblems() {
		// TODO Auto-generated method stub
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
