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


public class OntologyHijackingTest extends Assert{

        static Logger logger = Logger.getLogger(OntologyHijackingTest.class);
        
        protected TestLoader loader = new TestLoader();
        protected OntologyHijacking ontologyHijacking = new OntologyHijacking();
        
        @Before
        public void setUp() throws Exception {
                BasicConfigurator.configure();
                loader.loadDataSet(DataSetMappingForTestCase.OntologyHijacking);
        }

        @After
        public void tearDown() throws Exception {
        }

        @Test
        public final void test() {
                List<Quad> streamingQuads = loader.getStreamingQuads();
                for(Quad quad : streamingQuads){
                        ontologyHijacking.compute(quad);
                }
                //assertEquals(0.222222, ontologyHijacking.metricValue(), 0.00001);
        }

}
