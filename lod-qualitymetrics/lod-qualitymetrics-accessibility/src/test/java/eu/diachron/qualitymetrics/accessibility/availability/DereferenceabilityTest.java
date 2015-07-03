package eu.diachron.qualitymetrics.accessibility.availability;

import java.util.List;

import de.unibonn.iai.eis.diachron.configuration.DataSetMappingForTestCase;
import eu.diachron.qualitymetrics.accessibility.availability.Dereferenceability;
import eu.diachron.qualitymetrics.utilities.TestLoader;

import org.apache.jena.riot.lang.PipedRDFIterator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.Quad;

public class DereferenceabilityTest extends Assert {
	
	private static Logger logger = LoggerFactory.getLogger(DereferenceabilityTest.class);
	
	protected TestLoader loader = new TestLoader();
	protected EstimatedDereferenceability metric = new EstimatedDereferenceability();

	PipedRDFIterator<Triple> iter;
	@Before
	public void setUp() throws Exception {
//		loader.loadDataSet(DataSetMappingForTestCase.Dereferenceability);
		//loader.loadDataSet("http://transparency.270a.info/dataset/corruption-perceptions-index.ttl");
	}

	@After
	public void tearDown() throws Exception {
		// No clean-up required
	}

	@Test
	public void testDereferenceability() {
		// Load quads...
		long countLoadedQuads = 0;
		
		iter = (PipedRDFIterator<Triple>) loader.streamParser("/Volumes/Green-TeaExternal/datasets/europeana-merged-sorted.nt.gz");

		
		while (iter.hasNext()){
			Quad q = new Quad(null, iter.next());
			metric.compute(q);
			countLoadedQuads++;
		}
		
		logger.trace("Quads loaded, {} quads", countLoadedQuads);
		
		// The dataset corresponding to the test case has: 19 URIs, of which 6 are dereferenceable and abide by the rules,
		// 6 are dereferenceable but do not abide with the rules and 4 are non-dereferenceable (yield HTTP 404). 
		// Thus the expected value is: 6/23 = 0.26087
		// Only URIs that return HTTP 200 OK, are deemed as hash URIs and return a valid content-type are classified as dereferenceable.
		// Refer to the test resource for additional details.
		double expectedValue = 0.26087;
		double delta = 0.001;
		
		// Obtain the measurement of Dereferenceability for the source of the dataset
		double metricValue = metric.metricValue();
		logger.trace("Computed dereferenceability metric: " + metricValue);

		assertEquals(expectedValue, metricValue, delta);
	}

}
