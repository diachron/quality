package eu.diachron.qualitymetrics.intrinsic.consistency;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.annotations.QualityReport;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.qualitymetrics.utilities.TestLoader;

public class EntitiesAsMembersOfDisjointClassesTest extends Assert{
	
	protected TestLoader loader = new TestLoader();
	protected SimpleEntitiesAsMembersOfDisjointClasses metric = new SimpleEntitiesAsMembersOfDisjointClasses();

	@Before
	public void setUp() throws Exception {
		loader.loadDataSet("testdumps/SampleInput_EntitiesAsMembersOfDisjointClasses_Minimal.ttl");
//		loader.loadDataSet("/Users/jeremy/Downloads/restaurants.nt");
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * a minimal example for entities being members of disjoint classes: one entity is (of foaf:Person and foaf:Document), one isn't.
	 * 
	 * Note that the FOAF vocabulary has been published as LOD, and that foaf:Person is explicitly declared disjoint with foaf:Document.
	 */
	@Test
	public void testEntitiesAsMembersOfDisjointClassesMinimalExample() {
		List<Quad> streamingQuads = loader.getStreamingQuads();
		
		for(Quad quad : streamingQuads){
			// here we start streaming triples to the quality metric
			metric.compute(quad);
		}
		
		// 1 / 2
		assertEquals(0.5,metric.metricValue(), 0.0001);
	}	
	
	@Ignore
	@Test
	public void problemReportTest(){
		for(Quad q : loader.getStreamingQuads()){
			metric.compute(q);
		}
		metric.metricValue();
		
		ProblemList<?> pl = metric.getQualityProblems();
		QualityReport qr = new QualityReport();
//		Model plModel = qr.createQualityProblem(metric.getMetricURI(), pl);
		
//		plModel.write(System.out, "TURTLE");
	
	}
}