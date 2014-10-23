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
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.unibonn.iai.eis.luzzu.assessment.ComplexQualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.qualitymetrics.intrinsic.consistency.UndefinedClasses;

public class UndefinedClassesTest {
  private static ComplexQualityMetric metric;
  private static List<Quad> quads;

  @BeforeClass
  public static void setUpBeforeClass() throws ClassNotFoundException {

    Model model = ModelFactory.createDefaultModel();
    Resource rdfResource = model.createResource("http://example.org/them");
    model.createResource("http://example.org/#s")
        .addProperty(RDFS.subClassOf, FOAF.Person)
        .addProperty(RDFS.domain, FOAF.Agent)
        .addProperty(RDFS.range, FOAF.Agent)
        .addProperty(OWL.allValuesFrom, rdfResource)
        .addProperty(OWL.oneOf, rdfResource);

    quads = new ArrayList<Quad>();
    StmtIterator si = model.listStatements();
    while (si.hasNext()) {
      quads.add(new Quad(null, si.next().asTriple()));
    }
  }

  @Test
  public void metric() {
    metric = new UndefinedClasses();
    metric.before();
    for (Quad quad : quads) {
      metric.compute(quad);
    }

    ProblemList<?> problems = metric.getQualityProblems();
    Assert.assertFalse(problems.getProblemList().isEmpty());
    Assert.assertTrue(problems.getProblemList().size() == 2);
    Assert.assertEquals(0.4, metric.metricValue(), 0.0);
  }

  @AfterClass
  public static void tearDown() {
    metric.after();
  }
}
