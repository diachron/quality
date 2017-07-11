/**
 * 
 */
package eu.diachron.qualitymetrics.representational.representationalconciseness;

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
 * Test for the No Prolix RDF Test Metric.
 * In the used dataset, there are 29 RCC
 * and a total of 1475 triples 
 */
public class NoProlixRDFTest extends Assert {

	TestLoader loader = new TestLoader();
	NoProlixRDF metric = new NoProlixRDF();
	
	@Before
	public void setUp(){
		loader.loadDataSet("testdumps/eis.ttl");
	}
	
	@Test
	public void noBlankNodesTest(){
		for(Quad q : loader.getStreamingQuads()){
			metric.compute(q);
		}
		
		assertEquals(0.98033898305, metric.metricValue(), 0.00001);
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
