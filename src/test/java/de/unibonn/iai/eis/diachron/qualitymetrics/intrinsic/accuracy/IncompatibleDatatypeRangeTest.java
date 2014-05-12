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
 * Test class for {@link de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.accuracy.IncompatibleDatatypeRange#compute(com.hp.hpl.jena.sparql.core.Quad)}.
 * 
 * @author Muhammad Ali Qasmi
 * @date 20th Feb 2014
 */
public class IncompatibleDatatypeRangeTest extends Assert {

	static Logger logger = Logger.getLogger(IncompatibleDatatypeRangeTest.class);
	
	protected TestLoader loader = new TestLoader();
	IncompatibleDatatypeRange incompatibleDatatypeRange = new IncompatibleDatatypeRange();
	
	@Before
	public void setUp() throws Exception {
		BasicConfigurator.configure();
		loader.loadDataSet(DataSetMappingForTestCase.IncompatibleDatatypeRange);
	}

	@After
	public void tearDown() throws Exception {
		IncompatibleDatatypeRange.clearCache();
	}

	/**
	 * Test method for {@link de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.accuracy.IncompatibleDatatypeRange#compute(com.hp.hpl.jena.sparql.core.Quad)}.
	 */
	@Test
	public final void testCompute() {
		List<Quad> streamingQuads = loader.getStreamingQuads();
		for(Quad quad : streamingQuads){
			incompatibleDatatypeRange.compute(quad);
		}
		assertEquals(0.153846153,incompatibleDatatypeRange.metricValue(), 0.00001);
	}

}