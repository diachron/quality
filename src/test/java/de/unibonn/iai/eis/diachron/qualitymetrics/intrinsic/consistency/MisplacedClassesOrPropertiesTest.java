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
 * testing...
 * @author Muhammad Ali Qasmi
 * @date 13th March 2014
 */
public class MisplacedClassesOrPropertiesTest extends Assert {

	static Logger logger = Logger.getLogger(MisplacedClassesOrPropertiesTest.class);
	
	protected TestLoader loader = new TestLoader();
	protected MisplacedClassesOrProperties misplacedClassesOrProperties = new MisplacedClassesOrProperties(); 
	
	@Before
	public void setUp() throws Exception {
		BasicConfigurator.configure();
		loader.loadDataSet(DataSetMappingForTestCase.MisplacedClassesOrProperties);
	}

	@After
	public void tearDown() throws Exception {
		VocabularyReader.clear();
	}

	@Test
	public final void testCompute() {
		logger.trace("testCompute() --Started--");
		List<Quad> streamingQuads = loader.getStreamingQuads();
		for(Quad quad : streamingQuads){
			misplacedClassesOrProperties.compute(quad);
		}
		logger.info("Metric Value ::" + misplacedClassesOrProperties.metricValue());
		assertEquals(0.15384615, misplacedClassesOrProperties.metricValue(), 0.00001);
		logger.trace("testCompute() --Ended--");
	}

}
