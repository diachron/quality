package de.unibonn.iai.eis.diachron.qualitymetrics.reputation;

import static org.junit.Assert.*;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
         */
        @Test
        public final void testCompute() {
                fail("Not yet implemented"); // TODO
        }

}
