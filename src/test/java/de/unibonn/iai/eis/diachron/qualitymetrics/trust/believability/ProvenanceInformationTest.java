package de.unibonn.iai.eis.diachron.qualitymetrics.trust.believability;

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

public class ProvenanceInformationTest extends Assert {

private static Logger logger = LoggerFactory.getLogger(ProvenanceInformationTest.class);
	
	protected TestLoader loaderPositive = new TestLoader();
	protected TestLoader loaderNegative = new TestLoader();
	
	protected ProvenanceInformation metricPositive = new ProvenanceInformation();
	protected ProvenanceInformation metricNegative = new ProvenanceInformation();
	
	@Before
	public void setUp() throws Exception {
		loaderNegative.loadDataSet(DataSetMappingForTestCase.DuplicateInstance);
		loaderPositive.loadDataSet(DataSetMappingForTestCase.CurrencyDocumentStatements);
	}

	@After
	public void tearDown() throws Exception {
		// No clean-up required
	}

	@Test
	public void testMachineReadableLicense() {
		// Load quads for the positive test case
		List<Quad> streamingQuads = loaderPositive.getStreamingQuads();
		int countLoadedQuads = 0;
		
		for(Quad quad : streamingQuads){
			// Here we start streaming triples to the quality metric
			metricPositive.compute(quad);
			countLoadedQuads++;
		}
		logger.trace("Positive case: quads loaded, {} quads", countLoadedQuads);
		
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
		logger.trace("Computed machine-readable indication of a Authentisity of the Dataset metric; positive case: {}, negative case: {}", metricValuePositve, metricValueNegative);

		assertEquals(1.0, metricValuePositve, delta);
		assertEquals(0.0, metricValueNegative, delta);
	}

}
