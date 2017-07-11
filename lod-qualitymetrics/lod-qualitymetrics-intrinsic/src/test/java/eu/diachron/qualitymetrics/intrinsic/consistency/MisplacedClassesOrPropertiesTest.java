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
import eu.diachron.qualitymetrics.utilities.TestLoader;

/**
 * @author Jeremy Debattista
 * 
 */
public class MisplacedClassesOrPropertiesTest extends Assert{
	
	protected TestLoader loader = new TestLoader();
	protected MisplacedClassesOrProperties metric = new MisplacedClassesOrProperties();
	
	@Before
	public void setUp() throws Exception {
		loader.loadDataSet("testdumps/Sample_MisplacedClassesAndProperties.ttl");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testEntitiesAsMembersOfDisjointClassesMinimalExample() {
		List<Quad> streamingQuads = loader.getStreamingQuads();
		
		for(Quad quad : streamingQuads){
			// here we start streaming triples to the quality metric
			metric.compute(quad);
		}
		
		// 1- (2 / 6)
		assertEquals(0.66666666666,metric.metricValue(), 0.0001);
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
//		plModel.write(System.out, "TURTLE");
//	}


}
