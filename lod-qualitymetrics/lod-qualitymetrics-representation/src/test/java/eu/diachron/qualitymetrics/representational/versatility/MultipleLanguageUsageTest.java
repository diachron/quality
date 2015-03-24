/**
 * 
 */
package eu.diachron.qualitymetrics.representational.versatility;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Quad;

import eu.diachron.qualitymetrics.utilities.TestLoader;

/**
 * @author Jeremy Debattista
 * 
 * Test for the Multiple Language Usage Metric
 * 
 */
public class MultipleLanguageUsageTest  extends Assert {

	TestLoader loader = new TestLoader();
	MultipleLanguageUsage metric = new MultipleLanguageUsage();
	
	@Before
	public void setUp(){
		loader.loadDataSet("testdumps/eis.ttl");
	}
	
	@Test
	public void noBlankNodesTest(){
		for(Quad q : loader.getStreamingQuads()){
			metric.compute(q);
		}
		
		assertEquals(1.0, metric.metricValue(), 0.00001);
	}
	
}
