package de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.accuracy;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;

import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.qualitymetrics.intrinsic.accuracy.MalformedDatatypeLiterals;

public class MalformedDatatypeLiteralsTest {

  private static QualityMetric metric;
  private static List<Quad> quads = new ArrayList<Quad>();

  @BeforeClass
  public static void setUp() throws Exception {

    Model model = ModelFactory.createDefaultModel();
    model.createResource("http://example.org/#spiderman")
      .addLiteral(FOAF.birthday, ResourceFactory.createTypedLiteral("2012-03-11", XSDDatatype.XSDdate))
      .addLiteral(FOAF.givenname, ResourceFactory.createTypedLiteral("2012-03-11", XSDDatatype.XSDdate))
      .addLiteral(FOAF.name, ResourceFactory.createTypedLiteral("name", XSDDatatype.XSDstring))
      .addLiteral(FOAF.aimChatID, ResourceFactory.createTypedLiteral("3333", XSDDatatype.XSDboolean));

    StmtIterator si = model.listStatements();
    while (si.hasNext()) {
      quads.add(new Quad(null, si.next().asTriple()));
    }
  }

  @Test
  public void test() throws InstantiationException, IllegalAccessException {
    metric = new MalformedDatatypeLiterals();

    for (Quad quad : quads) {
      metric.compute(quad);
    }
    metric.metricValue();
    ProblemList<?> problems = metric.getQualityProblems();
    Assert.assertFalse(problems.getProblemList().isEmpty());
    Assert.assertTrue(problems.getProblemList().size() == 1);
    Assert.assertEquals(0.25, metric.metricValue(), 0.0);
  }

  // @Test
  public void emptyQuads() {
    metric = new MalformedDatatypeLiterals();

    // metric.compute(new ArrayList<Quad>());
    ProblemList<?> problems = metric.getQualityProblems();
    Assert.assertTrue(problems.getProblemList().isEmpty());
    Assert.assertTrue(problems.getProblemList().size() == 0);
    Assert.assertEquals(0.0, metric.metricValue(), 0.0);
  }

}
