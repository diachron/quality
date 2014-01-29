package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.TestLoader;

public class UnstructuredDataTest extends Assert{

	protected TestLoader loader = new TestLoader();
	protected UnstructuredData metric = new UnstructuredData();

	@Before
	public void setUp() throws Exception {
		loader.loadDataSet();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testUnstructuredData() {
		List<Quad> streamingQuads = loader.getStreamingQuads();
		
		for(Quad quad : streamingQuads){
			
			//Here we start streaming triples to the quality metric
			metric.compute(quad);
		}
		
		assertEquals(1.0,metric.metricValue(), 0.0);
	}
}
