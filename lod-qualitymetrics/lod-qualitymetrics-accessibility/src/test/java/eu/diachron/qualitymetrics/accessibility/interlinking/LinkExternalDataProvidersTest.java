package eu.diachron.qualitymetrics.accessibility.interlinking;


import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.apache.jena.riot.lang.PipedTriplesStream;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.datatypes.Object2Quad;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import eu.diachron.qualitymetrics.utilities.TestLoader;

public class LinkExternalDataProvidersTest extends Assert {
	
	private static Logger logger = LoggerFactory.getLogger(LinkExternalDataProvidersTest.class);
	
	protected TestLoader loader = new TestLoader();
	protected EstimatedLinkExternalDataProviders metric;

	@Before
	public void setUp() throws Exception {
//		loader.loadDataSet(DataSetMappingForTestCase.Dereferenceability);
//		loader.loadDataSet("/Users/jeremy/Desktop/data.dcs.shef.ac.uk.nt.gz");
	}

	@After
	public void tearDown() throws Exception {
		// No clean-up required
	}
	
	
	
	@Test
	public void testExternalDataProviders() {
		metric = new EstimatedLinkExternalDataProviders();
		metric.setDatasetURI("http://www.myexperiment.org");

		 PipedRDFIterator<?> iter =loader.streamParser("/Users/jeremy/Desktop/www.myexperiment.org.nt.gz");
		// Load quads...
//		List<Quad> streamingQuads = loader.getStreamingQuads();
//		int counter = 0;
		
//		for(Quad quad : streamingQuads){
//			metric.compute(quad);
//			counter++;
//		}
//		System.out.println(counter);
		
		while(iter.hasNext()){
			Object nxt = iter.next();
			Object2Quad quad = new Object2Quad(nxt);
			metric.compute(quad.getStatement());
		}
//		
		assertEquals(0.052,metric.metricValue(),0.001);
	}
	
//	private int[] resSize = new int[]{50,70,90,100,120,140,160,180,200,220,240,280,300,320,340,360,380,400,450,470};

	/*
	
	protected PipedRDFIterator<?> iterator;
	protected PipedRDFStream<?> rdfStream;

	private ExecutorService executor;
	
	@Test
	public void testExternalDataProviders() {
		// Load quads...
		for (int i : resSize){
			this.iterator = new PipedRDFIterator<Triple>();
			this.rdfStream = new PipedTriplesStream((PipedRDFIterator<Triple>) iterator);
			
			this.executor = Executors.newSingleThreadExecutor();

			
			Runnable parser = new Runnable() {
				public void run() {
					try{
						RDFDataMgr.parse(rdfStream, "/Users/jeremy/Downloads/swetodblp_april_2008.rdf");
					} catch (Exception e) {
						logger.error("Error parsing dataset {}. Error message {}", "/Users/jeremy/Downloads/swetodblp_april_2008.rdf", e.getMessage());
					}
				}
			};
			
			executor.submit(parser);
		
//		List<Quad> streamingQuads = loader.getStreamingQuads();
		
		

			metric = new EstimatedLinkExternalDataProviders();
			EstimatedLinkExternalDataProviders.setReservoirSize(i);

			int countLoadedQuads = 0;

			long start = System.currentTimeMillis();
			//for(Quad quad : streamingQuads){
			while (this.iterator.hasNext()) {
				// Here we start streaming triples to the quality metric
				Quad q = new Quad(null, (Triple) this.iterator.next());
				metric.compute(q);
				countLoadedQuads++;
			}
			logger.trace("Quads loaded, {} quads", countLoadedQuads);
			
			double metricValue = metric.metricValue();
			long now = System.currentTimeMillis();
			double sec = ((double)now - (double)start) / 1000.0;
			
			System.out.println(i + " - " + sec + " - " + metricValue);
			
		}
	}
*/
}
