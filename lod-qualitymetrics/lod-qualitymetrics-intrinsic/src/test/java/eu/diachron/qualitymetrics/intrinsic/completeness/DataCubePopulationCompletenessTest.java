/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.completeness;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.BeforeException;
import eu.diachron.qualitymetrics.utilities.TestLoader;

/**
 * @author Jeremy Debattista
 * 
 */
public class DataCubePopulationCompletenessTest extends Assert{
	
	protected TestLoader loader = new TestLoader();
	protected DataCubePopulationCompleteness metric = new DataCubePopulationCompleteness();

	@Before
	public void setUp() throws Exception {
		loader.loadDataSet("/Users/jeremy/Downloads/CompletenessMetric/maires.2014.ttl");
//		loader.loadDataSet("/Users/jeremy/Downloads/Archive/medailles.ttl");
		
	}
	
	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testDataCubePopulationCompleteness() throws BeforeException {
		metric.before();
		
		List<Quad> streamingQuads = loader.getStreamingQuads();
		
		for(Quad quad : streamingQuads){
			// here we start streaming triples to the quality metric
			metric.compute(quad);
		}
		
		//this.problemReportTest();
		System.out.println(metric.metricValue());

	}	
	
//	
//	private void problemReportTest(){
//		ProblemList<?> pl = metric.getQualityProblems();
//		QualityReport qr = new QualityReport();
//		String plModelURI = qr.createQualityProblem(metric.getMetricURI(), pl);
//		Model plModel = qr.getProblemReportFromTBD(plModelURI);
//		
//		try {
//			plModel.write(new FileOutputStream("/Users/jeremy/Desktop/report.ttl"), "TURTLE");
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//	}


}
