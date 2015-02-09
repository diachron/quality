package de.unibonn.iai.eis.diachron.qualitymetrics.dynamicity.volatility;

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
import eu.diachron.qualitymetrics.utilities.TestLoader;
import eu.diachron.qualitymetrics.dynamicity.currency.TemporalDataAnalyzer;
import eu.diachron.qualitymetrics.dynamicity.volatility.TimeValidityInterval;

public class TimeValidityIntervalTest extends Assert {
	
private static Logger logger = Logger.getLogger(TimeValidityIntervalTest.class);
	
	protected TestLoader loader = new TestLoader();
	protected TimeValidityInterval metric = new TimeValidityInterval();
	
	private DateFormat fmtValidTime = new SimpleDateFormat(TemporalDataAnalyzer.DateFormatters.XSD.getFormatSpecifier());
	private DateFormat fmtPublishingTime = new SimpleDateFormat(TemporalDataAnalyzer.DateFormatters.XSD.getFormatSpecifier());
	
	@Before
	public void setUp() throws Exception {
		loader.loadDataSet(DataSetMappingForTestCase.CurrencyDocumentStatements);
	}

	@After
	public void tearDown() throws Exception {
		// No clean-up required
	}

	@Test
	public void testTimeValidityInterval() {
		// Load quads...
		List<Quad> streamingQuads = loader.getStreamingQuads();
		int countLoadedQuads = 0;
		long accumValidityInterval = 0;
		
		for(Quad quad : streamingQuads){
			// Here we start streaming triples to the quality metric
			metric.compute(quad);
			countLoadedQuads++;
		}
		logger.trace("Quads loaded, " + countLoadedQuads + " quads");
		
		// Calculate validity of each of the instances in the sample input, for which a validity time is specified
		try {
			// "Earliest" publishing time found in the resource. Will be used as default publishing time
			long resPublishTime = fmtPublishingTime.parse("2010-05-10T17:03:45Z").getTime();

			accumValidityInterval += Math.max(0, fmtValidTime.parse("2014-06-10T17:03:45Z").getTime() - resPublishTime);
			accumValidityInterval += Math.max(0, fmtValidTime.parse("2017-01-03T17:03:45+03:00").getTime() - resPublishTime);
			accumValidityInterval += Math.max(0, fmtValidTime.parse("2015-12-14T17:03:45-05:00").getTime() - resPublishTime);
			accumValidityInterval += Math.max(0, fmtValidTime.parse("2013-11-14T17:03:45Z").getTime() - resPublishTime);
			accumValidityInterval += Math.max(0, fmtValidTime.parse("1999-08-14T17:03:45Z").getTime() - resPublishTime);
			accumValidityInterval += Math.max(0, fmtValidTime.parse("2010-08-14T17:03:45Z").getTime() - resPublishTime);
			accumValidityInterval += Math.max(0, fmtValidTime.parse("2000-01-14T17:03:45Z").getTime() - resPublishTime);
			accumValidityInterval += Math.max(0, fmtValidTime.parse("1982-04-08T07:03:45Z").getTime() - resPublishTime);
			accumValidityInterval += Math.max(0, fmtValidTime.parse("2014-05-28T03:03:45Z").getTime() - resPublishTime);
			// For this particular quad, a proper publishing time was specified, use it instead of the default, resource's publishing time
			accumValidityInterval += Math.max(0, fmtValidTime.parse("2012-05-30T03:03:45Z").getTime() - fmtPublishingTime.parse("2011-11-25T23:59:59Z").getTime());
		} catch (ParseException pex) {
			logger.trace("Test for exclusion-of-outdated-data metric aborted. At least one date/time could not be parsed");
			fail("Invalid date or time in test setup. Could not complete test");
		}
		
		// Set actual value as average of accumlated valid interval lengths, in seconds
		double actual = (((double)accumValidityInterval)/1000.0) / 10.0;
		double countInvalidTimeSubjects = 3.0;
		
		double delta = 0.0001;
		double metricValue = metric.metricValue();
		double metricInvalidTimes = metric.getCountInvalidFormatDates();
		logger.trace("Computed time-validity-interval metric: " + metricValue);

		assertEquals(actual, metricValue, delta);
		assertEquals(countInvalidTimeSubjects, metricInvalidTimes, delta);
	}


}
