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

public class MisuseOwlDatatypeOrObjectPropertiesTest extends Assert {

	static Logger logger = Logger.getLogger(MisuseOwlDatatypeOrObjectPropertiesTest.class);
	
	protected TestLoader loader = new TestLoader();
	protected MisuseOwlDatatypeOrObjectProperties misuseOwlDatatypeOrObjectProperty =  new MisuseOwlDatatypeOrObjectProperties();
	
	
	@Before
	public void setUp() throws Exception {
		BasicConfigurator.configure();
		loader.loadDataSet(DataSetMappingForTestCase.MisuseOwlDataTypeOrObjectProperties);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testCompute() {
		logger.trace("testCompute() --Started--");
		List<Quad> streamingQuads = loader.getStreamingQuads();
		for(Quad quad : streamingQuads){
			misuseOwlDatatypeOrObjectProperty.compute(quad);
		}
	}

}
