/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.consistency;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.properties.PropertyManager;
import eu.diachron.qualitymetrics.utilities.TestLoader;

/**
 * @author Jeremy Debattista
 * 
 */
public class OntologyHijackingTest extends Assert{

	protected TestLoader loader = new TestLoader();
	protected OntologyHijacking metric = new OntologyHijacking();

	@Before
	public void setUp() throws Exception {
		PropertyManager.getInstance().addToEnvironmentVars("baseURI", "http://example.org/data/");

		loader.loadDataSet("testdumps/SampleInput_OntologyHijacking_Minimal.ttl");
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testOntologyHijacking() {
		List<Quad> streamingQuads = loader.getStreamingQuads();
		
		for(Quad quad : streamingQuads){
			// here we start streaming triples to the quality metric
			metric.compute(quad);
		}
		
		//Total Possible Hijacks = 5
		//Total Hijacks = 2
		
		// 1 - (2 / 5)
		assertEquals(0.6,metric.metricValue(), 0.0001);
	}	
}
