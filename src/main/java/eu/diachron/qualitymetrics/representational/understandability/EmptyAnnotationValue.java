package eu.diachron.qualitymetrics.representational.understandability;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.xerces.util.URI;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.assessment.ComplexQualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import eu.diachron.qualitymetrics.utilities.Constants;
import eu.diachron.semantics.vocabulary.DQM;

/**
 * EmptyAnnotationValue consider the following widely used annotation properties
 * (labels, comments, notes, etc.) and identifies triples whose property is from
 * a pre-configured list of annotation properties, and whose object is an empty
 * string.
 *
 * list of widely used annotation properties are stored in
 * ..src/main/resources/AnnotationPropertiesList.txt
 *
 * The metric value is defined as the ratio of annotations with empty objects to
 * all annotations (triples having such properties).
 *
 * Metric value Range = [0 - 1] Best Case = 0 Worst Case = 1
 *
 * This metric is from the list of constrains for scientific pilots and is
 * introduced in the Deliverable 3.1 (Table 20)
 */
public class EmptyAnnotationValue implements ComplexQualityMetric {
	private static final Logger LOG = Logger.getLogger(EmptyAnnotationValue.class);

	private static final Resource METRIC_URI = DQM.EmptyAnnotationValueMetric;

	private long literals = 0;
	private long emptyLiterals = 0;

	private static Set<String> properties = new HashSet<String>();
	private List<Quad> problems = new ArrayList<Quad>();

	/**
	 * Loads a annotation properties.
	 * @param args Arguments, args[0] is a path to annotation properties file.
	 */
	public void before(Object... args) {
		String path = (args == null || args.length == 0) ? Constants.ANNOTATION_PROPERTIES_FILE
				: (String) args[0];
		File file = null;
		try {
			file = new File(path);
			if (file.exists() && file.isFile()) {
				String line = null;
				BufferedReader in = new BufferedReader(new FileReader(file));
				while ((line = in.readLine()) != null && !line.isEmpty()) {
					if (new URI(line.trim()) != null) {
						properties.add(line);
					}
				}
				in.close();
			}
		} catch (FileNotFoundException e) {
			LOG.error(e.getLocalizedMessage());
		} catch (IOException e) {
			LOG.error(e.getLocalizedMessage());
		}
	}

	/**
	 * The method checks if an object's value is empty.
	 * @param quad A quad to check for quality problems.
	 */
	public void compute(Quad quad) {
		Node predicate = quad.getPredicate();
		if (predicate.isURI() && properties.contains(predicate.getURI())) {
			literals++;

			if (isEmptyLiteral(quad)) {
				emptyLiterals++;
				problems.add(quad);
				LOG.info(String.format("Empty annotation is found in quad: %s", quad.toString()));
			}
		}
	}

	private boolean isEmptyLiteral(Quad quad) {
		Node object = quad.getObject();
		if (object.isBlank()) {
			return true;
		} else if (object.isLiteral()) {
			String value = object.getLiteralValue().toString().trim();
			if (value.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Calculates a metric value. Ratio of empty annotation literals to total
	 * number of literals.
	 * @return Ratio of empty annotation literals to the total number of literals.
	 */
	public double metricValue() {
		if (literals == 0) {
			LOG.warn("Total number of literals are ZERO");
			return 0;
		}
		return (double) emptyLiterals / (double) literals;
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

	public void after(Object... arg0) {
		properties.clear();
	}
}
