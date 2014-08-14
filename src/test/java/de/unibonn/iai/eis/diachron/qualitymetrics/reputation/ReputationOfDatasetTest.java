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
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.TestLoader;
import eu.diachron.qualitymetrics.reputation.ReputationOfDataset;

/**
 * Test class for {@link de.unibonn.iai.eis.diachron.qualitymetrics.reputation.ReputationOfDataset(com.hp.hpl.jena.sparql.core.Quad)}.
 * 
 * @author Muhammad Ali Qasmi
 * @date 12th June 2014
 */
public class ReputationOfDatasetTest {
        
        static Logger logger = Logger.getLogger(ReputationOfDatasetTest.class);
        protected TestLoader loader = new TestLoader();
        protected ReputationOfDataset reputationOfDataset = new ReputationOfDataset(); 
        
        @Before
        public void setUp() throws Exception {
                BasicConfigurator.configure();
//                reputationOfDataset.loadReputableSources();
                loader.loadDataSet(DataSetMappingForTestCase.ReputationOfDataset);
        }

        @After
        public void tearDown() throws Exception {
        }
        
        /**
         * Test method for {@link eu.diachron.qualitymetrics.reputation.ReputationOfDataset#compute(com.hp.hpl.jena.sparql.core.Quad)}.
         * 
         * Total Resources [Count = 27]
         * Total Not Reputable Resources [Count = 20]
         * metric value = 20 / 27 = 0.74074074
         * 
         * List of Not Reputable Resources
         * ------------------------------------
         * http://helloWorld.org/#spiderman
         * http://example.org/entity/0.1/name
         * http://helloWorld.org/#spiderman
         * http://example.org/entity/0.1/name
         * http://helloWorld.org/#spiderman
         * http://example.org/entity/0.1/Person
         * http://helloWorld.org/#spiderman
         * http://www.perceive.net/schemas/relationship/enemyOf
         * http://helloWorld.org/#green-goblin
         * http://helloWorld.org/#John
         * http://helloWorld.org/#Person
         * http://helloWorld.org/#green-goblin
         * http://example.org/entity/0.1/name
         * http://helloWorld.org/#green-goblin
         * http://example.org/entity/0.1/Person
         * http://helloWorld.org/#green-goblin
         * http://www.perceive.net/schemas/relationship/enemyOf
         * http://helloWorld.org/#spiderman
         * http://helloWorld.org/#Person
         * http://helloWorld.org/#married
         */
        @Test
        public final void testCompute() {
                List<Quad> streamingQuads = loader.getStreamingQuads();
                for(Quad quad : streamingQuads){
                        reputationOfDataset.compute(quad);
                }
                assertEquals(0.74074074, reputationOfDataset.metricValue(), 0.00001);
        }

}
