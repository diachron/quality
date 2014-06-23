package de.unibonn.iai.eis.diachron.qualitymetrics.reputation;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.configuration.DataSetMappingForTestCase;
import de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.consistency.OntologyHijackingTest;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.TestLoader;

/**
 * Test class for {@link de.unibonn.iai.eis.diachron.qualitymetrics.reputation.OBOFoundry(com.hp.hpl.jena.sparql.core.Quad)}.
 * 
 * @author Muhammad Ali Qasmi
 * @date 12th June 2014
 */
public class OBOFoundryTest {
        
        static Logger logger = Logger.getLogger(OBOFoundryTest.class);
        protected TestLoader loader = new TestLoader();
        protected OBOFoundry oBOFoundry = new OBOFoundry();
        
        @Before
        public void setUp() throws Exception {
                BasicConfigurator.configure();
                loader.loadDataSet(DataSetMappingForTestCase.OBOFoundry);
        }

        @After
        public void tearDown() throws Exception {
        }
        
        /**
         * Test method for {@link de.unibonn.iai.eis.diachron.qualitymetrics.reputation.OBOFoundry#compute(com.hp.hpl.jena.sparql.core.Quad)}.
         * 
         * Total not Reputable Resources : 358815
         * Total Resources : 438133
         * 
         * metric Value = 358815 / 438133 = 0.818963
         */
        @Test
        public final void testCompute() {
                List<Quad> streamingQuads = loader.getStreamingQuads();
                for(Quad quad : streamingQuads){
                        oBOFoundry.compute(quad);
                }
                assertEquals(0.818963, oBOFoundry.metricValue(), 0.00001);
        }

}
