package de.unibonn.iai.eis.diachron.qualitymetrics.dynamicity.currency;

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
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.TestLoader;
import eu.diachron.qualitymetrics.dynamicity.currency.TimeSinceModification;

public class TimeSinceModificationTest extends Assert {
	
private static Logger logger = Logger.getLogger(TimeSinceModificationTest.class);
	
	protected TestLoader loader = new TestLoader();
	protected TimeSinceModification metric = new TimeSinceModification();
	
	private DateFormat fmtLastModifiedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Before
	public void setUp() throws Exception {
		loader.loadDataSet(DataSetMappingForTestCase.CurrencyDocumentStatements);
	}

	@After
	public void tearDown() throws Exception {
		// No clean-up required
	}

	@Test
	public void testTimeSinceModification() {
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
		double lastModTime1 = 0.0;
		double lastModTime2 = 0.0;
		double lastModTime3 = 0.0;
		
		try {
			lastModTime1 = fmtLastModifiedTime.parse("2014-01-11 12:34:31").getTime();
			lastModTime2 = fmtLastModifiedTime.parse("2014-01-11 12:34:23").getTime();
			lastModTime3 = fmtLastModifiedTime.parse("2014-03-22 05:59:01").getTime();
		} catch (ParseException pex) {
			logger.trace("Test for time-since-modification metric aborted. At least one date/time could not be parsed");
			fail("Invalid date or time in test setup. Could not complete test");
		}
		
		// The value of the metric is calculated according to the formula:
		// Currency = AVG[<Observation Time> - <Last Modified Time N>]
		// Where <Last Modified Time N> is the timestamp corresponding to the last modification of subject N, as described by the quads
		double actual = ((observationTime - lastModTime1) + (observationTime - lastModTime2) + (observationTime - lastModTime3))/countOfModSubjects;
		
		// Two subjects having invalid Last Modified Times are known to be part of the test dataset (the last two subjects)
		double countInvalidTimeSubjects = 2;
		double delta = 0.0001;
		double metricValue = metric.metricValue();
		logger.trace("Computed time-since-modification metric: " + metricValue);

		assertEquals(actual, metricValue, delta);
		assertEquals(countInvalidTimeSubjects, metric.getCountInvalidFormatDates(), delta);
	}

}
