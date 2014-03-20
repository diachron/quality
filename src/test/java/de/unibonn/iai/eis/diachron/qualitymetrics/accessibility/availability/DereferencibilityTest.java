package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.CommonDataStructures;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.TestLoader;

public class DereferencibilityTest extends Assert{
	
	protected TestLoader loader = new TestLoader();
	protected Dereferencibility metric = new Dereferencibility();

	@Before
	public void setUp() throws Exception {
		loader.loadDataSet();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDereferencibilityMetric() {
		List<Quad> streamingQuads = loader.getStreamingQuads();
		
		for(Quad quad : streamingQuads){
			// here we start streaming triples to the quality metric
			metric.compute(quad);
		}
		//metric.getDerefPassedURI();
		//CommonDataStructures.getDerefPassedURI();
		
		assertEquals(0.08176100628,metric.metricValue(), 0.00001);
	}
}
