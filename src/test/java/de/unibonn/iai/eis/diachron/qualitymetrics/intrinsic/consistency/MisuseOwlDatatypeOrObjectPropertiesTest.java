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
 * Test class for {@link de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.consistency.MisuseOwlDatatypeOrObjectProperties#compute(com.hp.hpl.jena.sparql.core.Quad)}.
 * 
 * @author Muhammad Ali Qasmi
 * @data 12th May 2014
 *
 */
public class MisuseOwlDatatypeOrObjectPropertiesTest extends Assert {

	static Logger logger = Logger.getLogger(MisuseOwlDatatypeOrObjectPropertiesTest.class);
	
	protected TestLoader loader = new TestLoader();
	protected MisuseOwlDatatypeOrObjectProperties misuseOwlDatatypeOrObjectProperty =  new MisuseOwlDatatypeOrObjectProperties();
	
	
	@Before
	public void setUp() throws Exception {
		BasicConfigurator.configure();
		loader.loadDataSet(DataSetMappingForTestCase.MisuseOwlDataTypeOrObjectProperties);
		MisuseOwlDatatypeOrObjectProperties.filterAllOwlProperties((loader.getStreamingQuads()));
	}

	@After
	public void tearDown() throws Exception {
	    MisuseOwlDatatypeOrObjectProperties.clearAllOwlPropertiesList();
	}

	@Test
	/**
	 * Test method for {@link de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.consistency.MisuseOwlDatatypeOrObjectProperties#compute(com.hp.hpl.jena.sparql.core.Quad)}.
	 */
	public final void testCompute() {
		List<Quad> streamingQuads = loader.getStreamingQuads();
		for(Quad quad : streamingQuads){
			misuseOwlDatatypeOrObjectProperty.compute(quad);
		}
		assertEquals(0.222222, misuseOwlDatatypeOrObjectProperty.metricValue(), 0.00001);
	}

}
