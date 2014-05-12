package de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.accuracy;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;

/**
 * @author Muhammad Ali Qasmi
 * @date 13th Feb 2014
 */
public class MalformedDatatypeLiterals extends AbstractQualityMetric {

	private final Resource METRIC_URI = DQM.MalformedDatatypeLiteralsMetric;
	
	private static Logger logger = Logger.getLogger(MalformedDatatypeLiterals.class);

	private double totalLiterals = 0;
	private double malformedLiterals = 0;

	protected List<Quad> problemList = new ArrayList<Quad>();

	public void compute(Quad quad) {
		logger.trace("compute() --Started--");
		// retrieves object from statement
		Node object = quad.getObject();
		// checks if object is a literal
		if (object.isLiteral()) {
			// retrieves rdfDataType from literal
			RDFDatatype rdfdataType = object.getLiteralDatatype();
			// check if rdf data type is a valid data type
			if (rdfdataType != null) {
				logger.debug("RdfDataTypeLiteral :: " + object.toString());
				if (!rdfdataType.isValidLiteral(object.getLiteral())) {
					this.malformedLiterals++;
					this.problemList.add(quad);
					logger.debug("MalformedRDFDataTypeLiteral :: " + object.toString());
				}
				this.totalLiterals++;
			}
			logger.debug("Literal :: " + object.toString());

		}
		logger.debug("Object :: " + object.toString());
		logger.trace("compute() --Ended--");
	}

	public double metricValue() {

		logger.trace("metricValue() --Started--");
		logger.debug("Malformed Literals :: " + this.malformedLiterals);
		logger.debug("Total Literals :: " + this.totalLiterals);

		// return ZERO if total number of RDF literals are ZERO [WARN]
		if (0 >= this.totalLiterals) {
			logger.warn("Total number of RDF data type literals in given document is found to be zero.");
			return 0.0;
		}

		double metricValue = this.malformedLiterals / this.totalLiterals;
		logger.debug("Metric Value :: " + metricValue);
		logger.trace("metricValue() --Ended--");
		return metricValue;
	}

	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	/**
	 * Returns list of problematic Quads
	 */
	public ProblemList<?> getQualityProblems() {
		ProblemList<Quad> tmpProblemList = null;
		try {
			tmpProblemList = new ProblemList<Quad>(this.problemList); 
		} 
		catch (ProblemListInitialisationException problemListInitialisationException){
			logger.debug(problemListInitialisationException);
        	logger.error(problemListInitialisationException.getMessage());
		}
		return tmpProblemList;	
	}
}