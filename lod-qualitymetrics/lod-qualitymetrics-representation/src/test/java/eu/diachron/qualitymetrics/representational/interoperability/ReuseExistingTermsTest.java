/**
 * 
 */
package eu.diachron.qualitymetrics.representational.interoperability;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.annotations.QualityReport;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.BeforeException;
import de.unibonn.iai.eis.luzzu.properties.PropertyManager;
import eu.diachron.qualitymetrics.utilities.TestLoader;

/**
 * @author Jeremy Debattista
 * 
 * This tests the Reuse Existing Terms metric.
 * The dataset used has 2 distinct classes and
 * 14 distinct properties, of which 1 is a reused 
 * class term and 11 are reused property terms.
 */
public class ReuseExistingTermsTest extends Assert {

	TestLoader loader = new TestLoader();
	ReuseExistingTerms metric = new ReuseExistingTerms();
	
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
	public void reuseExistingTerms(){
		for(Quad q : loader.getStreamingQuads()){
			metric.compute(q);
		}
		
		assertEquals(0.75, metric.metricValue(), 0.0001);
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
		String plModelURI = qr.createQualityProblem(metric.getMetricURI(), pl);
		Model plModel = qr.getProblemReportFromTBD(plModelURI);
		
		plModel.write(System.out, "TURTLE");
	
	}
}
