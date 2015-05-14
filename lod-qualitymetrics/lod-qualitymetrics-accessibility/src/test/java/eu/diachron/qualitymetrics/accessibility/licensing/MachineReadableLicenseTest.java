/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.licensing;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.properties.PropertyManager;
import eu.diachron.qualitymetrics.utilities.TestLoader;

/**
 * @author Jeremy Debattista
 * 
 */
public class MachineReadableLicenseTest extends Assert {
	
	MachineReadableLicense mrl; 
	
	TestLoader l = new TestLoader();
	

	@Before
	public void setUp(){
		l.loadDataSet("testdumps/void.ttl");
		l.loadDataSet("testdumps/WSECTOR.ttl");
		
		PropertyManager.getInstance().addToEnvironmentVars("baseURI", "http://oecd.270a.info");
		PropertyManager.getInstance().addToEnvironmentVars("datasetURI", "http://oecd.270a.info/dataset/WSECTOR");
		mrl = new MachineReadableLicense();
	}
	
	
	@Test
	public void test(){
		List<Quad> quads = l.getStreamingQuads();
		
		for (Quad q : quads){
			mrl.compute(q);
		}
		
		assertEquals(0.66667, mrl.metricValue(), 0.00001);
	}
	

}
