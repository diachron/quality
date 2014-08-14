package de.unibonn.iai.eis.diachron.qualitymetrics.representational.conciseness;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.TestLoader;
import eu.diachron.qualitymetrics.representational.conciseness.ShortURIs;

public class ShortURIsTest extends Assert {
	
	private static Logger logger = Logger.getLogger(ShortURIsTest.class);
	
	protected TestLoader loader = new TestLoader();
	protected ShortURIs metric = new ShortURIs();

	@Before
	public void setUp() throws Exception {
		loader.loadDataSet();
	}

	@After
	public void tearDown() throws Exception {
		// No clean-up required
	}

	@Test
	public void testShortURIs() {
		logger.trace("Loading quads...");
		List<Quad> streamingQuads = loader.getStreamingQuads();
		int countLoadedQuads = 0;
		
		for(Quad quad : streamingQuads){
			// Here we start streaming triples to the quality metric
			metric.compute(quad);
			countLoadedQuads++;
		}
		logger.trace("Quads loaded, " + countLoadedQuads + " quads");
		
		// The test data dump contains 7 instance declarations, the sum of the length of the URIs identifying 
		// all ther subjects totals 225. Thus the expected value of the metric is: 225/7 = 32.14285
		double actual = 32.14285;
		double delta = 0.0001;
		double metricValue = metric.metricValue();
		logger.trace("Computed keeping-URIs-short metric: " + metricValue);
		
		assertEquals(actual, metricValue, delta);
	}

}
