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
import de.unibonn.iai.eis.diachron.qualitymetrics.reputation.ReputationOfDatasetTest;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.TestLoader;

/**
 * Test class for {@link de.unibonn.iai.eis.diachron.qualitymetrics.representational.understandability.EmptyAnnotationValue(com.hp.hpl.jena.sparql.core.Quad)}.
 * 
 * @author Muhammad Ali Qasmi
 * @date 19th June 2014
 */
public class EmptyAnnotationValueTest extends Assert{
        
        /**
         * static logger object
         */
        static Logger logger = Logger.getLogger(EmptyAnnotationValueTest.class);
        /**
         * Test data set loader object
         */
        protected TestLoader loader = new TestLoader();
        /**
         * Metric under test object
         */
        protected EmptyAnnotationValue emptyAnnotationValue = new EmptyAnnotationValue();
        
        @Before
        public void setUp() throws Exception {
                BasicConfigurator.configure();
                loader.loadDataSet(DataSetMappingForTestCase.EmptyAnnotationValue);
        }

        @After
        public void tearDown() throws Exception {
        }
        
        /**
         * Test method for {@link de.unibonn.iai.eis.diachron.qualitymetrics..representational.understandability.EmptyAnnotationValue#compute(com.hp.hpl.jena.sparql.core.Quad)}.
         * 
         */
        @Test
        public final void testComputer() {
                List<Quad> streamingQuads = loader.getStreamingQuads();
                for(Quad quad : streamingQuads){
                        emptyAnnotationValue.compute(quad);
                }
                assertEquals(0.00000, emptyAnnotationValue.metricValue(), 0.00001);
        }

}
