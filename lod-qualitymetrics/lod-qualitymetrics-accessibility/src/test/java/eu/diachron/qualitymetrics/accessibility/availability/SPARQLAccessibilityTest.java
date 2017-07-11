/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.availability;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.apache.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import eu.diachron.qualitymetrics.utilities.TestLoader;

/**
 * @author Jeremy Debattista
 * 
 */
public class SPARQLAccessibilityTest extends Assert{
	
	protected TestLoader loader = new TestLoader();
	protected SPARQLAccessibility metric = new SPARQLAccessibility();
	
	@Before
	public void setUp() throws Exception {
		loader.loadDataSet("http://bfs.270a.info/void.ttl");
		EnvironmentProperties.getInstance().setDatasetURI("http://bfs.270a.info/dataset/bfs");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testValidSparqlEndpoints() {
		List<Quad> streamingQuads = loader.getStreamingQuads();
		
		for(Quad quad : streamingQuads){
			// here we start streaming triples to the quality metric
			metric.compute(quad);
		}
	
		assertEquals(1.0,metric.metricValue(), 0.0001);
	}	
}
