/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.consistency;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.Quad;

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
		
		//	Total # IFP Statements : 4; # Violated Predicate-Object Statements : 1
		//  In our minimal example only the predicate object pair foaf:jabberID "I_AM_NOT_A_VALID_IFP" 
		//  was violated with :bob, :alice and :jack resources having the same jabberID
		
		// 1 - (3 / 6)
		assertEquals(0.5,metric.metricValue(), 0.0001);
		
		Model m = ((Model)metric.getQualityProblems().getProblemList().get(0));
		m.write(System.out, "TURTLE");
		
	}	
	
	
}
