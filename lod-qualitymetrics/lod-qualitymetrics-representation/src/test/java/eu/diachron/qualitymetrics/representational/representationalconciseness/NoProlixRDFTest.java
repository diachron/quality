/**
 * 
 */
package eu.diachron.qualitymetrics.representational.representationalconciseness;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Quad;

import eu.diachron.qualitymetrics.utilities.TestLoader;

/**
 * @author Jeremy Debattista
 * 
 * Test for the No Prolix RDF Test Metric.
 * In the used dataset, there are 29 RCC
 * and a total of 1475 triples 
 */
public class NoProlixRDFTest extends Assert {

	TestLoader loader = new TestLoader();
	NoProlixRDF metric = new NoProlixRDF();
	
	@Before
	public void setUp(){
		loader.loadDataSet("testdumps/eis.ttl");
	}
	
	@Test
	public void noBlankNodesTest(){
		for(Quad q : loader.getStreamingQuads()){
			metric.compute(q);
		}
		
		assertEquals(0.98033898305, metric.metricValue(), 0.00001);
	}

}
