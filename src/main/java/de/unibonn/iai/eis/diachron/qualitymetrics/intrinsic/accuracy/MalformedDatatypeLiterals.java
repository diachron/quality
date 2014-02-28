package de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.accuracy;

import java.util.List;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.qualitymetrics.DimensionNamesOntology;
import de.unibonn.iai.eis.diachron.qualitymetrics.QualityMetric;

/**
 * @author Muhammad Ali Qasmi
 * @date 13th Feb 2014
 */
public class MalformedDatatypeLiterals implements QualityMetric {

	static Logger logger = Logger.getLogger(MalformedDatatypeLiterals.class);

	private double totalLiterals = 0;
	private double malformedLiterals = 0;

	public double getTotalLiterals() {
		return totalLiterals;
	}

	public double getMalformedLiterals() {
		return malformedLiterals;
	}

	public void compute(Quad quad) {
		logger.trace("compute() --Started--");
		// retrieves object from statement
		Node object = quad.getObject();
		// checks if object is a literal
		if (object.isLiteral()) {
			// retrieves rdfDataType from literal
			RDFDatatype rdfdataType = object.getLiteralDatatype();
			// check if rdf data type is a valid data type
			if (null != rdfdataType) {
				logger.debug("RdfDataTypeLiteral :: " + object.toString());
				if (!rdfdataType.isValidLiteral(object.getLiteral())) {
					this.malformedLiterals++;
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

	public String getName() {
		return "MalformedDatatypeLiterals";
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

	public List<Triple> toDAQTriples() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDimension() {
		return DimensionNamesOntology.INTRINSIC.ACCURACY;
	}

	public String getGroup() {
		return DimensionNamesOntology.INTRINSIC.GROUP_NAME;
	}

	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

}
