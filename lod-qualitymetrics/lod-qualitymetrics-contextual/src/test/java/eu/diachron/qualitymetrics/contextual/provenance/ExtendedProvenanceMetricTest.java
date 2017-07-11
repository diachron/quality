/**
 * 
 */
package eu.diachron.qualitymetrics.contextual.provenance;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.apache.jena.sparql.core.Quad;

import eu.diachron.qualitymetrics.utilities.TestLoader;

/**
 * @author Jeremy Debattista
 * 
 */
public class ExtendedProvenanceMetricTest extends Assert {

	TestLoader loader = new TestLoader();
	ExtendedProvenanceMetric metric = new ExtendedProvenanceMetric();
	
	@Before
	public void setUp(){
		loader.loadDataSet("testdumps/prov.ttl");
	}
	
	@Test
	public void basicProvInfoMetricTest(){
		for(Quad q : loader.getStreamingQuads()){
			metric.compute(q);
		}
		
		assertEquals(0.75, metric.metricValue(), 0.0001);
	}
}
