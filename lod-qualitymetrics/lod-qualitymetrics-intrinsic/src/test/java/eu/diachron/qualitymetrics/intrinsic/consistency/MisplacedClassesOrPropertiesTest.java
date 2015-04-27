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

import eu.diachron.qualitymetrics.utilities.TestLoader;

/**
 * @author Jeremy Debattista
 * 
 */
public class MisplacedClassesOrPropertiesTest extends Assert{
	
	protected TestLoader loader = new TestLoader();
	protected MisplacedClassesOrProperties metric = new MisplacedClassesOrProperties();
	
	@Before
	public void setUp() throws Exception {
		loader.loadDataSet("testdumps/Sample_MisplacedClassesAndProperties.ttl");
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
		
		// 1- (2 / 6)
		assertEquals(0.66666666666,metric.metricValue(), 0.0001);
	}	

}
