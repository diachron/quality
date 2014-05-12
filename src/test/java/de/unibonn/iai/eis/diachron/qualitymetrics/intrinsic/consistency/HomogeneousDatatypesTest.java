package de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.consistency;

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
 * Test class for {@link de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.consistency.HomogeneousDatatypes#compute(com.hp.hpl.jena.sparql.core.Quad)}.
 * 
 * @author Muhammad Ali Qasmi
 * @date 12th May 2014
 */
public class HomogeneousDatatypesTest extends Assert {

	static Logger logger = Logger.getLogger(HomogeneousDatatypesTest.class);
	
	protected TestLoader loader = new TestLoader();
	protected HomogeneousDatatypes homogeneousDatatypes =  new HomogeneousDatatypes();
	
	@Before
	public void setUp() throws Exception {
		BasicConfigurator.configure();
		loader.loadDataSet(DataSetMappingForTestCase.HomogeneousDatatypes);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	/**
	 * Test method for {@link de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.consistency.HomogeneousDatatypes#compute(com.hp.hpl.jena.sparql.core.Quad)}.
	 */
	public final void testCompute() {
		List<Quad> streamingQuads = loader.getStreamingQuads();
		for(Quad quad : streamingQuads){
			homogeneousDatatypes.compute(quad);
		}
		double metricValue = homogeneousDatatypes.metricValue();
		assertEquals(0.2, metricValue, 0.00001);
	}

}
