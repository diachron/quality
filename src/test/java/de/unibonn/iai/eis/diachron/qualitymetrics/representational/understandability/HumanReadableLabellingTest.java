package de.unibonn.iai.eis.diachron.qualitymetrics.representational.understandability;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.TestLoader;
import eu.diachron.qualitymetrics.representational.understandability.HumanReadableLabelling;

public class HumanReadableLabellingTest extends Assert{
	
	protected TestLoader loader = new TestLoader();
	protected HumanReadableLabelling metric = new HumanReadableLabelling();

	@Before
	public void setUp() throws Exception {
		loader.loadDataSet();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testHumanReadableLabellingMetric() {
		List<Quad> streamingQuads = loader.getStreamingQuads();
		
		for(Quad quad : streamingQuads){
			// here we start streaming triples to the quality metric
			metric.compute(quad);
		}
		
		assertEquals(0.33333333,metric.metricValue(), 0.0001);
	}
}
