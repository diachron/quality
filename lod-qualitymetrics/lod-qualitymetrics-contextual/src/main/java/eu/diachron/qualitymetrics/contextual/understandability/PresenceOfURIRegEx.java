/**
 * 
 */
package eu.diachron.qualitymetrics.contextual.understandability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.VOID;
import eu.diachron.qualitymetrics.utilities.AbstractQualityMetric;

/**
 * @author Jeremy Debattista
 * 
 */
public class PresenceOfURIRegEx extends AbstractQualityMetric {

	final static Logger logger = LoggerFactory.getLogger(PresenceOfURIRegEx.class);

	private boolean uriRegExPresent = false;
	
	@Override
	public void compute(Quad quad) {
		Node predicate = quad.getPredicate();
		if (predicate.getURI().equals(VOID.uriRegexPattern.getURI())) {
			uriRegExPresent = true;
		}
	}

	@Override
	public double metricValue() {
		return (uriRegExPresent) ? 1.0d : 0.0d;
	}

	@Override
	public Resource getMetricURI() {
		return DQM.VocabularyUsageIndication;
	}

	
	public ProblemList<?> getQualityProblems() {
		return new ProblemList<Quad>();
	}
	@Override
	public boolean isEstimate() {
		return false;
	}

	@Override
	public Resource getAgentURI() {
		return DQM.LuzzuProvenanceAgent;
	}

}
