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
	public final void testCompute() {

		logger.trace("testCompute() --Started--");
		List<Quad> streamingQuads = loader.getStreamingQuads();
		for(Quad quad : streamingQuads){
			homogeneousDatatypes.compute(quad);
		}
		
		double metricValue = homogeneousDatatypes.metricValue();
		
		logger.info("Total Properties with Heterogeneous Datatype :: " + homogeneousDatatypes.getPropertiesWithHeterogeneousDatatype());
		logger.info("Total Properties :: " + homogeneousDatatypes.getTotalProperties());
		logger.info("Metric Value ::" + metricValue);
		
		assertEquals(0.2, metricValue, 0.00001);
		logger.trace("testCompute() --Ended--");
		
	}

}
