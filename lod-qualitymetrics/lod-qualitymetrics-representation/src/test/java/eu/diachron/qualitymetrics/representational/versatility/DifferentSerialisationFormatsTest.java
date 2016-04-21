/**
 * 
 */
package eu.diachron.qualitymetrics.representational.versatility;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.annotations.QualityReport;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.properties.PropertyManager;
import eu.diachron.qualitymetrics.utilities.TestLoader;

/*
 * SPARQL Query Used 
 * 
 * SELECT DISTINCT (COUNT(?o) AS ?featureCount) {
 * 	?s <http://rdfs.org/ns/void#feature> ?o .
 * }
 * 
 */

/**
 * @author Jeremy Debattista
 * 
 * Tests the Different Serialisation Formats metric.
 * The dataset being used for tests has 2 different
 * serialisation formats
 */
public class DifferentSerialisationFormatsTest extends Assert {
	
	TestLoader loader = new TestLoader();
	DifferentSerialisationFormats metric = new DifferentSerialisationFormats();
	
	@Before
	public void setUp(){
//		loader.loadDataSet("/Users/jeremy/luzzu/quality-metadata/bfs.270a.info.nt.gz");
		loader.loadDataSet("testdumps/eis.ttl");
		PropertyManager.getInstance().addToEnvironmentVars("baseURI", "http://eis.iai.uni-bonn.de/");
		PropertyManager.getInstance().addToEnvironmentVars("datasetURI", "http://eis.iai.uni-bonn.de/");
	}
	
	@Test
	public void noBlankNodesTest(){
		for(Quad q : loader.getStreamingQuads()){
			metric.compute(q);
		}
		
		assertEquals(0.0, metric.metricValue(), 0.00001);
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
