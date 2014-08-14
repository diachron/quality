package eu.diachron.qualitymetrics.intrinsic.accuracy;

import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.riot.RiotException;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import eu.diachron.semantics.vocabulary.DQM;
import eu.diachron.qualitymetrics.utilities.VocabularyReader;

/**
 * Detects incompatible data type of literals by comparing its Data Type URI with the
 * Data Type URI specified in the range of the Object's predicate
 * 
 * Metric Value Range : [0 - 1]
 * Best Case : 0
 * Worst Case : 1
 * 
 * @author Muhammad Ali Qasmi
 * @date 20th Feb 2014
 */
public class IncompatibleDatatypeRange implements QualityMetric {
	/**
	 * Metric URI
	 */
	private final Resource METRIC_URI = DQM.IncompatibleDatatypeRangeMetric;
	/**
	 * logger object
	 */
	static Logger logger = Logger.getLogger(IncompatibleDatatypeRange.class);
	/**
	 * cache frequently used Properties
	 */
	static Map<String, Statement> cacheProperty = new HashMap<String, Statement>();
	/**
	 * list of problematic quads
	 */
	protected List<Quad> problemList = new ArrayList<Quad>();
	/**
	 * total number of literals
	 */
	private double totalLiterals = 0;
	/**
	 * total number of incompatiable data type literals.
	 */
	private double incompatiableDataTypeLiterals = 0;

	/**
	 * Clears Property Cache
	 */
	public static void clearCache() {
		cacheProperty.clear();
	}

	/**
	 * Validates data type of literal by comparing its Data Type URI with the
	 * Data Type URI specified in the range of the Object's predicate
	 * 
	 * @param literalDateTypeURI
	 * @param RangeDataTypeURI
	 * @return true - if validated
	 */
	protected boolean checkTypeByComparingURI(URI literalDataTypeURI,
			URI rangeReferredURI) {

		// case: literDataTyprURI NOT null but RangeReferredURI is null
		if (literalDataTypeURI != null && rangeReferredURI == null) {
			logger.warn("literalDataTypeURI is NOT null but RangeReferredURI is null.");
			return true;
		}
		// case: literDataType is NUll and RangeRefferedURI is a literal
		else if (literalDataTypeURI == null
				&& rangeReferredURI.getFragment().toLowerCase()
						.equals("literal")) {
			return true;
		}
		// case: literalDataTypeURI is null
		else if (literalDataTypeURI == null) {
			logger.info("literDataTypeURI is null.");
			return true;
		}
		// case: rangeReferredURI is null
		else if (rangeReferredURI == null) {
			logger.warn("RangeReferredURI is null.");
			return true;
		}
		// case: Both are EQUAL
		else if (literalDataTypeURI.equals(rangeReferredURI)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Computes whether a given quad is incompatible data type literal or not
	 * 
	 * @param quad
	 *            - to be processed
	 */
	
	public void compute(Quad quad) {
		logger.trace("compute() --Started--");
		try {
			// retrieve predicate
			Node predicate = quad.getPredicate();
			// retrieve object
			Node object = quad.getObject();

			// check if predicate and object are NOT null
			if (predicate != null && object != null) {

				logger.debug("Processing for object ::" + object.toString());

				if (object.isLiteral()) { // check if the object is literal

					this.totalLiterals++; // increment total number of literals

					// retrieve predicate URI
					if (predicate.getURI() != null) {

						Statement tmpProperty = null;

						// check if the property is present in cache
						if (cacheProperty.containsKey(predicate.getURI())) {
							logger.debug(predicate.getURI()
									+ " :: found in cache.");
							tmpProperty = cacheProperty.get(predicate.getURI());
						} else { // load property from given URI source
							logger.debug("predicate vocabulary not found in cache.");
							logger.debug("loading vocabulary for predicate from :: "
									+ predicate.getURI());
							Model tmpModel = VocabularyReader.read(predicate.getURI()); // load
																					// vocabulary
																					// from
																					// the
																					// URI
							tmpProperty = (tmpModel != null) ? (tmpModel
									.getResource(predicate.getURI()))
									.getProperty(RDFS.range) : null;
						}

						// check if property is not empty
						if (tmpProperty != null) {
							// store new statement in cache
							if (!cacheProperty.containsKey(predicate.getURI())) {
								cacheProperty.put(predicate.getURI(),
										tmpProperty);
							}

							Triple triple = tmpProperty.asTriple();

							String predicateURI = predicate.getURI().toString(); // given
																					// predicate
							String subject = triple.getSubject().toString(); // retrieved
																				// predicate

							// check if retrieved predicate matches with the
							// given predicate
							if (subject.equals(predicateURI)) {

								logger.debug("Object DataType URI :: "
										+ object.getLiteralDatatypeURI());
								logger.debug("Range Referred DateType URI :: "
										+ triple.getObject());

								try {

									URI givenObjectDateTypeURI = (object
											.getLiteralDatatypeURI() != null) ? new URI(
											object.getLiteralDatatypeURI())
											: null;
									URI rangeObjectURI = (triple.getObject()
											.toString() != null) ? new URI(
											triple.getObject().toString())
											: null;
									if (!checkTypeByComparingURI(
											givenObjectDateTypeURI,
											rangeObjectURI)) {
										this.incompatiableDataTypeLiterals++;
										this.problemList.add(quad);
									}
								} catch (URISyntaxException e) {
									logger.error("Malformed URI exception for "
											+ e.getMessage());
									logger.debug(e.getStackTrace());
								}

							} // End-if (subject.equals(predicateURI))

						} // End-if (tmpProperty != null)

					} // End-if (predicate.getURI() != null)

				} // End-if (object.isLiteral())
			}
		} catch (Exception exception) {
			logger.debug(exception);
			logger.error(exception.getMessage());
		}
		logger.trace("compute() --Ended--");
	}

	/**
	 * Returns value of the metric based on
	 * 
	 * @return ( number of incompatiable Data type literls ) / ( total number of
	 *         literls )
	 */
	
	public double metricValue() {
		logger.trace("metricValue() --Started--");
		logger.debug("Incompatiable DataType Literals :: "
				+ this.incompatiableDataTypeLiterals);
		logger.debug("Total Literals :: " + this.totalLiterals);

		// return ZERO if total number of RDF literals are ZERO [WARN]
		if (this.totalLiterals <= 0) {
			logger.warn("Total number of RDF data type literals in given document is found to be zero.");
			return 0.0;
		}

		double metricValue = this.incompatiableDataTypeLiterals
				/ this.totalLiterals;
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
	 * @return list of problematic Quads
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