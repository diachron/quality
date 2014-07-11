package de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.consistency;

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
import com.hp.hpl.jena.vocabulary.RDFS;

import de.unibonn.iai.eis.diachron.configuration.DataSetMappingForTestCase;
import de.unibonn.iai.eis.diachron.configuration.OutputFileMappingForQualityProblems;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.TestLoader;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.VocabularyReader;

/**
 * Test class for {@link de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.consistency.UndefinedClasses#compute(com.hp.hpl.jena.sparql.core.Quad)}.
 * 
 * @author Muhammad Ali Qasmi
 * @date 11th March 2014
 */
public class UndefinedClassesOrPropertiesTest extends Assert {

	static Logger logger = Logger.getLogger(UndefinedClassesOrPropertiesTest.class);
	
	protected TestLoader loader = new TestLoader();
	protected UndefinedClasses undefinedClasses = new UndefinedClasses(); 
	protected UndefinedProperties undefinedProperties = new UndefinedProperties(); 
	@Before
	public void setUp() throws Exception {
		BasicConfigurator.configure();
		loader.loadDataSet(DataSetMappingForTestCase.UndefinedClassesOrProperties);
	}

	@After
	public void tearDown() throws Exception {
		VocabularyReader.clear();
	}
	
	@Test
	/**
	 * Test method for {@link de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.consistency.UndefinedClassesOrProperties#compute(com.hp.hpl.jena.sparql.core.Quad)}.
	 * 
	 * Number of Undefined Classes = 1
     * Number of Classes = 3
     * Number of Undefined Properties = 1
     * Number of Properties = 6
     * Metric Value = 2 /9 = 0.2222222222222222
	 */
	public final void testCompute() {
		List<Quad> streamingQuads = loader.getStreamingQuads();
		for(Quad quad : streamingQuads){
			undefinedClasses.compute(quad);
			undefinedProperties.compute(quad);
		}
		assertEquals(0.3333333, undefinedClasses.metricValue(), 0.00001);
		assertEquals(0.1666666, undefinedProperties.metricValue(), 0.00001);
	}
	
	@Test
	public final void test() {
		System.out.println("UndefinedClassesOrPropertiesTest.test() "+ RDFS.range.toString());
	}
    /**
     * Test method for {@link de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.consistency.UndefinedClassesOrProperties#compute(com.hp.hpl.jena.sparql.core.Quad)}.
     */
	/*
	@Test
	public final void testOutProblematicInstancesToStream() {
	        try {
	                
	            List<Quad> streamingQuads = loader.getStreamingQuads();
	            for(Quad quad : streamingQuads){
	                    undefinedClassesOrProperties.compute(quad);
	            }
	                
	            OutputStream tmpStream = null;
	            tmpStream = new FileOutputStream(OutputFileMappingForQualityProblems.UndefinedClassesOrProperties);
	            undefinedClassesOrProperties.outProblematicInstancesToStream(DataSetMappingForTestCase.UndefinedClassesOrProperties,tmpStream);
	            tmpStream.close();
	        } catch (FileNotFoundException e) {
	                e.printStackTrace();
	        } catch (IOException e) {
	                e.printStackTrace();
	        }
	    }
	 */   
}
