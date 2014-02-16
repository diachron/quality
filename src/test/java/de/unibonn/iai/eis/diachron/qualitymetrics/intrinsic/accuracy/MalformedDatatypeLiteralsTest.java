/**
 * 
 */
package de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.accuracy;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.TestLoader;

/**
 * @author Muhammad Ali Qasmi
 * @date 13th Feb 2014
 */
public class MalformedDatatypeLiteralsTest {

	static Logger logger = Logger.getLogger(MalformedDatatypeLiteralsTest.class);
	
	protected TestLoader loader = new TestLoader();
	protected MalformedDatatypeLiterals malformedDatatypeLiterals = new MalformedDatatypeLiterals();
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		BasicConfigurator.configure();
		loader.loadDataSet();
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
		//fail("Not yet implemented"); // TODO
		logger.trace("testCompute() --Ended--");
	}

	/**
	 * Test method for {@link de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.accuracy.MalformedDatatypeLiterals#getName()}.
	 */
	@Test
	public final void testGetName() {
		//fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.accuracy.MalformedDatatypeLiterals#metricValue()}.
	 */
	@Test
	public final void testMetricValue() {
		logger.trace("testMetricValue() --Started--");
		assertEquals(1.0,malformedDatatypeLiterals.metricValue(), 0.0); // TODO Get valid rdf document
		logger.trace("testMetricValue() --Ended--");
	}

	/**
	 * Test method for {@link de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.accuracy.MalformedDatatypeLiterals#toDAQTriples()}.
	 */
	@Test
	public final void testToDAQTriples() {
		//fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.lang.Object#Object()}.
	 */
	@Test
	public final void testObject() {
		//fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.lang.Object#getClass()}.
	 */
	@Test
	public final void testGetClass() {
		//fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.lang.Object#hashCode()}.
	 */
	@Test
	public final void testHashCode() {
		//fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.lang.Object#equals(java.lang.Object)}.
	 */
	@Test
	public final void testEquals() {
		//fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.lang.Object#clone()}.
	 */
	@Test
	public final void testClone() {
		//fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.lang.Object#toString()}.
	 */
	@Test
	public final void testToString() {
		//fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.lang.Object#notify()}.
	 */
	@Test
	public final void testNotify() {
		//fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.lang.Object#notifyAll()}.
	 */
	@Test
	public final void testNotifyAll() {
		//fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.lang.Object#wait(long)}.
	 */
	@Test
	public final void testWaitLong() {
		//fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.lang.Object#wait(long, int)}.
	 */
	@Test
	public final void testWaitLongInt() {
		//fail("Not yet implemented"); // TODO
	}


	/**
	 * Test method for {@link java.lang.Object#wait()}.
	 */
	@Test
	public final void testWait() {
		//fail("Not yet implemented"); // TODO
	}
	/**
	 * Test method for {@link java.lang.Object#finalize()}.
	 */
	@Test
	public final void testFinalize() {
		
		//fail("Not yet implemented"); // TODO
	}

}
