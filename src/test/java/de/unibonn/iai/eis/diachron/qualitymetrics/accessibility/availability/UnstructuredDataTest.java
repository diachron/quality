package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.graph.Triple;

import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.TestLoader;

public class UnstructuredDataTest extends Assert{

	protected TestLoader loader = new TestLoader();
	protected UnstructuredData metric = new UnstructuredData();

	@Before
	public void setUp() throws Exception {
		loader.loadDataSet();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testUnstructuredData() {
		List<Triple> streamedTriples = loader.getStreamingTriples();
		
		for(Triple triple : streamedTriples){
			
			//Here we start streaming triples to the quality metric
			metric.compute(triple);
		}
		
		assertEquals(1.0,metric.metricValue(), 0.0);
	}
}
