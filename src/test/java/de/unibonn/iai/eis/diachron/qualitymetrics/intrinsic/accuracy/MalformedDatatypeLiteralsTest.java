/**
 * 
 */
package de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.accuracy;

import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.configuration.DataSetMappingForTestCase;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.TestLoader;

/**
 * @author Muhammad Ali Qasmi
 * @date 13th Feb 2014
 */
public class MalformedDatatypeLiteralsTest extends Assert {

	static Logger logger = Logger.getLogger(MalformedDatatypeLiteralsTest.class);
	
	protected TestLoader loader = new TestLoader();
	protected MalformedDatatypeLiterals malformedDatatypeLiterals = new MalformedDatatypeLiterals();

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		BasicConfigurator.configure();
		loader.loadDataSet(DataSetMappingForTestCase.MalformedDatatypeLiterals);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.accuracy.MalformedDatatypeLiterals#compute(com.hp.hpl.jena.sparql.core.Quad)}.
	 */
	@Test
	public final void testCompute() {
		logger.trace("testCompute() --Started--");
		List<Quad> streamingQuads = loader.getStreamingQuads();
		for(Quad quad : streamingQuads){
			malformedDatatypeLiterals.compute(quad);
		}
		logger.info("Metric Value ::" + malformedDatatypeLiterals.metricValue());
		assertEquals(0.166666,malformedDatatypeLiterals.metricValue(), 0.00001);
		logger.trace("testCompute() --Ended--");
	}
}
