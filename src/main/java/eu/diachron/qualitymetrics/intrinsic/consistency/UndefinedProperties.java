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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.unibonn.iai.eis.luzzu.assessment.ComplexQualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import eu.diachron.semantics.vocabulary.DQM;
import eu.diachron.qualitymetrics.utilities.Constants;
import eu.diachron.qualitymetrics.utilities.VocabularyReader;

public class UndefinedProperties implements ComplexQualityMetric {

  private final Resource METRIC_URI = DQM.UndefinedPropertiesMetric;
  private static final Logger LOG = Logger.getLogger(UndefinedClasses.class);

  private long undefinedProperties = 0;
  private long properties = 0;

  private static Set<String> propertiesSet = new HashSet<String>();
  private List<Quad> problems = new ArrayList<Quad>();

  /**
   * Loads a list of properties.
   * @param args Arguments, args[0] is a path to properties file.
   */
  public void before(Object... args) {
    String path = (args == null || args.length == 0) ? Constants.UNDEFINED_PROPERTIES_FILE
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
              propertiesSet.add(line);
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
   * the given quad references an undefined class or property.
   * @param quad A quad to check for quality problems.
   */
  public void compute(Quad quad) {
    String predicateURI = quad.getPredicate().getURI();

    if (quad.getPredicate().isURI()) {
      properties++;
      Model model = VocabularyReader.read(predicateURI);
      if (model.isEmpty()) {
        undefinedProperties++;
        problems.add(quad);
        LOG.info(String.format("Undefined property is found: %s", predicateURI));
      } else if (model.getResource(predicateURI).isURIResource()) {
        checkDomainAndRange(model, predicateURI, quad);
      }
    }

    if (propertiesSet.contains(predicateURI)) {
      checkPropertyInObject(quad);
    }
  }

  private void checkDomainAndRange(Model model, String uri, Quad quad) {
    if (!(model.getResource(uri).hasProperty(RDFS.domain) && model.getResource(
        uri).hasProperty(RDFS.range))) {
      undefinedProperties++;
      problems.add(quad);
      LOG.info(String.format("Property has not a domain and range: %s", uri));
    }
  }

  private void checkPropertyInObject(Quad quad) {
    String objectURI = quad.getObject().getURI();
    if (quad.getObject().isURI()) {
      properties++;
      Model model = VocabularyReader.read(objectURI);
      if (model.isEmpty()) {
        undefinedProperties++;
        problems.add(quad);
        LOG.info(String.format("Object contains an undefined property: %s", objectURI));
      } else if (!model.getResource(objectURI).isURIResource()) {
        undefinedProperties++;
        problems.add(quad);
        LOG.info(String.format("Object contains an undefined property: %s", objectURI));
      }
    }
  }

  /**
   * This method returns metric value for the object of this class.
   * 
   * @return The ratio of undefined properties to the total number of properties.
   */
  public double metricValue() {
    if (properties == 0) {
      LOG.warn("Total number of properties is 0.");
      return 0.0;
    }
    return (double) undefinedProperties / (double) properties;
  }

  public Resource getMetricURI() {
    return this.METRIC_URI;
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
    propertiesSet.clear();
  }
}
