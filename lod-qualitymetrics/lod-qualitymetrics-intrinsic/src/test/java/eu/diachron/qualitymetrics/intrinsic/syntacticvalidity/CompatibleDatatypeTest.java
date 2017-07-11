/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.syntacticvalidity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.qualitymetrics.utilities.TestLoader;

/**
 * @author Jeremy Debattista
 * 
 */
public class CompatibleDatatypeTest extends Assert {

	protected TestLoader loader = new TestLoader();
	protected CompatibleDatatype metric = new CompatibleDatatype();

	
	@Before
	public void setUp() throws Exception {
		loader.loadDataSet("testdumps/SampleInput_CompatibleDatatype.ttl");
//		loader.loadDataSet("/Users/jeremy/Desktop/www.bibsonomy.org.nt.gz");

	}
	
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * a minimal example for entities being members of disjoint classes: one entity is (of foaf:Person and foaf:Document), one isn't.
	 * 
	 * Note that the FOAF vocabulary has been published as LOD, and that foaf:Person is explicitly declared disjoint with foaf:Document.
	 */
	@Ignore
	@Test
	public void testCompatibleDatatypeMinimalExample() {
		List<Quad> streamingQuads = loader.getStreamingQuads();
		
		for(Quad quad : streamingQuads){
			// here we start streaming triples to the quality metric
			metric.compute(quad);
		}
		
		// incorrect 3
		// correct 11
		// 11 / 14
		assertEquals(0.7857,metric.metricValue(), 0.0001);
	}	
	

//	@Test
//	public void problemReportTest() throws IOException{
//		for(Quad q : loader.getStreamingQuads()){
//			metric.compute(q);
//		}
//		metric.metricValue();
//		
//		ProblemList<?> pl = metric.getQualityProblems();
//		QualityReport qr = new QualityReport();
//		String plModelURI = qr.createQualityProblem(metric.getMetricURI(), pl);
//		Model plModel = qr.getProblemReportFromTBD(plModelURI);
//		
////		plModel.write(new FileWriter(new File("/Users/jeremy/Desktop/pr.ttl")), "TURTLE");
//		plModel.write(System.out, "TURTLE");
//
//	}
}
