package eu.diachron.qualitymetrics.intrinsic.consistency;

import java.util.ArrayList;
import java.util.List;

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
import eu.diachron.qualitymetrics.utilities.VocabularyLoader;

/**
 * This metric is based on the metric defined by Hogan et al.
 * Weaving the Pedantic Web. This metric checks if the assessed
 * dataset has a defined classed placed in the triple's predicate
 * and defined property in the object position. If an undefined
 * class or property is used, then it is ignored
 *  
 * Best Case : 1
 * Worst Case : 0
 * 
 * @author Jeremy Debattista
 * @date 16th April 2015
 */
public class MisplacedClassesOrProperties implements QualityMetric {
	/**
	 * Metric URI
	 */
	private final Resource METRIC_URI = DQM.MisplacedClassesOrPropertiesMetric;
	/**
	 * static logger object
	 */
	static Logger logger = LoggerFactory.getLogger(MisplacedClassesOrProperties.class);
	/**
	 * total number of misplaces classes
	 */
	protected long misplacedClassesCount = 0;
	/**
	 * total number of classes
	 */
	protected long totalClassesCount = 0;
	/**
	 * total number of misplaces properties
	 */
	protected long misplacedPropertiesCount = 0;
	/**
	 * total number properties
	 */
	protected long totalPropertiesCount = 0;
	/**
	 * list of problematic quads
	 */
	protected List<Quad> problemList = new ArrayList<Quad>();

	/**
	 * This method identifies whether a given quad is a misplaced class or a
	 * misplaced property.
	 * 
	 * @param quad - to be identified
	 */
	
	public void compute(Quad quad) {

			Node predicate = quad.getPredicate(); // retrieve predicate
			Node object = quad.getObject(); // retrieve object
			
			//checking if classes are found in the property position
			logger.info("Checking {} for misplaced class", predicate.getURI());
			this.totalPropertiesCount++;
			if ((VocabularyLoader.isClass(predicate)) && (VocabularyLoader.checkTerm(predicate))){
				this.misplacedPropertiesCount++;
				//problem
			}
			
			//checking if properties are found in the object position
			if ((object.isURI()) && (predicate.getURI().equals(RDF.type.getURI())) && (VocabularyLoader.checkTerm(object))){
				logger.info("Checking {} for misplaced class", object.getURI());
				this.totalClassesCount++;
				if (VocabularyLoader.isProperty(object)){
					this.misplacedClassesCount++;
					//problem
				}
			}
	}

	/**
	 * This method computes metric value for the object of this class.
	 * 
	 * @return (total number of undefined classes or properties) / (total number
	 *         of classes or properties)
	 */
	
	public double metricValue() {
		logger.info("Number of Misplaced Classes: {}", this.misplacedClassesCount);
		logger.info("Number of Misplaced Properties: {}", this.misplacedPropertiesCount);

		double metricValue = 1.0;
		
		double misplaced = (double)this.misplacedPropertiesCount + (double)this.misplacedPropertiesCount;
		if (misplaced > 0) 
			metricValue = 1.0 - (misplaced / ((double)this.totalPropertiesCount + (double)this.totalClassesCount));
		
		logger.info("Metric Value: {}", metricValue);
		return metricValue;
	}

	/**
	 * Returns Metric URI
	 * 
	 * @return metric URI
	 */
	
	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	/**
	 * Returns list of problematic Quads
	 * 
	 * @return list of problematic quads
	 */
	public ProblemList<?> getQualityProblems() {
		ProblemList<Quad> tmpProblemList = null;
		try {
			tmpProblemList = new ProblemList<Quad>(this.problemList);
		} catch (ProblemListInitialisationException problemListInitialisationException) {
			logger.error(problemListInitialisationException.getMessage());
		}
		return tmpProblemList;
	}

	/* (non-Javadoc)
	 * @see de.unibonn.iai.eis.luzzu.assessment.QualityMetric#isEstimate()
	 */
	@Override
	public boolean isEstimate() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see de.unibonn.iai.eis.luzzu.assessment.QualityMetric#getAgentURI()
	 */
	@Override
	public Resource getAgentURI() {
		// TODO Auto-generated method stub
		return null;
	}
	
}