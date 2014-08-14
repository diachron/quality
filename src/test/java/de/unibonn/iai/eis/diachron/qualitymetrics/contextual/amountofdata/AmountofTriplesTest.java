package de.unibonn.iai.eis.diachron.qualitymetrics.contextual.amountofdata;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.TestLoader;
import eu.diachron.qualitymetrics.contextual.amountofdata.AmountOfTriples;

public class AmountofTriplesTest extends Assert{
	
	protected TestLoader loader = new TestLoader();
	protected AmountOfTriples metric = new AmountOfTriples();

	@Before
	public void setUp() throws Exception {
		loader.loadDataSet();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAmountofTriplesMetric() {
			List<Quad> streamingQuads = loader.getStreamingQuads();
			
			for(Quad quad : streamingQuads){
				// here we start streaming triples to the quality metric
				metric.compute(quad);
			}
			
			
		
		//System.out.println(metric.metricValue());
		//Number of triples we have is 94 so the expected value is 0
		assertEquals(0.0,metric.metricValue(), 0.0);
	}
}

