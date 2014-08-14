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
import eu.diachron.qualitymetrics.dynamicity.currency.ExclusionOutdatedData;
import eu.diachron.qualitymetrics.dynamicity.currency.TemporalDataAnalyzer;

public class ExclusionOutdatedDataTest extends Assert {
	
	private static Logger logger = Logger.getLogger(ExclusionOutdatedDataTest.class);
	
	protected TestLoader loader = new TestLoader();
	protected ExclusionOutdatedData metric = new ExclusionOutdatedData();
	
	private DateFormat fmtValidTime = new SimpleDateFormat(TemporalDataAnalyzer.DateFormatters.XSD.getFormatSpecifier());
	
	@Before
	public void setUp() throws Exception {
		loader.loadDataSet(DataSetMappingForTestCase.CurrencyDocumentStatements);
	}

	@After
	public void tearDown() throws Exception {
		// No clean-up required
	}

	@Test
	public void testExclusionOutdatedData() {
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
		// a Last Modified Time (countOfModSubjects) is set to three. There is one subject with invalid Last Modified Time (countInvalidTimeSubjects)
		long observationTime = metric.getObservationTime().getTime();
		long validTimeStamps[] = new long[10];
		double countTotalSubjects = 1018.0;
		double countOutdatedSubjects = 0.0; 
		double countInvalidTimeSubjects = 1.0;
		
		// Calculate outdated subjects according to their dates, as current subjects today could be outdated tomorrow
		try {
			validTimeStamps[0] = fmtValidTime.parse("2014-06-10T17:03:45Z").getTime();
			validTimeStamps[1] = fmtValidTime.parse("2017-01-03T17:03:45+03:00").getTime();
			validTimeStamps[2] = fmtValidTime.parse("2015-12-14T17:03:45-05:00").getTime();
			validTimeStamps[3] = fmtValidTime.parse("2013-11-14T17:03:45Z").getTime();
			validTimeStamps[4] = fmtValidTime.parse("1999-08-14T17:03:45Z").getTime();
			validTimeStamps[5] = fmtValidTime.parse("2010-08-14T17:03:45Z").getTime();
			validTimeStamps[6] = fmtValidTime.parse("2000-01-14T17:03:45Z").getTime();
			validTimeStamps[7] = fmtValidTime.parse("1982-04-08T07:03:45Z").getTime();
			validTimeStamps[8] = fmtValidTime.parse("2014-05-28T03:03:45Z").getTime();
			validTimeStamps[9] = fmtValidTime.parse("2012-05-30T03:03:45Z").getTime();
		} catch (ParseException pex) {
			logger.trace("Test for exclusion-of-outdated-data metric aborted. At least one date/time could not be parsed");
			fail("Invalid date or time in test setup. Could not complete test");
		}
		
		// Determine how many of the subjects are already outdated
		for(long curValidTimeStamp : validTimeStamps) {
			// Compare the observation time with the expiration time of the subject to determine if it's outdated
			if(curValidTimeStamp <= observationTime) {
				countOutdatedSubjects += 1.0;
			}
		}

		// The value of the metric is calculated according to the formula:
		// Exclusion-of-Outdated-Data = 1 - (<Total Outdated Subjects> / <Total Subjects>)
		double actual = 1.0 - (countOutdatedSubjects/countTotalSubjects);
		
		double delta = 0.0001;
		double metricValue = metric.metricValue();
		double metricInvalidTimes = metric.getCountInvalidFormatDates();
		logger.trace("Computed exclusion of outdated data metric: " + metricValue);

		assertEquals(actual, metricValue, delta);
		assertEquals(countInvalidTimeSubjects, metricInvalidTimes, delta);
	}

}
