/**
 * 
 */
package eu.diachron.qualitymetrics.representational.interpretability;

import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;
import de.unibonn.iai.eis.luzzu.annotations.QualityReport;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.semantics.utilities.Commons;
import eu.diachron.qualitymetrics.utilities.SerialisableQuad;
import eu.diachron.qualitymetrics.utilities.TestLoader;



/*
 * SPARQL Query used to get all blank nodes in dataset:
 * 
 * SELECT DISTINCT (COUNT(*) as ?count) WHERE {
 * 	?s ?p ?o .
 * 	FILTER (isBlank(?s) || isBlank(?o))
 * }
 * 
 * SPARQL Query used to get all Unique DLC not rdf:type
 * SELECT (COUNT(DISTINCT ?s ) AS ?count) { 
 * 	{ ?s ?p ?o  } 
 * 	UNION { ?o ?p ?s } 
 * 	FILTER(!isBlank(?s) && !isLiteral(?s) && (?p != rdf:type)) }         
 * }
 * 
 */

/**
 * @author Jeremy Debattista
 * 
 * Test for the No Blank Node Usage Metric.
 * In the used dataset, there are 2 Blank Nodes
 * and a total of 573 unique DLC 
 */
public class NoBlankNodeUsageTest extends Assert {

	TestLoader loader = new TestLoader();
	NoBlankNodeUsage metric = new NoBlankNodeUsage();
	
	@Before
	public void setUp(){
		loader.loadDataSet("testdumps/eis.ttl");
	}
	

	@Test
	public void noBlankNodesTest(){
		for(Quad q : loader.getStreamingQuads()){
			metric.compute(q);
		}
		
		assertEquals(0.99652173913, metric.metricValue(), 0.00001);
	}
	

	@Ignore
	@Test
	public void problemReportTest(){
		for(Quad q : loader.getStreamingQuads()){
			metric.compute(q);
		}
		
		ProblemList<?> pl = metric.getQualityProblems();
		QualityReport qr = new QualityReport();
		String plModelURI = qr.createQualityProblem(metric.getMetricURI(), pl);
		Model plModel = qr.getProblemReportFromTBD(plModelURI);
		
		plModel.write(System.out, "TURTLE");
	
	}


	private Set<SerialisableQuad> _problemList = MapDbFactory.getMapDBAsyncTempFile().createHashSet("problem-list").make();

	@Ignore
	@Test
	public void problemReportStrechingTest(){
		Model m = ModelFactory.createDefaultModel();
		System.out.println("populating");
		for (long i = 0; i < 10000000; i++){
			Quad q = new Quad(null, Commons.generateURI().asNode(), m.createProperty("ex:something").asNode(), Commons.generateURI().asNode());
			_problemList.add(new SerialisableQuad(q));
		}
		
		
		System.out.println(_problemList.size());
		System.out.println("creating problem list");
		ProblemList<SerialisableQuad> pl = null;
		try {
			if(_problemList != null && _problemList.size() > 0) {
				pl = new ProblemList<SerialisableQuad>(_problemList);
			} else {
				System.out.println("some error");
				pl = new ProblemList<SerialisableQuad>();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
			QualityReport qr = new QualityReport();
			String plModelURI = qr.createQualityProblem(metric.getMetricURI(), pl);
			Model plModel = qr.getProblemReportFromTBD(plModelURI);
			System.out.println(plModel.size());
//			plModel.write(System.out, "TURTLE");
	}

}
