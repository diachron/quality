package eu.diachron.qualitymetrics.accessibility.availability;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.annotations.QualityReport;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.qualitymetrics.utilities.TestLoader;

public class DereferenceabilityTest extends Assert {
	
	
	protected TestLoader loader = new TestLoader();
	protected Dereferenceability metric = new Dereferenceability();

	@Before
	public void setUp() throws Exception {
//		loader.loadDataSet("testdumps/sample_deref.ttl");
		loader.loadDataSet("/Users/jeremy/Desktop/zbw.eu_stw.nt.gz");
	}
	
	@Test
	public void testDereferenceability() {
		// Load quads...
		List<Quad> streamingQuads = loader.getStreamingQuads();
		int counter = 0;
		
		for(Quad quad : streamingQuads){
			metric.compute(quad);
			counter++;
		}
		System.out.println(counter);
		
		Double d = metric.metricValue();
		
		
		// Problem report
		ProblemList<?> pl = metric.getQualityProblems();
		QualityReport qr = new QualityReport();
		String plModelURI = qr.createQualityProblem(metric.getMetricURI(), pl);
		Model plModel = qr.getProblemReportFromTBD(plModelURI);
		
		try {
			plModel.write(new FileOutputStream(new File("/Users/jeremy/Desktop/preport_test.ttl")), "TURTLE");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
		assertEquals(0.052,d,0.001);

	}

}
