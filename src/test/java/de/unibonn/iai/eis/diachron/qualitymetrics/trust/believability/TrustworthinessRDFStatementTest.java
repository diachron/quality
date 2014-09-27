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

public class TrustworthinessRDFStatementTest extends Assert {

private static Logger logger = LoggerFactory.getLogger(TrustworthinessRDFStatementTest.class);
	
	protected TestLoader loadernegone = new TestLoader();
	protected TestLoader loaderzero = new TestLoader();
	protected TestLoader loaderposone = new TestLoader();
	
	protected TrustworthinessRDFStatement metricPositive = new TrustworthinessRDFStatement();
	protected TrustworthinessRDFStatement metricNeutral = new TrustworthinessRDFStatement();
	protected TrustworthinessRDFStatement metricNegative = new TrustworthinessRDFStatement();
	
	@Before
	public void setUp() throws Exception {
		loadernegone.loadDataSet(DataSetMappingForTestCase.DuplicateInstance);
		loaderzero.loadDataSet(DataSetMappingForTestCase.CurrencyDocumentStatements);
		loaderposone.loadDataSet(DataSetMappingForTestCase.CurrencyDocumentStatementsCM);
	}

	@After
	public void tearDown() throws Exception {
		// No clean-up required
	}

	@Test
	public void testMachineReadableLicense() {
		// Load quads for the positive test case
		List<Quad> streamingQuads = loaderposone.getStreamingQuads();
		int countLoadedQuads = 0;
		
		for(Quad quad : streamingQuads){
			// Here we start streaming triples to the quality metric
			metricPositive.compute(quad);
			countLoadedQuads++;
		}
		logger.trace("Positive case: quads loaded, {} quads", countLoadedQuads);
				
		streamingQuads = loaderzero.getStreamingQuads();
		countLoadedQuads = 0;
		
		for(Quad quad : streamingQuads){
			// Here we start streaming triples to the quality metric
			metricNeutral.compute(quad);
			countLoadedQuads++;
		}
		logger.trace("Neutral case: quads loaded, {} quads", countLoadedQuads);
		
		streamingQuads = loadernegone.getStreamingQuads();
		countLoadedQuads = 0;
		
		for(Quad quad : streamingQuads){
			// Here we start streaming triples to the quality metric
			metricNegative.compute(quad);
			countLoadedQuads++;
		}
		logger.trace("Negative case: quads loaded, {} quads", countLoadedQuads);

		// Obtain the value of the machine-readable indication of a license metric, for the positive case
		double delta = 0.0001;
		double metricValuePositive = metricPositive.metricValue();
		double metricValueNeutral = metricNeutral.metricValue();
		
		double metricValueNegative = metricNegative.metricValue();
		logger.trace("Computed machine-readable indication of a Authentisity of the Dataset metric; positive case: {}, neutral case: {}, negative case: {}", metricValuePositive, metricValueNeutral, metricValueNegative);

		assertEquals(0.0, metricValuePositive, delta);
		assertEquals(-0.25, metricValueNeutral, delta);
		assertEquals(-1.0, metricValueNegative, delta);
	}

}
