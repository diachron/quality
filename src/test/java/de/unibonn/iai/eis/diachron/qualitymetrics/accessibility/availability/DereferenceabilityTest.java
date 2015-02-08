package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import java.net.URL;
import java.util.List;

import de.unibonn.iai.eis.diachron.configuration.DataSetMappingForTestCase;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.TestLoader;
import eu.diachron.qualitymetrics.accessibility.availability.Dereferenceability;
import eu.diachron.qualitymetrics.utilities.HTTPRetriever;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.core.Quad;

public class DereferenceabilityTest extends Assert {
	
	private static Logger logger = LoggerFactory.getLogger(DereferenceabilityTest.class);
	
	protected TestLoader loader = new TestLoader();
	protected Dereferenceability metric = new Dereferenceability();

	@Before
	public void setUp() throws Exception {
		loader.loadDataSet(DataSetMappingForTestCase.Dereferenceability);
	}

	@After
	public void tearDown() throws Exception {
		// No clean-up required
	}

	@Test
	public void testDereferenceability() {
		
		ClassLoader classLoader = HTTPRetriever.class.getClassLoader();		
		URL resource = classLoader.getResource("org/apache/http/message/BasicLineFormatter.class");
		System.out.println(resource);

		// Load quads...
		List<Quad> streamingQuads = loader.getStreamingQuads();
		int countLoadedQuads = 0;
		
		for(Quad quad : streamingQuads){
			// Here we start streaming triples to the quality metric
			metric.compute(quad);
			countLoadedQuads++;
		}
		logger.trace("Quads loaded, {} quads", countLoadedQuads);
		
		// The dataset corresponding to the test case has: 19 URIs, of which 15 are dereferenceable and the rest 
		// non-dereferenceable (yield HTTP 404). Thus the expected value is: 15/19 = 0.7894736 
		double expectedValue = 0.789474;
		double delta = 0.001;
		
		// Obtain the measurement of Dereferenceability for the source of the dataset
		double metricValue = metric.metricValue();
		logger.trace("Computed dereferenceability metric: " + metricValue);

		assertEquals(expectedValue, metricValue, delta);
	}

}
