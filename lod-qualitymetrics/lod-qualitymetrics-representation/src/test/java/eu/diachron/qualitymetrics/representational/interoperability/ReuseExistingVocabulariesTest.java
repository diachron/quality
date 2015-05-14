/**
 * 
 */
package eu.diachron.qualitymetrics.representational.interoperability;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.exceptions.BeforeException;
import de.unibonn.iai.eis.luzzu.properties.PropertyManager;
import eu.diachron.qualitymetrics.utilities.TestLoader;

/**
 * @author Jeremy Debattista
 * 
 * This tests the Reuse Existing Vocabularies metric.
 * The dataset has 4 suggested vocabularies (3 in config
 * 1 in LOV), but make use of only 3 of the suggested
 * vocabs.
 */
public class ReuseExistingVocabulariesTest extends Assert {

	TestLoader loader = new TestLoader();
	ReuseExistingVocabularies metric = new ReuseExistingVocabularies();
	
	@Before
	public void setUp(){
		loader.loadDataSet("testdumps/conf.rdf");
		
		PropertyManager.getInstance().addToEnvironmentVars("datasetURI", "http://colinda.org/resource/conference/");
		PropertyManager.getInstance().addToEnvironmentVars("baseURI", "http://colinda.org/");
		try {
			String filePath = this.getClass().getClassLoader().getResource("config.ttl").getFile();
			Object[] before = new Object[1];
			before[0] = filePath;
			metric.before(before);
		} catch (BeforeException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void reuseExistingVocabualries(){
		for(Quad q : loader.getStreamingQuads()){
			metric.compute(q);
		}
		
		assertEquals(0.75, metric.metricValue(), 0.0001);
	}
}
