/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.licensing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.jena.riot.lang.PipedRDFIterator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.annotations.QualityReport;
import de.unibonn.iai.eis.luzzu.datatypes.Object2Quad;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.qualitymetrics.utilities.TestLoader;

/**
 * @author Jeremy Debattista
 * 
 */
public class MachineReadableLicenseTest extends Assert {
	
	TestLoader l = new TestLoader();

	@Before
	public void setUp(){
//		l.loadDataSet("testdumps/void.ttl");
	}
	
	@Test
	public void testMachineReadableLicense(){
		HumanReadableLicense mrl = new HumanReadableLicense();
		mrl.setDatasetURI("http://lod.taxonconcept.org");

//		List<Quad> quads = l.getStreamingQuads();
		
//		for (Quad q : quads){
//			mrl.compute(q);
//		}
		
		PipedRDFIterator<?> iter = l.streamParser("/Users/jeremy/Desktop/lod.taxonconcept.org.nt.gz");
		while(iter.hasNext()){
			Object nxt = iter.next();
			Object2Quad quad = new Object2Quad(nxt);
			mrl.compute(quad.getStatement());
		}
		
//		assertEquals(0.0, mrl.metricValue(), 0.00001);
		System.out.println(mrl.metricValue());
		
		// Problem report
		ProblemList<?> pl = mrl.getQualityProblems();
		QualityReport qr = new QualityReport();
		String plModelURI = qr.createQualityProblem(mrl.getMetricURI(), pl);
		Model plModel = qr.getProblemReportFromTBD(plModelURI);
		
		try {
			plModel.write(new FileOutputStream(new File("/Users/jeremy/Desktop/preport_test.ttl")), "TURTLE");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	@Ignore
	@Test
	public void testHumanReadableLicense(){
		HumanReadableLicense mrl = new HumanReadableLicense();
		mrl.setDatasetURI("http://www.myexperiment.org");

		List<Quad> quads = l.getStreamingQuads();
		
		for (Quad q : quads){
			mrl.compute(q);
		}
		
		assertEquals(1.0, mrl.metricValue(), 0.00001);
	}

}
