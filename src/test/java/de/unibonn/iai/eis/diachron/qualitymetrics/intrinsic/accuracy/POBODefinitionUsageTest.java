package de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.accuracy;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.TestLoader;

public class POBODefinitionUsageTest  extends Assert{
	
	protected TestLoader loader = new TestLoader();
	protected POBODefinitionUsage metric = new POBODefinitionUsage();

	@Before
	public void setUp() throws Exception {
		loader.loadDataSet("testdumps/efosample.trig");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testPOBODefinitionUsage() {
		List<Quad> streamingQuads = loader.getStreamingQuads();
		
		for(Quad quad : streamingQuads){
			// here we start streaming triples to the quality metric
			metric.compute(quad);
		}
		
		assertEquals(1,metric.metricValue(), 0.0001);
	}	
	
}
