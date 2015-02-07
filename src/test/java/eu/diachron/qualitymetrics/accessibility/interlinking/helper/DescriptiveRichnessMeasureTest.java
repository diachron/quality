/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.interlinking.helper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.vocabulary.OWL;

import de.unibonn.iai.eis.diachron.mapdb.MapDBGraph;


/**
 * @author Jeremy Debattista
 * 
 */
public class DescriptiveRichnessMeasureTest extends Assert {
	
	private DescriptiveRichnessMeasure sam;
	private MapDBGraph graph = new MapDBGraph();
	
	@Before
	public void setUp() {
	}
	
	@Test
	public void fullDescriptiveTest2Nodes(){
		graph.addConnectedNodes("a", "b",OWL.sameAs.getURI());
		graph.addConnectedNodes("b", "x","p1");
		graph.addConnectedNodes("b", "y","p2");
		graph.addConnectedNodes("b", "z","p3");
		sam = new DescriptiveRichnessMeasure(graph);
		
		assertEquals(3.0,sam.getMeasure("a"),0.0);
		
		assertEquals(0.25,sam.getIdealMeasure(),0.0);
	}
	
	@Test
	public void partialDescriptiveTest2Nodes(){
		graph.addConnectedNodes("a", "b",OWL.sameAs.getURI());
		graph.addConnectedNodes("a", "x","p1");
		graph.addConnectedNodes("a", "y","p2");
		graph.addConnectedNodes("b", "x","p1");
		graph.addConnectedNodes("b", "y","p2");
		graph.addConnectedNodes("b", "z","p3");
		sam = new DescriptiveRichnessMeasure(graph);
		
		assertEquals(1.0,sam.getMeasure("a"),0.0);
		
		assertEquals(0.5,sam.getIdealMeasure(),0.0);
	}
	
	@Test
	public void partialDescriptiveTest3Nodes(){
		graph.addConnectedNodes("a", "b",OWL.sameAs.getURI());
		graph.addConnectedNodes("b", "c",OWL.sameAs.getURI());

		graph.addConnectedNodes("a", "x","p1");
		graph.addConnectedNodes("c", "x","p1");
		graph.addConnectedNodes("c", "y","p2");
		sam = new DescriptiveRichnessMeasure(graph);
		
		assertEquals(1.0,sam.getMeasure("a"),0.0);
		assertEquals(2.0,sam.getMeasure("b"),0.0);

		//(1/2)+(1/3)
		assertEquals(0.83333333333,sam.getIdealMeasure(),0.00001);
	}
}
