package de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.consistency;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.unibonn.iai.eis.luzzu.assessment.ComplexQualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.qualitymetrics.intrinsic.consistency.UndefinedProperties;

public class UndefinedPropertiesTest {
  private static ComplexQualityMetric metric;
  private static List<Quad> quads;

  @BeforeClass
  public static void setUpBeforeClass() {

    Model model = ModelFactory.createDefaultModel();
    Resource rdfResource = model.createResource("http://example.org/them");
    model.createResource("http://example.org/#")
      .addProperty(RDFS.subPropertyOf, rdfResource)
      .addProperty(ResourceFactory.createProperty("http://example.org/#s1"), rdfResource)
      .addProperty(ResourceFactory.createProperty("http://example.org/#s2"), rdfResource)
      .addProperty(OWL.onProperty, rdfResource)
      .addProperty(OWL.oneOf, rdfResource)
      .addProperty(RDFS.comment, ResourceFactory.createResource("http://example.org/#sp"));

    quads = new ArrayList<Quad>();
    StmtIterator si = model.listStatements();
    while (si.hasNext()) {
      quads.add(new Quad(null, si.next().asTriple()));
    }
  }

  @Test
  public void metric() {
    metric = new UndefinedProperties();
    metric.before();

    for (Quad quad : quads) {
      metric.compute(quad);
    }
    ProblemList<?> problems = metric.getQualityProblems();
    Assert.assertFalse(problems.getProblemList().isEmpty());
    Assert.assertTrue(problems.getProblemList().size() == 4);
    Assert.assertEquals(0.5, metric.metricValue(), 0.0);
  }

  @AfterClass
  public static void tearDown() {
    metric.after();
  }
}
