package eu.diachron.qualitymetrics.intrinsic.consistency;

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
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.assessment.ComplexQualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import eu.diachron.qualitymetrics.utilities.Constants;
import eu.diachron.qualitymetrics.utilities.VocabularyReader;
import eu.diachron.semantics.vocabulary.DQM;

/**
 * Detects undefined classes and properties from data set by checking for its
 * definition in their respective referred vocabulary.
 * 
 * Metric Value Range : [0 - 1] Best Case : 0 Worst Case : 1
 */
public class UndefinedClasses implements ComplexQualityMetric {
	private static final Logger LOG = Logger.getLogger(UndefinedClasses.class);

	private static final Resource METRIC_URI = DQM.UndefinedClassesMetric;

	private long undefinedClasses = 0;
	private long classes = 0;

	private static Set<String> properties = new HashSet<String>();
	private List<Quad> problems = new ArrayList<Quad>();

	/**
	 * Loads a list of class properties.
	 * @param args Arguments, args[0] is a path to annotation properties file.
	 */
	public void before(Object... args) {
		String path = (args == null || args.length == 0) ? Constants.UNDEFINED_CLASS_PROPERTIES_FILE
				: (String) args[0];
		File file = null;
		try {
			if (!path.isEmpty()) {
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
			}
		} catch (FileNotFoundException e) {
			LOG.error(e.getLocalizedMessage());
		} catch (IOException e) {
			LOG.error(e.getLocalizedMessage());
		}
	}

	/**
	 * The method identifies whether a component (subject, predicate or object) of
	 * the given quad references an undefined class.
	 * @param quad A quad to check for quality problems.
	 */
	public void compute(Quad quad) {
		Node object = quad.getObject();

		if (properties.contains(quad.getPredicate().getURI()) && object.isURI()) {
			classes++;

			Model model = VocabularyReader.read(object.getURI());
			if (model.isEmpty()) {
				undefinedClasses++;
				problems.add(quad);
				LOG.info(String.format("Undefined class is found in quad: %s", quad.toString()));
			} else if (!model.getResource(object.getURI()).isURIResource()) {
				undefinedClasses++;
				problems.add(quad);
				LOG.info(String.format("Undefined class is found in quad: %s", quad.toString()));
			}
		}
	}

	/**
	 * This method returns metric value for the object of this class.
	 * @return The ratio of undefined classes to the total number of classes.
	 */
	public double metricValue() {
		if (classes == 0) {
			LOG.warn("Total number of classes is zero.");
			return 0.0;
		}
		return (double) undefinedClasses / classes;
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

	/**
	 * Clears a list of class properties.
	 */
	public void after(Object... args) {
		properties.clear();
	}
}
