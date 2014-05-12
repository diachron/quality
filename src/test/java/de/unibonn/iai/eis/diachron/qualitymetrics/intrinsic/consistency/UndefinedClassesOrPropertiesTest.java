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
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.VocabularyReader;

/**
 * 
 * @author Muhammad Ali Qasmi
 * @date 11th March 2014
 */
public class UndefinedClassesOrPropertiesTest extends Assert {

	static Logger logger = Logger.getLogger(UndefinedClassesOrPropertiesTest.class);
	
	protected TestLoader loader = new TestLoader();
	protected UndefinedClassesOrProperties undefinedClassesOrProperties = new UndefinedClassesOrProperties(); 
	@Before
	public void setUp() throws Exception {
		BasicConfigurator.configure();
		loader.loadDataSet(DataSetMappingForTestCase.UndefinedClassesOrProperties);
	}

	@After
	public void tearDown() throws Exception {
		VocabularyReader.clear();
	}
	
	/**
	 * 
	 */
	@Test
	public final void testCompute() {
		List<Quad> streamingQuads = loader.getStreamingQuads();
		for(Quad quad : streamingQuads){
			undefinedClassesOrProperties.compute(quad);
		}
		assertEquals(0.076923076, undefinedClassesOrProperties.metricValue(), 0.00001);
	}

}
