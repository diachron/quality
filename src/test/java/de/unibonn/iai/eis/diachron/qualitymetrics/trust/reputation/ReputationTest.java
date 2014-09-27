package de.unibonn.iai.eis.diachron.qualitymetrics.trust.reputation;

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

public class ReputationTest extends Assert {

private static Logger logger = LoggerFactory.getLogger(ReputationTest.class);
	
	protected TestLoader loaderPositive = new TestLoader();
	protected TestLoader loaderNegative = new TestLoader();
	protected TestLoader loaderMiddle = new TestLoader();
	
	protected Reputation metricPositive = new Reputation();
	protected Reputation metricNegative = new Reputation();
	protected Reputation metricMiddle = new Reputation();
	
	@Before
	public void setUp() throws Exception {
		loaderNegative.loadDataSet(DataSetMappingForTestCase.DuplicateInstance);
		loaderPositive.loadDataSet(DataSetMappingForTestCase.CurrencyDocumentStatements);
		loaderMiddle.loadDataSet(DataSetMappingForTestCase.CurrencyDocumentStatementsCM);
		metricPositive.setUriDataset("http://omim.bio2rdf.org/sparql");
		metricNegative.setUriDataset("http://localhost:8080/openrdf-sesame/repositories/test");
		metricMiddle.setUriDataset("http://genage.bio2rdf.org/sparql");
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
		logger.trace("Positive case: quads loaded, {} quads", countLoadedQuads);
		
		
		// Obtain the value of the machine-readable indication of a license metric, for the positive case
		double delta = 0.0001;
		double metricValuePositve = metricPositive.metricValue();
		
		
		assertEquals(1.0, metricValuePositve, delta);
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
	public void testMiddleCase() {
		// Load quads for the positive test case
		List<Quad> streamingQuads = loaderMiddle.getStreamingQuads();
		int countLoadedQuads = 0;
		
		
		for(Quad quad : streamingQuads){
			// Here we start streaming triples to the quality metric
			metricMiddle.compute(quad);
			countLoadedQuads++;
		}
		logger.trace("Middle case: quads loaded, {} quads", countLoadedQuads);

		// Obtain the value of the machine-readable indication of a license metric, for the positive case
		double delta = 0.0001;
		
		// Obtain the value of the machine-readable indication of a license metric, for the negative case
		double metricValueMiddle = metricMiddle.metricValue();
				
		assertEquals(0.6666, metricValueMiddle, delta);
	}

	
}
