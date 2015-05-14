/**
 * 
 */
package eu.diachron.qualitymetrics.representational.versatility;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.properties.PropertyManager;
import eu.diachron.qualitymetrics.utilities.TestLoader;

/*
 * SPARQL Query Used 
 * 
 * SELECT DISTINCT (COUNT(?o) AS ?featureCount) {
 * 	?s <http://rdfs.org/ns/void#feature> ?o .
 * }
 * 
 */

/**
 * @author Jeremy Debattista
 * 
 * Tests the Different Serialisation Formats metric.
 * The dataset being used for tests has 2 different
 * serialisation formats
 */
public class DifferentSerialisationFormatsTest extends Assert {
	
	TestLoader loader = new TestLoader();
	DifferentSerialisationFormats metric = new DifferentSerialisationFormats();
	
	@Before
	public void setUp(){
		loader.loadDataSet("testdumps/eis.ttl");
		PropertyManager.getInstance().addToEnvironmentVars("baseURI", "http://eis.iai.uni-bonn.de/");
		PropertyManager.getInstance().addToEnvironmentVars("datasetURI", "http://eis.iai.uni-bonn.de/");
	}
	
	@Test
	public void noBlankNodesTest(){
		for(Quad q : loader.getStreamingQuads()){
			metric.compute(q);
		}
		
		assertEquals(1.0, metric.metricValue(), 0.00001);
	}
}
