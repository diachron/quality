/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.consistency;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import eu.diachron.qualitymetrics.utilities.TestLoader;

/**
 * @author Jeremy Debattista
 * 
 */
public class UsageOfIncorrectDomainOrRangeDatatypesTest extends Assert {
	protected TestLoader loader = new TestLoader();
	protected UsageOfIncorrectDomainOrRangeDatatypes metric = new UsageOfIncorrectDomainOrRangeDatatypes();


	@Before
	public void setUp() throws Exception {
		EnvironmentProperties.getInstance().setDatasetURI("http://www.example.org");
		loader.loadDataSet("testdumps/SampleInput_UsageOfIncorrectDomainOrRangeDatatypes_Minimal.ttl");
//		loader.loadDataSet("/Users/jeremy/Dropbox/Public/knud/social.mercedes-benz.com.full.ttl");
	}

	@After
	public void tearDown() throws Exception {
	}
	
//	@Ignore
	@Test
	public void testUsageOfIncorrectClassesAndPropertiesMinimalExample() {
		List<Quad> streamingQuads = loader.getStreamingQuads();
		
		for(Quad quad : streamingQuads){
			// here we start streaming triples to the quality metric
			metric.compute(quad);
		}
//		 # Incorrect Domains : 1; # Incorrect Ranges : 1; # Predicates Assessed : 4; # Undereferenceable Predicate : 0
		
		// 2 / 8
		assertEquals(0.75,metric.metricValue(), 0.0001);
	}	

	@Ignore
	@Test
	public void problemReportTest() throws IOException{
		for(Quad q : loader.getStreamingQuads()){
			metric.compute(q);
		}
		metric.metricValue();
		
		ProblemList<?> pl = metric.getQualityProblems();
		QualityReport qr = new QualityReport();
		String plModelURI = qr.createQualityProblem(metric.getMetricURI(), pl);
		Model plModel = qr.getProblemReportFromTBD(plModelURI);
		
		plModel.write(new FileWriter(new File("/Users/jeremy/Desktop/pr.ttl")), "TURTLE");
	}
	
	
}
