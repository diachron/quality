package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.performance;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.TestLoader;
import eu.diachron.qualitymetrics.accessibility.performance.HighThroughput;

public class HighThroughputTest extends Assert {
	
	private static Logger logger = LoggerFactory.getLogger(HighThroughputTest.class);
	
	protected TestLoader loader = new TestLoader();
	protected HighThroughput metric = new HighThroughput();
	
	@Before
	public void setUp() throws Exception {
		loader.loadDataSet();
	}

	@After
	public void tearDown() throws Exception {
		// No clean-up required
	}

	@Test
	public void testHighThroughput() {
		// Load quads...
		List<Quad> streamingQuads = loader.getStreamingQuads();
		int countLoadedQuads = 0;
		
		for(Quad quad : streamingQuads){
			// Here we start streaming triples to the quality metric
			metric.compute(quad);
			countLoadedQuads++;
		}
		logger.trace("Quads loaded, {} quads", countLoadedQuads);
		
		// Obtain the estimated number of requests server per second by the endpoint the dataset comes from
		double metricValue = metric.metricValue();
		logger.trace("Computed high-throughput metric: " + metricValue);

		assertTrue("High Throughput should be a positive number", metricValue > 0.0);
	}

}
