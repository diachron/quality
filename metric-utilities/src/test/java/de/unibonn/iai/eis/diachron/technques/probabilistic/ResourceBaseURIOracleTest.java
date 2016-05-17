/**
 * 
 */
package de.unibonn.iai.eis.diachron.technques.probabilistic;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.properties.PropertyManager;
import eu.diachron.qualitymetrics.utilities.TestLoader;

/**
 * @author Jeremy Debattista
 * 
 */
public class ResourceBaseURIOracleTest extends Assert {
	
	private ResourceBaseURIOracle oracle = new ResourceBaseURIOracle();
	
	@Test
	public void extractPayLevelDomainURITest(){
		
		String test1 = ResourceBaseURIOracle.extractPayLevelDomainURI("https://www.google.com");
		assertTrue("google.com".equals(test1));
		
		String test2 = ResourceBaseURIOracle.extractPayLevelDomainURI("http://dws.informatik.uni-mannheim.de/en/projects/current-projects/#c13686");
		assertTrue("uni-mannheim.de".equals(test2));
		
		String test3 = ResourceBaseURIOracle.extractPayLevelDomainURI("http://opendatacommunities.org/data/transparency/input-indicators/affordable-rent-per-dwelling");
		assertTrue("opendatacommunities.org".equals(test3));
		
		String test4 = ResourceBaseURIOracle.extractPayLevelDomainURI("http://esd-toolkit.eu");
		assertTrue("esd-toolkit.eu".equals(test4));
		
		String test5 = ResourceBaseURIOracle.extractPayLevelDomainURI("http://oecd.270a.info");
		assertFalse("oecd.270a.info".equals(test5));
	}
	
	@Test
	public void estimateDatasetURI(){
		PropertyManager.getInstance().addToEnvironmentVars("baseURI", "http://oecd.270a.info");
		TestLoader l = new TestLoader();
		l.loadDataSet("void.ttl");
		l.loadDataSet("WSECTOR.ttl");
		List<Quad> quads = l.getStreamingQuads();
		
		for (Quad q : quads){
			oracle.addHint(q);
		}
		// Test with declared dataset URI
		assertTrue("http://oecd.270a.info/dataset/WSECTOR".equals(oracle.getEstimatedResourceDatasetURI()));
		
		try {
			// Manually unset the declared dataset URI, in order to test the dataset URI guessing mechanism 
			Field declDatasetField = oracle.getClass().getDeclaredField("declaredResDatasetURI");
			declDatasetField.setAccessible(true);
			declDatasetField.set(oracle, null);
			// Test the result of estimating the dataset URI when it has not been explicitly declared in the resource
			// assertTrue("http://oecd.270a.info/dataset/WSECTOR".equals(oracle.getEstimatedResourceDatasetURI()));
		} catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
}
