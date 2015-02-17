package eu.diachron.qualitymetrics.accessibility.performance;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.configuration.DataSetMappingForTestCase;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.qualitymetrics.utilities.TestLoader;

public class CorrectURIUsageTest extends Assert {
	
	private static Logger logger = LoggerFactory.getLogger(CorrectURIUsageTest.class);
	
	protected TestLoader loader = new TestLoader();
	
	protected CorrectURIUsage metric = new CorrectURIUsage();
	
	@Before
	public void setUp() throws Exception {
		loader.loadDataSet(DataSetMappingForTestCase.CorrectURIUsage);
	}

	@After
	public void tearDown() throws Exception {
		// No clean-up required
	}
	
	@Test
	public void testCorrectURIUsage() {
		// Load quads for the positive test case
		List<Quad> streamingQuads = loader.getStreamingQuads();
		int countLoadedQuads = 0;
		
		for(Quad quad : streamingQuads){
			// Here we start streaming triples to the quality metric
			metric.compute(quad);
			countLoadedQuads++;
		}
		logger.trace("Positive case: quads loaded, {} quads", countLoadedQuads);
		
		// Obtain the value of the Correct URI Usage metric
		double delta = 0.0;
		double metricValue = metric.metricValue();

		assertEquals(0.0, metricValue, delta);
		
		ProblemList<?> lstProblems = metric.getQualityProblems();
		assertEquals(lstProblems.getProblemList().size(), 14, 0.0);		
	}

}
