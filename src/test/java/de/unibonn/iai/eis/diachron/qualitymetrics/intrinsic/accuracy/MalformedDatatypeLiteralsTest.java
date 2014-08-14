/**
 * 
 */
package de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.accuracy;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.configuration.DataSetMappingForTestCase;
import de.unibonn.iai.eis.diachron.configuration.OutputFileMappingForQualityProblems;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.TestLoader;
import eu.diachron.qualitymetrics.intrinsic.accuracy.MalformedDatatypeLiterals;

/**
 * Test class for {@link eu.diachron.qualitymetrics.intrinsic.accuracy.MalformedDatatypeLiterals#compute(com.hp.hpl.jena.sparql.core.Quad)}. 
 * 
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
	 * Test method for {@link eu.diachron.qualitymetrics.intrinsic.accuracy.MalformedDatatypeLiterals#compute(com.hp.hpl.jena.sparql.core.Quad)}.
	 */
	@Test
	public final void testCompute() {
	    List<Quad> streamingQuads = loader.getStreamingQuads();
		for(Quad quad : streamingQuads){
			malformedDatatypeLiterals.compute(quad);
		}
		assertEquals(0.166666,malformedDatatypeLiterals.metricValue(), 0.00001);
	}
	
	/**
	 * Test method for {@link de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.accuracy.MalformedDatatypeLiterals#compute(com.hp.hpl.jena.sparql.core.Quad)}.
	 */
	/*
	@Test
	public final void testOutProblematicInstancesToStream() {
        try {
                
            List<Quad> streamingQuads = loader.getStreamingQuads();
            for(Quad quad : streamingQuads){
                malformedDatatypeLiterals.compute(quad);
            }
                
            OutputStream tmpStream = null;
            tmpStream = new FileOutputStream(OutputFileMappingForQualityProblems.MalformedDatatypeLiterals);
            malformedDatatypeLiterals.outProblematicInstancesToStream(DataSetMappingForTestCase.MalformedDatatypeLiterals,tmpStream);
            tmpStream.close();
        } catch (FileNotFoundException e) {
                e.printStackTrace();
        } catch (IOException e) {
                e.printStackTrace();
        }
	}
	*/
}
