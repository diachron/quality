/**
 * 
 */
package eu.diachron.qualitymetrics.representational.interpretability;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.annotations.QualityReport;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.qualitymetrics.utilities.TestLoader;


/**
 * @author Jeremy Debattista
 * 
 * Test for the Undefined Classes and Properties Metric.
 * In the used dataset, there are 11 Undefined Classes,
 * 23 Undefined Properties and a total of 145 unique
 * classes and properties.
 * 
 */
public class UndefinedClassesAndPropertiesTest  extends Assert {

	TestLoader loader = new TestLoader();
	UndefinedClassesAndProperties metric = new UndefinedClassesAndProperties();
	
	@Before
	public void setUp(){
		loader.loadDataSet("testdumps/eis.ttl");
	}
	

	@Test
	public void undefinedClassesAndPropertiesTest(){
		for(Quad q : loader.getStreamingQuads()){
			metric.compute(q);
		}
				
		assertEquals(0.765517241, metric.metricValue(), 0.00001);
	}
	

	@Ignore
	@Test
	public void problemReportTest(){
		for(Quad q : loader.getStreamingQuads()){
			metric.compute(q);
		}
		
		ProblemList<?> pl = metric.getQualityProblems();
		QualityReport qr = new QualityReport();
		String plModelURI = qr.createQualityProblem(metric.getMetricURI(), pl);
		Model plModel = qr.getProblemReportFromTBD(plModelURI);
		
		plModel.write(System.out, "TURTLE");
	
	}

}
