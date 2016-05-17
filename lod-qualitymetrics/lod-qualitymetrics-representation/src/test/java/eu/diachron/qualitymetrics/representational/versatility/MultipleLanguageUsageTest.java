/**
 * 
 */
package eu.diachron.qualitymetrics.representational.versatility;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Quad;

import eu.diachron.qualitymetrics.utilities.TestLoader;

/**
 * @author Jeremy Debattista
 * 
 * Test for the Multiple Language Usage Metric
 * 
 */
public class MultipleLanguageUsageTest  extends Assert {

	TestLoader loader = new TestLoader();
	MultipleLanguageUsage metric = new MultipleLanguageUsage();
	
	@Before
	public void setUp(){
		loader.loadDataSet("testdumps/language.ttl");
	}
	
	@Test
	public void noBlankNodesTest(){
		for(Quad q : loader.getStreamingQuads()){
			metric.compute(q);
		}
		
		assertEquals(5.0, metric.metricValue(), 0.00001);
	}
	
	
//	@Test
//	public void problemReportTest(){
//		for(Quad q : loader.getStreamingQuads()){
//			metric.compute(q);
//		}
//		
//		metric.metricValue();
//		
//		ProblemList<?> pl = metric.getQualityProblems();
//		QualityReport qr = new QualityReport();
//		String plModelURI = qr.createQualityProblem(metric.getMetricURI(), pl);
//		Model plModel = qr.getProblemReportFromTBD(plModelURI);
//		
//		plModel.write(System.out, "TURTLE");
//	}
}
