package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.security;

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
import de.unibonn.iai.eis.luzzu.properties.PropertyManager;
import eu.diachron.qualitymetrics.accessibility.security.HTTPSDataAccess;

public class HTTPSDataAccessTest extends Assert {
	
	private static Logger logger = LoggerFactory.getLogger(HTTPSDataAccessTest.class);
	
	protected TestLoader loader = new TestLoader();
	protected HTTPSDataAccess metric = new HTTPSDataAccess();
	
	@Before
	public void setUp() throws Exception {
		loader.loadDataSet(DataSetMappingForTestCase.SecureDataAccess);
	}

	@After
	public void tearDown() throws Exception {
		// No clean-up required
	}

	@Test
	public void testHTTPSDataAccess() {
		
		// Set the dataset URI into the datasetURI property for the positive case, so that it's retrieved by EnvironmentProperties
		PropertyManager.getInstance().addToEnvironmentVars("datasetURI", "https://raw.github.com/openphacts/ops-platform-setup/master/void/drugbank_void.ttl#drugbank-rdf");
		
		// Load quads...
		List<Quad> streamingQuads = loader.getStreamingQuads();
		int countLoadedQuads = 0;
		
		for(Quad quad : streamingQuads){
			// Here we start streaming triples to the quality metric
			metric.compute(quad);
			countLoadedQuads++;
		}
		logger.trace("Quads loaded, {} quads", countLoadedQuads);
		
		double expectedValue = 1.0;
		double delta = 0.001;
		
		// Obtain the measurement of HTTPS data access for the source of the dataset
		double metricValue = metric.metricValue();
		logger.trace("Computed HTTPS-data-access metric: " + metricValue);

		assertEquals(expectedValue, metricValue, delta);
	}

}
