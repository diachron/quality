/**
 * 
 */
package eu.diachron.qualitymetrics.representational.representationalconciseness;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.annotations.QualityReport;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.qualitymetrics.utilities.TestLoader;

/*
 * SPARQL Query used to get all URIs
 * SELECT DISTINCT (STR(?uri) as ?label)  {
 * 		{ ?uri ?p ?o . } UNION
 * 		{ ?s ?p ?uri}
 * 		FILTER (isURI(?uri) && (REGEX(STR(?uri),"^http") || REGEX(STR(?uri),"^https")) && (?p != rdf:type))
 * }
 * 
 */

/**
 * @author Jeremy Debattista
 * 
 * Test for the Short URI Metric.
 * In the used dataset, there are 66 URIS
 * that do not adhere to the set rules
 * and a total of 534 unique URIs 
 */
public class ShortURIsTest extends Assert {

	TestLoader loader = new TestLoader();
	ShortURIs metric = new ShortURIs();
	
	@Before
	public void setUp(){
		loader.loadDataSet("testdumps/eis.ttl");
	}
	
	@Test
	public void noBlankNodesTest(){
		for(Quad q : loader.getStreamingQuads()){
			metric.compute(q);
		}
		
		assertEquals(0.87640449, metric.metricValue(), 0.00001);
	}
	
	@Ignore
	@Test
	public void problemReportTest(){
		for(Quad q : loader.getStreamingQuads()){
			metric.compute(q);
		}
		
		metric.metricValue();
		
		ProblemList<?> pl = metric.getQualityProblems();
		QualityReport qr = new QualityReport();
		String plModelURI = qr.createQualityProblem(metric.getMetricURI(), pl);
		Model plModel = qr.getProblemReportFromTBD(plModelURI);
		
		plModel.write(System.out, "TURTLE");
	}
	
}
