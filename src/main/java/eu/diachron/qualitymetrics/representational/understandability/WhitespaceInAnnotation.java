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
 * WhitespaceInAnnotation consider the following widely used annotation
 * properties (labels, comments, notes, etc.) and identifies triples whose
 * property is from a pre-configured list of annotation properties, and whose
 * object value has leading or ending white space in string.
 * 
 * list of widely used annotation properties are stored in
 * ..src/main/resources/AnnotationPropertiesList.txt
 * 
 * Metric value Range = [0 - 1] Best Case = 0 Worst Case = 1
 * 
 * The metric value is defined as the ratio of annotations with whitespace to
 * all annotations (triples having such properties).
 * 
 * This metric is from the list of constrains for scientific pilots and is
 * introduced in the Deliverable 3.1 (Table 20)
 */
public class WhitespaceInAnnotation implements ComplexQualityMetric {
  private static final Logger LOG = Logger
      .getLogger(WhitespaceInAnnotation.class);
  private static final Resource METRIC_URI = DQM.WhitespaceInAnnotationMetric;

  private long literals = 0;
  private long whitespaceLiterals = 0;
  private Set<String> properties = new HashSet<String>();
  protected List<Quad> problems = new ArrayList<Quad>();

  /**
   * Loads a list of annotation properties.
   * @param args Arguments, args[0] is a path to annotation properties file.
   */
  public void before(Object... args) {
    String path = (args == null || args.length == 0) ? Constants.ANNOTATION_PROPERTIES_FILE
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
   * Check an object of a quad for whitespace.
   * 
   * @param quad
   *        A quad to check for quality problems.
   */
  public void compute(Quad quad) {
    Node predicate = quad.getPredicate();
    if (predicate.isURI() && properties.contains(predicate.getURI())) {
      literals++;
      detectWhitespaces(quad);
    }
  }

  /**
   * Check whether annotation in a quad has whitespace.
   * @param quad A quad to check for quality problems.
   */
  private void detectWhitespaces(Quad quad) {
    Node object = quad.getObject();
    if (object.isLiteral()) {
      String value = object.getLiteralValue().toString();
      if (!value.equals(value.trim())) {
        whitespaceLiterals++;
        problems.add(quad);
        LOG.info(String.format("Whitespce is found in annotation of quad: %s", quad.toString()));
      }
    }
  }

  /**
   * Calculates a metric value. Ratio of literals with whitespace to total number of literals.
   * @return Ratio of literals with whitespace to the total number of literals.
   */
  public double metricValue() {
    if (literals == 0) {
      LOG.warn("Total number of literals is 0.");
      return 0;
    }
    return (double) whitespaceLiterals / (double) literals;
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
    return null;
  }

  /**
   * Clears a list of annotation properties.
   */
  public void after(Object... arg0) {
    properties.clear();
  }
}
