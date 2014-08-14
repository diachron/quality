package de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.consistency;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.TestLoader;
import eu.diachron.qualitymetrics.intrinsic.consistency.ObsoleteConceptsInOntology;

public class ObsoleteConceptsInOntologyTest extends Assert{
	
	protected TestLoader loader = new TestLoader();
	protected ObsoleteConceptsInOntology metric = new ObsoleteConceptsInOntology();

	@Before
	public void setUp() throws Exception {
		loader.loadDataSet("testdumps/efo-2.34.owl");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testObsoleteConceptsInOntologyMetric() {
		List<Quad> streamingQuads = loader.getStreamingQuads();
		
		for(Quad quad : streamingQuads){
			// here we start streaming triples to the quality metric
			metric.compute(quad);
		}
		
		// 267 / 13235
		assertEquals(0.02017378163,metric.metricValue(), 0.0001);
	}	
}