package eu.diachron.qualitymetrics.accessibility.interlinking;


import java.util.List;

import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import eu.diachron.qualitymetrics.utilities.TestLoader;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.core.Quad;

public class LinkExternalDataProvidersTest extends Assert {
	
	private static Logger logger = LoggerFactory.getLogger(LinkExternalDataProvidersTest.class);
	
	protected TestLoader loader = new TestLoader();
	protected LinkExternalDataProviders metric = new LinkExternalDataProviders();

	@Before
	public void setUp() throws Exception {
//		loader.loadDataSet(DataSetMappingForTestCase.Dereferenceability);
		EnvironmentProperties.getInstance().setDatasetURI("http://data.linkededucation.org/resource/lak/conference/");
		loader.loadDataSet("/Users/jeremy/Downloads/lak.nt");
	}

	@After
	public void tearDown() throws Exception {
		// No clean-up required
	}

	@Test
	public void testDereferenceability() {
		// Load quads...
		List<Quad> streamingQuads = loader.getStreamingQuads();
		int countLoadedQuads = 0;
		
		for(Quad quad : streamingQuads){
			// Here we start streaming triples to the quality metric
			metric.compute(quad);
			countLoadedQuads++;
		}
		logger.trace("Quads loaded, {} quads", countLoadedQuads);
		
		// Obtain the measurement of Dereferenceability for the source of the dataset
		double metricValue = metric.metricValue();
		System.out.println(metricValue);
	}

}
