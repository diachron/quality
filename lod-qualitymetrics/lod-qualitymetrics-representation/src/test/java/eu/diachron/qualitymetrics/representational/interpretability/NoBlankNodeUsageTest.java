/**
 * 
 */
package eu.diachron.qualitymetrics.representational.interpretability;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Quad;

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


}
