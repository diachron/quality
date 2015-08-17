/**
 * 
 */
package eu.diachron.qualitymetrics.contextual.understandability;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.VOID;

/**
 * @author Jeremy Debattista
 * 
 */
public class PresenceOfURIRegEx implements QualityMetric {

	final static Logger logger = LoggerFactory.getLogger(PresenceOfURIRegEx.class);

	private boolean uriRegExPresent = false;
	
	private List<Quad> _problemList = new ArrayList<Quad>();

	
	
	@Override
	public void compute(Quad quad) {
		Node predicate = quad.getPredicate();
		if (predicate.getURI().equals(VOID.uriRegexPattern.getURI())) {
			logger.info("Dataset {} has the following regex pattern: {}", EnvironmentProperties.getInstance().getBaseURI(), quad.getObject().getLiteralValue().toString());
			uriRegExPresent = true;
		}
	}

	@Override
	public double metricValue() {
		
		if (!uriRegExPresent) createProblemQuad();
		
		return (uriRegExPresent) ? 1.0d : 0.0d;
	}

	@Override
	public Resource getMetricURI() {
		return DQM.VocabularyUsageIndication;
	}

	private void createProblemQuad(){
		String baseURI = EnvironmentProperties.getInstance().getBaseURI();
		Quad q = new Quad(null, ModelFactory.createDefaultModel().createResource(baseURI).asNode(), QPRO.exceptionDescription.asNode(), DQM.NoVocabularyIndication.asNode());
		this._problemList.add(q);
	}
	
	public ProblemList<?> getQualityProblems() {
		ProblemList<Quad> pl = null;
		try {
			if(this._problemList != null && this._problemList.size() > 0) {
				pl = new ProblemList<Quad>(this._problemList);
			} else {
				pl = new ProblemList<Quad>();
			}
		} catch (ProblemListInitialisationException e) {
			logger.error(e.getMessage());
		}
		return pl;
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
