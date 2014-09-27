package de.unibonn.iai.eis.diachron.qualitymetrics.trust.verifiability;

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

public class DigitalSignatureTest extends Assert {

private static Logger logger = LoggerFactory.getLogger(DigitalSignatureTest.class);
	
	protected TestLoader loaderPositive = new TestLoader();
	protected TestLoader loaderPositive2 = new TestLoader();
	protected TestLoader loaderNegative = new TestLoader();
	
	protected DigitalSignatures metricPositive = new DigitalSignatures();
	protected DigitalSignatures metricPositive2 = new DigitalSignatures();
	protected DigitalSignatures metricNegative = new DigitalSignatures();
	
	@Before
	public void setUp() throws Exception {
		loaderNegative.loadDataSet(DataSetMappingForTestCase.CurrencyDocumentStatements);
		loaderPositive.loadDataSet(DataSetMappingForTestCase.CurrencyDocumentStatementsCM);
		loaderPositive2.loadDataSet(DataSetMappingForTestCase.CurrencyDocumentStatementsCM2);
	}

	@After
	public void tearDown() throws Exception {
		// No clean-up required
	}

	@Test
	public void testPositiveCase() {
		// Load quads for the positive test case
		List<Quad> streamingQuads = loaderPositive.getStreamingQuads();
		int countLoadedQuads = 0;
		
		for(Quad quad : streamingQuads){
			// Here we start streaming triples to the quality metric
			metricPositive.compute(quad);
			countLoadedQuads++;
		}
		logger.trace("Positive case one: quads loaded, {} quads", countLoadedQuads);
		
		// Obtain the value of the machine-readable indication of a license metric, for the positive case
		double delta = 0.0001;
		double metricValuePositive = metricPositive.metricValue();
		
		assertEquals(1.0, metricValuePositive, delta);
	}
	
	@Test
	public void testNegativeCase() {
		// Load quads for the positive test case
		List<Quad> streamingQuads = loaderNegative.getStreamingQuads();
		int countLoadedQuads = 0;
		
				
		for(Quad quad : streamingQuads){
			// Here we start streaming triples to the quality metric
			metricNegative.compute(quad);
			countLoadedQuads++;
		}
		logger.trace("Negative case: quads loaded, {} quads", countLoadedQuads);

		// Obtain the value of the machine-readable indication of a license metric, for the positive case
		double delta = 0.0001;
		
		// Obtain the value of the machine-readable indication of a license metric, for the negative case
		double metricValueNegative = metricNegative.metricValue();
		
		assertEquals(0.0, metricValueNegative, delta);
	}

	@Test
	public void testNeutralCase() {
		// Load quads for the positive test case
		List<Quad> streamingQuads = loaderPositive2.getStreamingQuads();
		int countLoadedQuads = 0;
		
		for(Quad quad : streamingQuads){
			// Here we start streaming triples to the quality metric
			metricPositive2.compute(quad);
			countLoadedQuads++;
		}
		logger.trace("Positive case two: quads loaded, {} quads", countLoadedQuads);
		
		
		// Obtain the value of the machine-readable indication of a license metric, for the positive case
		double delta = 0.0001;
		
		// Obtain the value of the machine-readable indication of a license metric, for the positive case
		double metricValuePositive2 = metricPositive2.metricValue();
				
		assertEquals(1.0, metricValuePositive2, delta);
	}
	
}
