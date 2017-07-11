package eu.diachron.qualitymetrics.accessibility.performance;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.sparql.core.Quad;

import eu.diachron.qualitymetrics.utilities.TestLoader;

public class LowLatencyTest extends Assert {
	
	private static Logger logger = LoggerFactory.getLogger(LowLatencyTest.class);
	
	protected TestLoader loader = new TestLoader();
	protected LowLatency metric = new LowLatency();
	
	@Before
	public void setUp() throws Exception {
		loader.loadDataSet("/Users/jeremy/Documents/Workspaces/eis/quality/lod-qualitymetrics/lod-qualitymetrics-accessibility/src/test/resources/testdumps/performance.nt");
//		loader.loadDataSet("/Users/jeremy/Desktop/pisa.rkbexplorer.com.nt.gz");
	}

	@After
	public void tearDown() throws Exception {
		// No clean-up required
	}

	@Test
	public void testLowLatency() {
		metric.setDatasetURI("http://bfs.270a.info/");
		// Load quads...
		List<Quad> streamingQuads = loader.getStreamingQuads();
		int countLoadedQuads = 0;
		
		for(Quad quad : streamingQuads){
			// Here we start streaming triples to the quality metric
			metric.compute(quad);
			countLoadedQuads++;
		}
		logger.trace("Quads loaded, {} quads", countLoadedQuads);
		
		// Obtain the average measurement of the latency elicited by accessing the dataset's URI
		double metricValue = metric.metricValue();
		System.out.println("Computed low-latency metric: " + metricValue);

		assertTrue("Latency is out of range", (metricValue >= 0.0) && (metricValue <= 1.0));
		
		// The dataset resource that should be determined during metric computation is expected to have a reasonably 
		// low latency: https://raw.github.com/openphacts/ops-platform-setup/master/void/drugbank_void.ttl
		double expectedValue = 0.75;
		double delta = 0.25;
		assertEquals(expectedValue, metricValue, delta);
	}

}
