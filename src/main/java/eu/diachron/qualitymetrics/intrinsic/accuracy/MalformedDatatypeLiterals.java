package eu.diachron.qualitymetrics.intrinsic.accuracy;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import eu.diachron.semantics.vocabulary.DQM;


/**
 * Detects whether the value of a typed literal is valid with respect to its
 * given xsd datatype.
 * 
 * Metric Value Range : [0 - 1] Best Case : 0 Worst Case : 1
 */
public class MalformedDatatypeLiterals implements QualityMetric {
	private static final Logger LOG = Logger.getLogger(MalformedDatatypeLiterals.class);
	private static final Resource METRIC_URI = DQM.MalformedDatatypeLiteralsMetric;

	private List<Quad> problems = new ArrayList<Quad>();

	private int literals = 0;
	private int malformedLiterals = 0;

	/**
	 * Identifies whether a given quad is malformed.
	 * @param quad A quad to check for quality problems.
	 */
	public void compute(Quad quad) {
		Node object = quad.getObject();
		if (object.isLiteral()) {
			detectMalformedDatatype(quad);
			literals++;
		}
	}

	private void detectMalformedDatatype(Quad quad) {
		Node object = quad.getObject();
		RDFDatatype rdfdataType = object.getLiteralDatatype();

		if (rdfdataType != null && !rdfdataType.isValidLiteral(object.getLiteral())) {
			malformedLiterals++;
			problems.add(quad);
			LOG.info(String.format("Malformed literal is found in quad: %s", quad.toString()));
		}
	}

	/**
	 * Calculates a metric value. Ratio of literals with malformed data types to
	 * total number of literals.
	 * @return Ratio of literals with malformed data types to total number of literals.
	 */
	public double metricValue() {
		if (literals == 0) {
			LOG.warn("Total number of literals in given document is 0.");
			return 0.0;
		}
		return (double) malformedLiterals / (double) literals;
	}

	public Resource getMetricURI() {
		return METRIC_URI;
	}

	public ProblemList<?> getQualityProblems() {
		try {
			return new ProblemList<Quad>(problems);
		} catch (ProblemListInitialisationException e) {
			LOG.error(e.getLocalizedMessage());
		}
		// TODO change ProblemList
		return null;
	}
}