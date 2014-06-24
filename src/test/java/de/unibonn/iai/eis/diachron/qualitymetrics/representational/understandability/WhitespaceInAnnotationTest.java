package de.unibonn.iai.eis.diachron.qualitymetrics.representational.understandability;

import static org.junit.Assert.*;

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
 * Test class for {@link de.unibonn.iai.eis.diachron.qualitymetrics.representational.understandability.WhitespaceInAnnotation(com.hp.hpl.jena.sparql.core.Quad)}.
 * 
 * @author Muhammad Ali Qasmi
 * @date 19th June 2014
 */
public class WhitespaceInAnnotationTest extends Assert{
        
        /**
         * static logger object
         */
        static Logger logger = Logger.getLogger(WhitespaceInAnnotationTest.class);
        /**
         * Test data set loader object
         */
        protected TestLoader loader = new TestLoader();
        /**
         * Metric under test object
         */
        protected WhitespaceInAnnotation whitespaceInAnnotation = new WhitespaceInAnnotation();

        @Before
        public void setUp() throws Exception {
                BasicConfigurator.configure();
                loader.loadDataSet(DataSetMappingForTestCase.WhitespaceInAnnotation);
                WhitespaceInAnnotation.loadAnnotationPropertiesSet(null);
        }

        @After
        public void tearDown() throws Exception {
                WhitespaceInAnnotation.clearAnnotationPropertiesSet();
        }
        
        /**
         * Test method for {@link de.unibonn.iai.eis.diachron.qualitymetrics..representational.understandability.WhitespaceInAnnotation#compute(com.hp.hpl.jena.sparql.core.Quad)}.
         * 
         * Total number of white space literals : 3
         * Total total number of annotation literals : 4
         * 
         * Metric Value = 3 / 4 = 0.750000
         * 
         */
        @Test
        public final void testCompute() {
                List<Quad> streamingQuads = loader.getStreamingQuads();
                for(Quad quad : streamingQuads){
                        whitespaceInAnnotation.compute(quad);
                }
                assertEquals(0.750000, whitespaceInAnnotation.metricValue(), 0.00001);
        }

}
