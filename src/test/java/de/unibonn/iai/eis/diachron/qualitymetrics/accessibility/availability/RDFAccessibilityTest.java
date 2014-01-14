package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import java.util.List;

import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.TestLoader;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.graph.Triple;

public class RDFAccessibilityTest extends Assert {

	protected TestLoader loader = new TestLoader();
	protected RDFAccessibility metric = new RDFAccessibility();

	@Before
	public void setUp() throws Exception {
		loader.loadDataSet();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRDFAccessibility() {
		List<Triple> streamedTriples = loader.getStreamingTriples();
		
		for(Triple triple : streamedTriples){
			// here we start streaming triples to the quality metric
			metric.compute(triple);
		}
		
		assertEquals(metric.metricValue(),1.0, 0.0);
	}
}
