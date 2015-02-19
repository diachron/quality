package eu.diachron.qualitymetrics.accessibility.performance;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.configuration.DataSetMappingForTestCase;
import eu.diachron.qualitymetrics.utilities.TestLoader;
import eu.diachron.qualitymetrics.accessibility.performance.DataSourceScalability;

public class DataSourceScalabilityTest extends Assert {
	
	private static Logger logger = LoggerFactory.getLogger(DataSourceScalabilityTest.class);
	
	protected TestLoader loader = new TestLoader();
	protected DataSourceScalability metric = new DataSourceScalability();
	
	@Before
	public void setUp() throws Exception {
		loader.loadDataSet(DataSetMappingForTestCase.DataSourceScalability);
	}

	@After
	public void tearDown() throws Exception {
		// No clean-up required
	}

	@Test
	public void testDataSourceScalability() {
		// Load quads...
		List<Quad> streamingQuads = loader.getStreamingQuads();
		int countLoadedQuads = 0;
				
		for(Quad quad : streamingQuads){
			// Here we start streaming triples to the quality metric
			metric.compute(quad);
			countLoadedQuads++;
		}
		logger.trace("Quads loaded, {} quads", countLoadedQuads);
		
		// Obtain the measurement of the scalability differential for the source of the dataset
		double metricValue = metric.metricValue();
		System.out.println("Computed scalability-of-a-datasource metric: " + metricValue);
		
		assertTrue("Scalability of a data source is out of range", (metricValue >= 0.0) && (metricValue <= 1.0));
		
		// The dataset resource that should be determined during metric computation is:
		// https://raw.github.com/openphacts/ops-platform-setup/master/void/drugbank_void.ttl, which is expected to be reasonably scalable
		double expectedValue = 0.8;
		double delta = 0.25;
		assertEquals(expectedValue, metricValue, delta);
	}

}
