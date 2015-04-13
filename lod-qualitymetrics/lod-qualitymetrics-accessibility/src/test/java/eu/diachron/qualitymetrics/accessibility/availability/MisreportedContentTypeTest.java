package eu.diachron.qualitymetrics.accessibility.availability;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.core.Quad;

import eu.diachron.qualitymetrics.utilities.TestLoader;

public class MisreportedContentTypeTest extends Assert {
	
	private static Logger logger = LoggerFactory.getLogger(MisreportedContentTypeTest.class);
	
	protected TestLoader loader = new TestLoader();
	protected EstimatedMisreportedContentType metric = new EstimatedMisreportedContentType();

	@Before
	public void setUp() throws Exception {
//		loader.loadDataSet(DataSetMappingForTestCase.MisreportedContentType);
		loader.loadDataSet("http://imf.270a.info/dataset/MCORE");
	}

	@After
	public void tearDown() throws Exception {
		// No clean-up required
	}
	
	@Test
	public void testMisreportedContentType() {
		// Load quads...
		List<Quad> streamingQuads = loader.getStreamingQuads();
		int countLoadedQuads = 0;
		
		for(Quad quad : streamingQuads){
			// Here we start streaming triples to the quality metric
			metric.compute(quad);
			countLoadedQuads++;
		}
		logger.trace("Quads loaded, {} quads", countLoadedQuads);
		
		// The dataset corresponding to the test case has 27 URIs (including rdf:type predicates) declared
		// in subjects/objects, of which 9 have misreported content types.
		double expectedValue = 1.0;
		double delta = 0.001;
		
		// Obtain the measurement of Dereferenceability for the source of the dataset
		double metricValue = metric.metricValue();
		logger.trace("Computed misreported content-type metric: " + metricValue);
		assertEquals(expectedValue, metricValue, delta);
	}

}
