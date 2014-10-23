package eu.diachron.qualitymetrics.intrinsic.consistency;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import eu.diachron.semantics.vocabulary.DQM;
import eu.diachron.qualitymetrics.utilities.VocabularyReader;

/**
 * This metric should find resources that are - defined as a property but also
 * appear on subject or object positions in other triples (except cases like
 * ex:prop rdf:type rdfs:Property, ex:prop rds:subPropetyOf) - defined as a
 * class but also appear on predicate position in other triples. The metric is
 * computed as a ratio of misplaced classes and properties
 * 
 * Metric Value Range : [0 - 1]
 * Best Case : 0
 * Worst Case : 1
 * 
 * @author Muhammad Ali Qasmi
 * @date 13th March 2014
 */
public class MisplacedClassesOrProperties implements QualityMetric {
	/**
	 * Metric URI
	 */
	private final Resource METRIC_URI = DQM.MisplacedClassesOrPropertiesMetric;
	/**
	 * static logger object
	 */
	static Logger logger = Logger.getLogger(MisplacedClassesOrProperties.class);
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
	 * @param quad
	 *            - to be identified
	 */
	
	public void compute(Quad quad) {
		logger.trace("compute() --Started--");

		try {

			Node subject = quad.getSubject(); // retrieve subject
			Node predicate = quad.getPredicate(); // retrieve predicate
			Node object = quad.getObject(); // retrieve object

			if (subject.isURI()) { // check if subject is URI (not Blank)
				this.totalClassesCount++;
				// load model
				Model subjectModel = VocabularyReader.read(subject.getURI());
				if (!subjectModel.isEmpty()) { // check if system is able to
											// retrieve model
					// search for URI resource from Model
					if (subjectModel.getResource(subject.getURI())
							.isURIResource()) {
						// search for its domain and range properties
						// if it has one then it is a property not class.
						if (subjectModel.getResource(subject.getURI())
								.hasProperty(RDFS.domain)
								|| subjectModel.getResource(subject.getURI())
										.hasProperty(RDFS.range)) {
							logger.debug("Misplace Class Found in Subject::"
									+ subject);
							this.misplacedClassesCount++;
							this.problemList.add(quad);
						}
					}
				}
			}

			if (predicate.isURI()) { // check if predicate is URI
				this.totalPropertiesCount++;
				// load model
				Model predicateModel = VocabularyReader
						.read(predicate.getURI());
				if (!predicateModel.isEmpty()) { // check if system is able to
												// retrieve model
					// search for URI resource from Model
					if (predicateModel.getResource(predicate.getURI())
							.isURIResource()) {
						// search for its domain and range properties
						// if it does NOT have some domain and range than its
						// NOT a property
						if (!(predicateModel.getResource(predicate.getURI())
								.hasProperty(RDFS.domain) && predicateModel
								.getResource(predicate.getURI()).hasProperty(
										RDFS.range))) {
							logger.debug("Misplace Property Found in Predicate ::"
									+ predicate);
							this.misplacedPropertiesCount++;
							this.problemList.add(quad);
						}
					}
				}
			}

			if (object.isURI()) { // check if object is URI (not blank or
									// literal)
				this.totalClassesCount++;
				// load model
				Model objectModel = VocabularyReader.read(object.getURI());
				if (!objectModel.isEmpty()) { // check if system is able to
											// retrieve model
					// search for URI resource from Model
					if (objectModel.getResource(object.getURI())
							.isURIResource()) {
						// search for its domain and range properties
						// if it has one then it is a property not class.
						if (objectModel.getResource(object.getURI())
								.hasProperty(RDFS.domain)
								|| objectModel.getResource(object.getURI())
										.hasProperty(RDFS.range)) {
							logger.debug("Misplace Class Found in Object ::"
									+ object);
							this.misplacedClassesCount++;
							this.problemList.add(quad);
						}
					}
				}
			}

		} catch (Exception exception) {
			logger.debug(exception);
			logger.error(exception.getMessage());
		}

		logger.trace("compute() --Ended--");
	}

	/**
	 * This method computes metric value for the object of this class.
	 * 
	 * @return (total number of undefined classes or properties) / (total number
	 *         of classes or properties)
	 */
	
	public double metricValue() {
		logger.trace("metricValue() --Started--");
		logger.debug("Number of Misplaced Classes :: "
				+ this.misplacedClassesCount);
		logger.debug("Number of Classes :: " + this.totalClassesCount);
		logger.debug("Number of Misplaced Properties :: "
				+ this.misplacedPropertiesCount);
		logger.debug("Number of Properties :: " + this.totalPropertiesCount);

		long tmpTotalUndefinedClassesAndUndefinedProperties = this.misplacedClassesCount
				+ this.misplacedPropertiesCount;
		long tmpTotalClassesAndProperties = this.totalClassesCount
				+ this.totalPropertiesCount;
		// return ZERO if total number of RDF literals are ZERO [WARN]
		if (tmpTotalClassesAndProperties <= 0) {
			logger.warn("Total number of classes and properties in given document is found to be zero.");
			return 0.0;
		}

		double metricValue = (double) tmpTotalUndefinedClassesAndUndefinedProperties
				/ tmpTotalClassesAndProperties;
		logger.debug("Metric Value :: " + metricValue);
		logger.trace("metricValue() --Ended--");
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
			logger.debug(problemListInitialisationException);
			logger.error(problemListInitialisationException.getMessage());
		}
		return tmpProblemList;
	}
	
}