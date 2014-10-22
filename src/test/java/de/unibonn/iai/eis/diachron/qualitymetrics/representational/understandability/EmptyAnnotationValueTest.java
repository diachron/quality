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
import eu.diachron.qualitymetrics.representational.understandability.EmptyAnnotationValue;

public class EmptyAnnotationValueTest {

	private static ComplexQualityMetric metric;
	private static List<Quad> quads = new ArrayList<Quad>();

	@BeforeClass
	public static void setUp() {
		Model model = ModelFactory.createDefaultModel();
		model.createResource("http://example.org/#spiderman")
				.addProperty(RDFS.comment, "Name of Spiderman")
				.addProperty(RDF.type, "").addProperty(RDFS.label, "SpidErman")
				.addProperty(RDFS.label, "").addProperty(RDFS.comment, "");
		model.createResource("http://example.org/#green-goblin")
				.addProperty(RDFS.comment, "Name of Green Goblin")
				.addProperty(RDFS.label, "");

		StmtIterator si = model.listStatements();
		while (si.hasNext()) {
			quads.add(new Quad(null, si.next().asTriple()));
		}
	}

	@Test
	public void metric() {
		metric = new EmptyAnnotationValue();
		metric.before();
		for (Quad quad : quads) {
			metric.compute(quad);
		}

		ProblemList<?> problems = metric.getQualityProblems();
		Assert.assertFalse(problems.getProblemList().isEmpty());
		Assert.assertTrue(problems.getProblemList().size() == 3);
		Assert.assertEquals(0.5, metric.metricValue(), 0.0);
	}

	// @Test
	public void annotationNotInFile() {

		Model m = ModelFactory.createDefaultModel();
		m.createResource("http://example.org/#spiderman")
				.addProperty(RDFS.comment, "Name of Spiderman")
				.addProperty(RDF.type, FOAF.Person)
				.addProperty(RDFS.seeAlso, "SpidErman");

		ArrayList<Quad> quards = new ArrayList<Quad>();
		StmtIterator s = m.listStatements();
		while (s.hasNext()) {
			quards.add(new Quad(null, s.next().asTriple()));
		}

		metric = new EmptyAnnotationValue();
		metric.before();
		for (Quad quad : quards) {
			metric.compute(quad);
		}

		ProblemList<?> problems = metric.getQualityProblems();
		Assert.assertTrue(problems.getProblemList().isEmpty());
		Assert.assertEquals(0.0, metric.metricValue(), 0.0);
	}

	@After
	public void tearDown() {
		metric.after();
	}
}