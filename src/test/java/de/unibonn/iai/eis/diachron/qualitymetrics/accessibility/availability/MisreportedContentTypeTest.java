package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.TestLoader;
import eu.diachron.qualitymetrics.accessibility.availability.MisreportedContentType;

public class MisreportedContentTypeTest extends Assert{
	protected TestLoader loader = new TestLoader();
	protected MisreportedContentType metric = new MisreportedContentType();

	@Before
	public void setUp() throws Exception {
		loader.loadDataSet();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMisreportedContentType() {
		List<Quad> streamingQuads = loader.getStreamingQuads();
		
		for(Quad quad : streamingQuads){
			metric.compute(quad);
		}
		
		assertEquals(1.0,metric.metricValue(), 0.0001);
	}

}
