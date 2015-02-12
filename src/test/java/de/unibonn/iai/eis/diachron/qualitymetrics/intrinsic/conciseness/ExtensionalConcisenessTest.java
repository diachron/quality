package de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.conciseness;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Quad;

import eu.diachron.qualitymetrics.utilities.TestLoader;
import eu.diachron.qualitymetrics.intrinsic.conciseness.ExtensionalConcisenessNew;

public class ExtensionalConcisenessTest extends Assert {
	
	private static Logger logger = Logger.getLogger(ExtensionalConcisenessTest.class);
	
	protected TestLoader loader = new TestLoader();
	protected ExtensionalConcisenessNew metric = new ExtensionalConcisenessNew();

	@Before
	public void setUp() throws Exception {
		loader.loadDataSet();
	}

	@After
	public void tearDown() throws Exception {
		// No clean-up required
	}

	@Test
	public void testExtensionalConciseness() {
		logger.trace("Loading quads...");
		List<Quad> streamingQuads = loader.getStreamingQuads();
		int countLoadedQuads = 0;
		
		for(Quad quad : streamingQuads){
			// Here we start streaming triples to the quality metric
			metric.compute(quad);
			countLoadedQuads++;
		}
		logger.trace("Quads loaded, " + countLoadedQuads + " quads");
		
		// 9 subjects in the dump file, only two of them are equivalent:
		// <http://acrux.weposolutions.de/xodx/?c=person&id=Lukasw> foaf:knows people:NatanaelArndt . and 
		// <http://acrux.weposolutions.de/xodx/?c=person&id=toni> foaf:knows people:NatanaelArndt .
		// Thus, (No. of Unique Objects) / (Total No. of Objects in Dataset) = 8 / 9 = 0.888
		double actual = 0.8888; 
		double delta = 0.0001;
		double metricValue = metric.metricValue();
		logger.trace("Computed extensional conciseness metric: " + metricValue);
		
		assertEquals(actual, metricValue, delta);
	}

}
