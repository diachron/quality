package de.unibonn.iai.eis.diachron.qualitymetrics.report;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;

public class QualityReportTest extends Assert {

	private QualityReport qr = new QualityReport();
	private ProblemList<Quad> plQuad;
	private ProblemList<Resource> plResource;
	private Resource metricURI = ModelFactory.createDefaultModel().createResource("urn:metric/Capitalisation789");
	
	@Before
	public void setUp() throws Exception {
		Resource graph = ModelFactory.createDefaultModel().createResource("ex:Graph");
		Resource joe = ModelFactory.createDefaultModel().createResource("ex:Joe");
		Literal joeLiteral = ModelFactory.createDefaultModel().createLiteral("JoeDoe");
		Resource UniBonn = ModelFactory.createDefaultModel().createResource("ex:UniBonn");
		Literal uniBonnLiteral = ModelFactory.createDefaultModel().createLiteral("UniBonn");	
		
		// set up a problemlist with quads
		List<Quad> lQuad = new ArrayList<Quad>();
		
		lQuad.add(new Quad(graph.asNode(), joe.asNode(), RDFS.label.asNode(), joeLiteral.asNode()));
		lQuad.add(new Quad(graph.asNode(), UniBonn.asNode(), RDFS.label.asNode(), uniBonnLiteral.asNode()));
		plQuad = new ProblemList<Quad>(lQuad);
		
		// set up a problemlist with resource
		List<Resource> lResource = new ArrayList<Resource>();
		lResource.add(joe);
		lResource.add(UniBonn);
		plResource = new ProblemList<Resource>(lResource);
		
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void generateQuadProblem(){
//		Resource metricURI = ModelFactory.createDefaultModel().createResource("urn:metric/Capitalisation789");
//		List<Statement> stmt = qr.createQualityProblem(metricURI, plQuad);
		//TODO
	
	}
	
	@Test
	public void generateSeqProblem(){
//		List<Statement> stmt = qr.createQualityProblem(metricURI, plResource);
	//TODO	
	}
	
	@Test
	public void generateQualityReport(){
		Resource computedOn = ModelFactory.createDefaultModel().createResource("ex:qrtest.trig");
		List<Statement> qp1 = qr.createQualityProblem(metricURI, plQuad);
		
		List<Resource> uris = new ArrayList<Resource>();
		uris.add(qr.getProblemURI(qp1));
		
		List<Statement> stmt = qr.createQualityReport(computedOn, uris);
		
		//TODO: asserts
	}
	
	@Ignore
	@Test
	public void generateModelForQualityReport(){
		
		// 1. Generate problem triples
		Resource computedOn = ModelFactory.createDefaultModel().createResource("ex:qrtest.trig");
		List<Statement> qp1 = qr.createQualityProblem(metricURI, plQuad);
		
		// 2. Generate quality report
		List<Resource> uris = new ArrayList<Resource>();
		uris.add(qr.getProblemURI(qp1));
		List<Statement> stmt = qr.createQualityReport(computedOn, uris);
		
		// 3. Create Model for report - this will be eventually replaced by the API call
		Model m = ModelFactory.createDefaultModel();
		m.add(stmt);
		m.add(qp1);
		
		m.write(System.out, "TURTLE");
	}
	
	

}
