package de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.conciseness;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.configuration.DataSetMappingForTestCase;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.TestLoader;
import eu.diachron.qualitymetrics.intrinsic.conciseness.DuplicateInstance;

public class DuplicateInstanceTest extends Assert {
	
	private static Logger logger = Logger.getLogger(DuplicateInstanceTest.class);
	
	protected TestLoader loader = new TestLoader();
	protected DuplicateInstance metric = new DuplicateInstance();

	@Before
	public void setUp() throws Exception {
		loader.loadDataSet(DataSetMappingForTestCase.DuplicateInstance);
	}

	@After
	public void tearDown() throws Exception {
		// No clean-up required
	}

	@Test
	public void testDuplicateInstance() {
		logger.trace("Loading quads...");
		List<Quad> streamingQuads = loader.getStreamingQuads();
		int countLoadedQuads = 0;
		
		metric.before();
		
		for(Quad quad : streamingQuads){
			// Here we start streaming triples to the quality metric
			metric.compute(quad);
			countLoadedQuads++;
		}
		
		metric.after();

		logger.trace("Quads loaded, " + countLoadedQuads + " quads");
		
		// All instances are declared once in the test dump file, therefore the number of 
		// instances that violate the uniqueness rule is 0 and there are 4 instance declarations in total,
		// thus, the value of the duplicate instance metric is in this case: 1 - (0/4) = 1
		double actual = 1.0; 
		double delta = 0.0001;
		double metricValue = metric.metricValue();
		logger.trace("Computed duplicate instance metric: " + metricValue);
		
		assertEquals(actual, metricValue, delta);
	}

}
