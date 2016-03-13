/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.consistency;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.utilities.VocabularyLoader;

/**
 * @author Jeremy Debattista
 * 
 * This metric checks if a dataset makes use of Deprecated Classes or Properties.
 * A high usage of such classes or properties will give a low value (closer to 0),
 * whilst a low usage of such classes will give a high value (closer to 1).
 */
public class UsageOfDeprecatedClassesOrProperties implements QualityMetric{
	
	private final Resource METRIC_URI = DQM.UsageOfDeprecatedClassesOrProperties;

	private static Logger logger = LoggerFactory.getLogger(UsageOfDeprecatedClassesOrProperties.class);
	
	private Set<Quad> _problemList = new HashSet<Quad>();

	private long totalTypes = 0;
	private long totalProperties = 0;
	private long deprecatedTypes = 0;
	private long deprecatedProperties = 0;
	
	@Override
	public void compute(Quad quad) {
		Node property = quad.getPredicate();
		Node object = quad.getObject();
		
		if (property.getURI().equals(RDF.type.getURI())){
			if (VocabularyLoader.isDeprecatedTerm(object)) {
				deprecatedTypes++;
				Quad q = new Quad(null, object, QPRO.exceptionDescription.asNode(), DQM.DeprecatedClass.asNode());
				this._problemList.add(q);
			}
			totalTypes++;
		} else {
			if (VocabularyLoader.isDeprecatedTerm(property)) {
				deprecatedProperties++;
				Quad q = new Quad(null, property, QPRO.exceptionDescription.asNode(), DQM.DeprecatedProperty.asNode());
				this._problemList.add(q);
			}
		}
		totalProperties++;
	}

	@Override
	public double metricValue() {
		double value = 1 - (((double) deprecatedTypes + (double) deprecatedProperties) /  
				((double) totalTypes + (double) totalProperties));
		return value;
	}

	@Override
	public Resource getMetricURI() {
		return METRIC_URI;
	}

	@Override
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
