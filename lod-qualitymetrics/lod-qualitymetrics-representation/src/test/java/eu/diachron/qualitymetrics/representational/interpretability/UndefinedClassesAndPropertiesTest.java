/**
 * 
 */
package eu.diachron.qualitymetrics.representational.interpretability;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Quad;

import eu.diachron.qualitymetrics.utilities.TestLoader;


/**
 * @author Jeremy Debattista
 * 
 * Test for the Undefined Classes and Properties Metric.
 * In the used dataset, there are 11 Undefined Classes,
 * 33 Undefined Properties and a total of 145 unique
 * classes and properties.
 * 
 */
public class UndefinedClassesAndPropertiesTest  extends Assert {

	TestLoader loader = new TestLoader();
	UndefinedClassesAndProperties metric = new UndefinedClassesAndProperties();
	
	@Before
	public void setUp(){
		loader.loadDataSet("testdumps/eis.ttl");
	}
	
	@Test
	public void noBlankNodesTest(){
		for(Quad q : loader.getStreamingQuads()){
			metric.compute(q);
		}
		
		assertEquals(0.696551724, metric.metricValue(), 0.00001);
	}

}
