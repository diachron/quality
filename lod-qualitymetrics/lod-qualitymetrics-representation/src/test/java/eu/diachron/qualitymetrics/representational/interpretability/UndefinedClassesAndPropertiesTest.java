/**
 * 
 */
package eu.diachron.qualitymetrics.representational.interpretability;

import org.apache.jena.riot.lang.PipedRDFIterator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

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
	}
	
	@Test
	public void noBlankNodesTest(){
		PipedRDFIterator<Triple> iter = (PipedRDFIterator<Triple>) loader.streamParser("/Volumes/Green-TeaExternal/datasets/dbpedia-merged-sorted.nt.gz");

		Long counter = 0l;
		
		while (iter.hasNext()){
			Quad q = new Quad(null, iter.next());
			metric.compute(q);
			counter++;
		}		
		
		assertEquals(0.765517241, metric.metricValue(), 0.00001);
	}

}
