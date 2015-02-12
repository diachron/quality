package eu.diachron.qualitymetrics.accessibility.interlinking;

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

public class LinkExternalDataProvidersTest extends Assert {
	
	private static Logger logger = LoggerFactory.getLogger(LinkExternalDataProvidersTest.class);
	
	protected TestLoader loader = new TestLoader();
	
	protected LinkExternalDataProviders metric = new LinkExternalDataProviders();
	
	@Before
	public void setUp() throws Exception {
		loader.loadDataSet(DataSetMappingForTestCase.CurrencyDocumentStatements);
	}

	@After
	public void tearDown() throws Exception {
		// No clean-up required
	}
	
	@Test
	public void testLinkExternalDataProviders() {
		
		// Set the dataset URI into the datasetURI property for the positive case, so that it's retrieved by EnvironmentProperties
		PropertyManager.getInstance().addToEnvironmentVars("datasetURI", "http://pleiades.stoa.org/places");
		
		// Load quads for the positive test case
		List<Quad> streamingQuads = loader.getStreamingQuads();
		int countLoadedQuads = 0;
		
		for(Quad quad : streamingQuads){
			// Here we start streaming triples to the quality metric
			metric.compute(quad);
			countLoadedQuads++;
		}
		logger.trace("Quads loaded, {} quads", countLoadedQuads);
		
		// Obtain the value of the existence of links to external data providers metric, 
		// 24 objects are of triples that are not in rdf:type statements and have an URI outside of the resource's URI
		// The sample dataset contains 5243 data-level constant URIs, of which 3 belong to external data provides
		double delta = 0.0001;
		double metricValue = metric.metricValue();

		assertEquals(0.00057219, metricValue, delta);
	}

}
