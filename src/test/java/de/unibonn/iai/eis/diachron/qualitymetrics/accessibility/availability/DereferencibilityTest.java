package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.TestLoader;
import eu.diachron.qualitymetrics.accessibility.availability.Dereferencibility;
import eu.diachron.qualitymetrics.utilities.CommonDataStructures;

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
	public void testDereferencibilityMetric() throws InterruptedException {
		List<Quad> streamingQuads = loader.getStreamingQuads();
		
		for(Quad quad : streamingQuads){
			// here we start streaming triples to the quality metric
			metric.compute(quad);
		}
		
		metric.after();

		// The expected value is calculated by going through all possible URIs
		// in HyperThing.org and using DEV http client for Chrome.
		// We had a total of 59 unique URIs and 16 had a 303 See Other code
		// or hash URI. In HyperThing 2 URIs gave problems which was
		// beyond our capabilities.
		assertEquals(0.271186441,metric.metricValue(), 0.0001);
	}
}
