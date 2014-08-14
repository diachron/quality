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
import eu.diachron.qualitymetrics.intrinsic.consistency.OntologyHijacking;

/**
 * Test class for {@link de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.consistency.OntologyHijacking(com.hp.hpl.jena.sparql.core.Quad)}.
 * 
 * @author Muhammad Ali Qasmi
 * @date 10th June 2014
 *
 */
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

        /**
         * Test method for {@link eu.diachron.qualitymetrics.intrinsic.consistency.OntologyHijacking#compute(com.hp.hpl.jena.sparql.core.Quad)}.
         * 
         * Test Case explanation
         * 
         * Hijacked Ontology Case 1 => rdf:type a rdfs:Class; rdfs:subClassOf <#Person> .
         * Hijacked Ontology Case 2 => <#sister> rdfs:domain rdfs:Class ; rdfs:range <#Woman> .
         * 
         * Total locally defined classes or properties = 7
         * Total Hijacked classes or properties = 2
         * 
         * Metric Value = 2 / 7 = 0.285714 
         */
        @Test
        public final void testCompute() {
                List<Quad> streamingQuads = loader.getStreamingQuads();
                for(Quad quad : streamingQuads){
                        ontologyHijacking.compute(quad);
                }
                assertEquals(0.285714, ontologyHijacking.metricValue(), 0.00001);
        }

}
