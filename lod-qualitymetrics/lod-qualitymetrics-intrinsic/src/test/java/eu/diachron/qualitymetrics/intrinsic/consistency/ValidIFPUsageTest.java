/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.consistency;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import eu.diachron.qualitymetrics.utilities.TestLoader;

/**
 * @author Jeremy Debattista
 * 
 */
public class ValidIFPUsageTest extends Assert {
	protected TestLoader loader = new TestLoader();
	protected ValidIFPUsage metric = new ValidIFPUsage();


	@Before
	public void setUp() throws Exception {
		EnvironmentProperties.getInstance().setDatasetURI("http://www.example.org");
		loader.loadDataSet("testdumps/SampleInput_ValidIFPUsage_Minimal.ttl");
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testValidIFPUsageMinimalExample() {
		List<Quad> streamingQuads = loader.getStreamingQuads();
		
		for(Quad quad : streamingQuads){
			// here we start streaming triples to the quality metric
			metric.compute(quad);
		}
		
		//	Total # IFP Statements : 6; # Violated Predicate-Object Statements : 2
		//  In principle, there are 3 triples with the same value for the IFP, but only 2 of them
		//  are violating the IFP.
		
		//  In our minimal example only the predicate object pair foaf:jabberID "I_AM_NOT_A_VALID_IFP" 
		//  was violated with :bob, :alice and :jack resources having the same jabberID
		
		// 1 - (2 / 6)
		assertEquals(0.666667,metric.metricValue(), 0.0001);
		
	}	
	

//	@Ignore
//	@Test
//	public void problemReportTest(){
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
//		
//		plModel.write(System.out, "TURTLE");
//	}
	
}
