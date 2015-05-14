/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.availability;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Quad;

import eu.diachron.qualitymetrics.utilities.TestLoader;

/**
 * @author Jeremy Debattista
 * 
 */
public class DereferenceabiltyForwardLinksTest extends Assert {
	
	//private static Logger logger = LoggerFactory.getLogger(DereferenceabiltyForwardLinksTest.class);
	
	protected TestLoader loader = new TestLoader();
	protected EstimatedDereferenceabilityForwardLinks metric = new EstimatedDereferenceabilityForwardLinks();
	
	@Before
	public void setUp() throws Exception {
		loader.loadDataSet("http://imf.270a.info/dataset/MCORE");
	}
	
	@Test
	public void testDereferenceability() {
		// Load quads...
		List<Quad> streamingQuads = loader.getStreamingQuads();
		int counter = 0;
		
		for(Quad quad : streamingQuads){
			metric.compute(quad);
			counter++;
		}
		System.out.println(counter);
		
		System.out.println(metric.metricValue());
	}

}
