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
 * Test class for {@link de.unibonn.iai.eis.diachron.qualitymetrics.representational.understandability.LabelsUsingCapitals(com.hp.hpl.jena.sparql.core.Quad)}.
 * 
 * @author Muhammad Ali Qasmi
 * @date 19th June 2014
 */
public class LabelsUsingCapitalsTest extends Assert{
        /**
         * static logger object
         */
        static Logger logger = Logger.getLogger(LabelsUsingCapitalsTest.class);
        /**
         * Test data set loader object
         */
        protected TestLoader loader = new TestLoader();
        /**
         * Metric under test object
         */
        protected LabelsUsingCapitals labelsUsingCapitals  = new LabelsUsingCapitals();

        @Before
        public void setUp() throws Exception {
                BasicConfigurator.configure();
                loader.loadDataSet(DataSetMappingForTestCase.LabelsUsingCapitals);
                LabelsUsingCapitals.loadAnnotationPropertiesSet(null);
        }

        @After
        public void tearDown() throws Exception {
                LabelsUsingCapitals.clearAnnotationPropertiesSet();
        }

        /**
         * Test method for {@link de.unibonn.iai.eis.diachron.qualitymetrics.representational.understandability.LabelsUsingCapitals#compute(com.hp.hpl.jena.sparql.core.Quad)}.
         * 
         * Case1 => rdfs:label "GreenGoblin"
         *   
         * Total number of empty literals : 1
         * Total total number of literals : 4
         * 
         * Metric Value = 1 / 4 = 0.250000
         * 
         */
        @Test
        public final void testCompute() {
                List<Quad> streamingQuads = loader.getStreamingQuads();
                for(Quad quad : streamingQuads){
                        labelsUsingCapitals.compute(quad);
                }
                assertEquals(0.250000, labelsUsingCapitals.metricValue(), 0.00001);
        }

}
