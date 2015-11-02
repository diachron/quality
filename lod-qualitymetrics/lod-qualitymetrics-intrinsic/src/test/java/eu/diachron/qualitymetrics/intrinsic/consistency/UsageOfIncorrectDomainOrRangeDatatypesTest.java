/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.consistency;

import java.util.List;

import org.apache.jena.riot.lang.PipedRDFIterator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

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
	}

	@After
	public void tearDown() throws Exception {
	}
	
@Ignore
	@Test
	public void testUsageOfDeprecatedClassesAndPropertiesMinimalExample() {
		List<Quad> streamingQuads = loader.getStreamingQuads();
		
		for(Quad quad : streamingQuads){
			// here we start streaming triples to the quality metric
			metric.compute(quad);
		}
//		 # Incorrect Domains : 1; # Incorrect Ranges : 2; # Predicates Assessed : 4; # Undereferenceable Predicate : 0
		
		// 3 / 8
		assertEquals(0.625,metric.metricValue(), 0.0001);
	}	
	
	
}
