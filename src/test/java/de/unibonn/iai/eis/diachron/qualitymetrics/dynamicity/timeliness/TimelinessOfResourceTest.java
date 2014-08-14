package de.unibonn.iai.eis.diachron.qualitymetrics.dynamicity.timeliness;

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
import eu.diachron.qualitymetrics.dynamicity.currency.TemporalDataAnalyzer;
import eu.diachron.qualitymetrics.dynamicity.timeliness.TimelinessOfResource;

public class TimelinessOfResourceTest extends Assert {
	
private static Logger logger = Logger.getLogger(TimelinessOfResourceTest.class);
	
	protected TestLoader loader = new TestLoader();
	protected TimelinessOfResource metric = new TimelinessOfResource();
	
	private DateFormat fmtValidTimeFormat = new SimpleDateFormat(TemporalDataAnalyzer.DateFormatters.XSD.getFormatSpecifier());
	
	@Before
	public void setUp() throws Exception {
		loader.loadDataSet(DataSetMappingForTestCase.CurrencyDocumentStatements);
	}

	@After
	public void tearDown() throws Exception {
		// No clean-up required
	}

	@Test
	public void testTimelinessOfResource() {
		// Load quads...
		List<Quad> streamingQuads = loader.getStreamingQuads();
		int countLoadedQuads = 0;
		
		for(Quad quad : streamingQuads){
			// Here we start streaming triples to the quality metric
			metric.compute(quad);
			countLoadedQuads++;
		}
		logger.trace("Quads loaded, " + countLoadedQuads + " quads");
		
		// Prepare the setup required to calculate the expected value of the metric. The Expiration/Valid Times known to 
		// be part of the test dataset are parsed according to the default custom datetime formats and the number of subjects qualified with
		// that very property is set to 10. There are 1 subjects with invalid Expiration/Valid Time (countInvalidTimeSubjects)
		long observationTime = metric.getObservationTime().getTime();
		long validTimeStamps[] = new long[10];
		double countTotalValidSubjects = 10.0;
		double countInvalidTimeSubjects = 1.0;
		long accumValidityDifferences = 0;
		
		// Calculate outdated subjects according to their dates, as current subjects today could be outdated tomorrow
		try {
			validTimeStamps[0] = fmtValidTimeFormat.parse("2014-06-10T17:03:45Z").getTime();
			validTimeStamps[1] = fmtValidTimeFormat.parse("2017-01-03T17:03:45+03:00").getTime();
			validTimeStamps[2] = fmtValidTimeFormat.parse("2015-12-14T17:03:45-05:00").getTime();
			validTimeStamps[3] = fmtValidTimeFormat.parse("2013-11-14T17:03:45Z").getTime();
			validTimeStamps[4] = fmtValidTimeFormat.parse("1999-08-14T17:03:45Z").getTime();
			validTimeStamps[5] = fmtValidTimeFormat.parse("2010-08-14T17:03:45Z").getTime();
			validTimeStamps[6] = fmtValidTimeFormat.parse("2000-01-14T17:03:45Z").getTime();
			validTimeStamps[7] = fmtValidTimeFormat.parse("1982-04-08T07:03:45Z").getTime();
			validTimeStamps[8] = fmtValidTimeFormat.parse("2014-05-28T03:03:45Z").getTime();
			validTimeStamps[9] = fmtValidTimeFormat.parse("2012-05-30T03:03:45Z").getTime();
		} catch (ParseException pex) {
			logger.trace("Test for timeliness-of-the-resource metric aborted. At least one date/time could not be parsed");
			fail("Invalid date or time in test setup. Could not complete test");
		}
		
		// Accumulate the differences between the Observation Time and the Expiration/Valid Times, which will be averaged afterwards
		for(long curValidTimeStamp : validTimeStamps) {
			// Compare the observation time with the expiration time of the subject to determine if it's outdated
			accumValidityDifferences += (observationTime - curValidTimeStamp);
		}

		// The value of the metric is calculated according to the formula:
		// AVG(<Observation Time> - <Expiration/Valid Time>)
		double actual = ((double)accumValidityDifferences) / (countTotalValidSubjects);
		double delta = 0.0001;
		double metricValue = metric.metricValue();
		logger.trace("Computed timeliness-of-the-resource metric: " + metricValue);
		
		assertEquals(actual, metricValue, delta);
		assertEquals(countInvalidTimeSubjects, metric.getCountInvalidFormatDates(), delta);
	}

}
