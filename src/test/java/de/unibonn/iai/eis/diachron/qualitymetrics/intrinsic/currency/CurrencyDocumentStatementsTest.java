package de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.currency;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.configuration.DataSetMappingForTestCase;
import de.unibonn.iai.eis.diachron.qualitymetrics.dynamicity.currency.CurrencyDocumentStatements;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.TestLoader;

public class CurrencyDocumentStatementsTest extends Assert {
	
	private static Logger logger = Logger.getLogger(CurrencyDocumentStatementsTest.class);
	
	protected TestLoader loader = new TestLoader();
	protected CurrencyDocumentStatements metric = new CurrencyDocumentStatements();
	
	private DateFormat fmtLastModifiedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private DateFormat fmtPublishingTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

	@Before
	public void setUp() throws Exception {
		loader.loadDataSet(DataSetMappingForTestCase.CurrencyDocumentStatements);
	}

	@After
	public void tearDown() throws Exception {
		// No clean-up required
	}

	@Test
	public void testCurrencyDocumentStatements() {
		// Load quads...
		List<Quad> streamingQuads = loader.getStreamingQuads();
		int countLoadedQuads = 0;
		
		for(Quad quad : streamingQuads){
			// Here we start streaming triples to the quality metric
			metric.compute(quad);
			countLoadedQuads++;
		}
		logger.trace("Quads loaded, " + countLoadedQuads + " quads");
		
		// Prepare the setup required to calculate the expected value of the metric. The Publishing Time and Last Modified Times known to 
		// be part of the test dataset are parsed according to the default custom datetime formats and the number of subjects qualified with
		// a Last Modified Time (countOfModSubjects) is set to three. There are two subjects with invalid Last Modified Time (countInvalidTimeSubjects)
		long observationTime = metric.getObservationTime().getTime();
		double countOfModSubjects = 3.0;
		double publishingTime = 0.0;
		double lastModTime1 = 0.0;
		double lastModTime2 = 0.0;
		double lastModTime3 = 0.0;
		
		try {
			publishingTime = fmtPublishingTime.parse("2010-05-10T17:03:45Z").getTime();
			lastModTime1 = fmtLastModifiedTime.parse("2014-01-11 12:34:31").getTime();
			lastModTime2 = fmtLastModifiedTime.parse("2014-01-11 12:34:23").getTime();
			lastModTime3 = fmtLastModifiedTime.parse("2014-03-22 05:59:01").getTime();
		} catch (ParseException pex) {
			logger.trace("Test for currency-of-documents/statements metric aborted. At least one date/time could not be parsed");
			fail("Invalid date or time in test setup. Could not complete test");
		}
		
		// The value of the metric is calculated according to the formula:
		// Currency = AVG[1 - (<Observation Time> - <Last Modified Time N>)/(<Observation Time> - <Publishing Time>)]
		// Where <Last Modified Time N> is the timestamp corresponding to the last modification of subject N, as described by the quads
		// * Note that the following expression, used to calculate the actual value, is equivalent to the expression above (good-ol'-algebra)
		double actual = (countOfModSubjects - 
				((observationTime - lastModTime1) + (observationTime - lastModTime2) + (observationTime - lastModTime3))/(observationTime - publishingTime))/
				countOfModSubjects;
		
		// Two subjects having invalid Last Modified Times are known to be part of the test dataset (the last two subjects)
		double countInvalidTimeSubjects = 2;
		double delta = 0.0001;
		double metricValue = metric.metricValue();
		logger.trace("Computed currency-of-documents/statements metric: " + metricValue);
		
		assertEquals(actual, metricValue, delta);
		assertEquals(countInvalidTimeSubjects, metric.getCountInvalidFormatDates(), delta);
	}

}