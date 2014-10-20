package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.licensing;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.configuration.DataSetMappingForTestCase;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.TestLoader;
import de.unibonn.iai.eis.luzzu.properties.PropertyManager;
import eu.diachron.qualitymetrics.accessibility.licensing.IndicationAttributionCopyLeft;

public class IndicationAttributionCopyLeftTest extends Assert {
	
	private static Logger logger = LoggerFactory.getLogger(IndicationAttributionCopyLeftTest.class);
	
	protected TestLoader loaderPositive = new TestLoader();
	protected TestLoader loaderNegative = new TestLoader();
	
	protected IndicationAttributionCopyLeft metricPositive = new IndicationAttributionCopyLeft();
	protected IndicationAttributionCopyLeft metricNegative = new IndicationAttributionCopyLeft();
	
	@Before
	public void setUp() throws Exception {
		loaderPositive.loadDataSet(DataSetMappingForTestCase.SecureDataAccess);
		loaderNegative.loadDataSet(DataSetMappingForTestCase.CurrencyDocumentStatements);
	}

	@After
	public void tearDown() throws Exception {
		// No clean-up required
	}

	@Test
	public void testIndicationAttributionCopyLeft() {
		
		// Set the dataset URI into the datasetURI property for the positive case, so that it's retrieved by EnvironmentProperties
		PropertyManager.getInstance().addToEnvironmentVars("datasetURI", "https://raw.github.com/openphacts/ops-platform-setup/master/void/drugbank_void.ttl#drugbank-rdf");
		
		// Load quads for the positive test case
		List<Quad> streamingQuads = loaderPositive.getStreamingQuads();
		int countLoadedQuads = 0;
		
		for(Quad quad : streamingQuads){
			// Here we start streaming triples to the quality metric
			metricPositive.compute(quad);
			countLoadedQuads++;
		}
		logger.trace("Positive case: quads loaded, {} quads", countLoadedQuads);
		
		// Set the dataset URI into the datasetURI property for the negative case, so that it's retrieved by EnvironmentProperties
		PropertyManager.getInstance().addToEnvironmentVars("datasetURI", "http://pleiades.stoa.org/places");
		
		streamingQuads = loaderNegative.getStreamingQuads();
		countLoadedQuads = 0;
		
		for(Quad quad : streamingQuads){
			// Here we start streaming triples to the quality metric
			metricNegative.compute(quad);
			countLoadedQuads++;
		}
		logger.trace("Negative case: quads loaded, {} quads", countLoadedQuads);

		// Obtain the value of the machine-readable indication of a license metric, for the positive case
		double delta = 0.0001;
		double metricValuePositve = metricPositive.metricValue();
		
		// Obtain the value of the machine-readable indication of a license metric, for the negative case
		double metricValueNegative = metricNegative.metricValue();
		logger.trace("Computed indication of attribution CopyLeft metric; positive case: {}, negative case: {}", metricValuePositve, metricValueNegative);

		assertEquals(1.0, metricValuePositve, delta);
		assertEquals(0.0, metricValueNegative, delta);
	}

}
