/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.licensing;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Quad;

import eu.diachron.qualitymetrics.utilities.TestLoader;

/**
 * @author Jeremy Debattista
 * 
 */
public class MachineReadableLicenseTest extends Assert {
	
	TestLoader l = new TestLoader();

	@Before
	public void setUp(){
		l.loadDataSet("testdumps/void.ttl");		
	}
	
	@Test
	public void testMachineReadableLicense(){
		MachineReadableLicense mrl = new MachineReadableLicense();
		mrl.setDatasetURI("http://oecd.270a.info/dataset/");

		List<Quad> quads = l.getStreamingQuads();
		
		for (Quad q : quads){
			mrl.compute(q);
		}
		
		assertEquals(0.0, mrl.metricValue(), 0.00001);
	}
	
	
	@Test
	public void testHumanReadableLicense(){
		HumanReadableLicense mrl = new HumanReadableLicense();
		mrl.setDatasetURI("http://oecd.270a.info/dataset/");

		List<Quad> quads = l.getStreamingQuads();
		
		for (Quad q : quads){
			mrl.compute(q);
		}
		
		assertEquals(1.0, mrl.metricValue(), 0.00001);
	}

}
