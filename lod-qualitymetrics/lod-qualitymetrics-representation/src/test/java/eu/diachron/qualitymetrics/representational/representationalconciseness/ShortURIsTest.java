/**
 * 
 */
package eu.diachron.qualitymetrics.representational.representationalconciseness;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Quad;

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
	
}
