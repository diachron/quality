/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.syntacticvalidity;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Quad;

import eu.diachron.qualitymetrics.utilities.TestLoader;

/**
 * @author Jeremy Debattista
 * 
 */
public class CorrectLanguageTagTest extends Assert {

	protected TestLoader loader = new TestLoader();
	protected CorrectLanguageTag metric = new CorrectLanguageTag();

	
	@Before
	public void setUp() throws Exception {
		loader.loadDataSet("testdumps/SampleInput_CorrectLanguageTag.ttl");
		metric.before();
	}
	
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testEntitiesAsMembersOfDisjointClassesMinimalExample() {
		List<Quad> streamingQuads = loader.getStreamingQuads();
		
		for(Quad quad : streamingQuads){
			// here we start streaming triples to the quality metric
			metric.compute(quad);
		}
		
		// total 7
		// correct 5
		// 5 / 7
		assertEquals(0.71428571428,metric.metricValue(), 0.0001);
	}	
}
