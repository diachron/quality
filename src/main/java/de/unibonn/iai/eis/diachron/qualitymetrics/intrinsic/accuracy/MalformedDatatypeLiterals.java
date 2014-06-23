package de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.accuracy;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;
import de.unibonn.iai.eis.diachron.vocabularies.QR;

/**
 * Detects whether the value of a typed literal is valid with respect to its
 * given xsd datatype.
 * 
 * Metric Value Range : [0 - 1]
 * Best Case : 0
 * Worst Case : 1
 * 
 * @author Muhammad Ali Qasmi
 * @date 13th Feb 2014
 */
public class MalformedDatatypeLiterals extends AbstractQualityMetric {
	/**
	 * Metric URI
	 */
	private final Resource METRIC_URI = DQM.MalformedDatatypeLiteralsMetric;
	/**
	 * logger static object
	 */
	private static Logger logger = Logger
			.getLogger(MalformedDatatypeLiterals.class);
	/**
	 * total number of literals
	 */
	private double totalLiterals = 0;
	/**
	 * total number of malformed literals
	 */
	private double malformedLiterals = 0;
	/**
	 * list of problematic quads
	 */
	protected List<Quad> problemList = new ArrayList<Quad>();

	/**
	 * This method identify whether a given quad is malformed or not.
	 * 
	 * @param quad
	 *            - to be identified
	 */
	@Override
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
					logger.debug("MalformedRDFDataTypeLiteral :: "
							+ object.toString());
				}
				this.totalLiterals++;
			}
			logger.debug("Literal :: " + object.toString());
		}
		logger.debug("Object :: " + object.toString());
		logger.trace("compute() --Ended--");
	}

	/**
	 * Returns metric value for object this class
	 * 
	 * @return (number of malformed literals) / (total number of literals)
	 */
	@Override
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

	/**
	 * Returns the metric URI
	 * 
	 * @return the metric URI
	 */
	@Override
	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	/**
	 * Returns list of problematic Quads
	 * 
	 * @return list of problematic quads
	 */
	@Override
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
	
	/**
     * Writes problematic instances to given stream
     * 
     * @param inputSource - name/URI of source
     * @param outputStream - stream where instances are to be written
     */
    public void outProblematicInstancesToStream(String inputSource, OutputStream outputStream) {
           
           Model model = ModelFactory.createDefaultModel();
           
           Resource qp = QR.MalformedDatatypeLiterals;
           qp.addProperty(QR.isDescribedBy, this.METRIC_URI);
           
           for(int i=0; i < this.problemList.size(); i++){
                   model.add(qp,QR.problematicThing,this.problemList.get(i).getObject().toString());     
           }
           
           model.add(QR.QualityReport,QR.computedOn,inputSource);
           model.add(QR.QualityReport,QR.hasProblem,qp);
           model.write(outputStream);
    }
}