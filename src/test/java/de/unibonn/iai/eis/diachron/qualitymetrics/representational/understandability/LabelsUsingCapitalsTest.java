package de.unibonn.iai.eis.diachron.qualitymetrics.representational.understandability;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.unibonn.iai.eis.luzzu.assessment.ComplexQualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.qualitymetrics.representational.understandability.LabelsUsingCapitals;

public class LabelsUsingCapitalsTest {

  private static ComplexQualityMetric metric;
  private static List<Quad> quads;

  @BeforeClass
  public static void setUp() throws Exception {

    Model model = ModelFactory.createDefaultModel();
    model.createResource("http://example.org/#obj1")
        .addProperty(RDFS.comment, "Some name")
        .addProperty(RDF.type, FOAF.Person)
        .addProperty(RDFS.label, "SomeLabel")
        .addProperty(RDFS.label, "Otherlabel")
        .addProperty(RDF.type, FOAF.Person);
    model.createResource("http://example.org/#obj2")
        .addProperty(RDFS.comment, "The comment")
        .addProperty(RDFS.label, "OneMoreLabel")
        .addProperty(RDFS.label, "Label");

    quads = new ArrayList<Quad>();
    StmtIterator si = model.listStatements();
    while (si.hasNext()) {
      quads.add(new Quad(null, si.next().asTriple()));
    }
  }

  @Test
  public void metric() {
    metric = new LabelsUsingCapitals();
    metric.before();

    for (Quad quad : quads) {
      metric.compute(quad);
    }
    ProblemList<?> problems = metric.getQualityProblems();
    Assert.assertFalse(problems.getProblemList().isEmpty());
    Assert.assertTrue(problems.getProblemList().size() == 2);
    Assert.assertEquals(0.5, metric.metricValue(), 0.0);
  }

  // @Test
  public void emptyQuads() {
    metric = new LabelsUsingCapitals();
    metric.before();

    Model model = ModelFactory.createDefaultModel();
    List<Quad> quads = new ArrayList<Quad>();
    StmtIterator si = model.listStatements();
    while (si.hasNext()) {
      quads.add(new Quad(null, si.next().asTriple()));
    }

    for (Quad quad : quads) {
      metric.compute(quad);
    }
    ProblemList<?> problems = metric.getQualityProblems();
    Assert.assertFalse(problems.getProblemList().isEmpty());
    Assert.assertEquals(0.0, metric.metricValue(), 0.0);
  }

  @After
  public void tearDown() {
    metric.after();
  }
}
