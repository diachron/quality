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
public class SameAsMeasureTest extends Assert{

	private SameAsMeasure sam;
	private MapDBGraph graph = new MapDBGraph();
	
	@Before
	public void setUp() {
	}
	
	@Test
	public void simpleClosedChain(){
		graph.addConnectedNodes("a", "b",OWL.sameAs.getURI());
		graph.addConnectedNodes("b", "c",OWL.sameAs.getURI());
		graph.addConnectedNodes("b", "d","p");
		graph.addConnectedNodes("c", "a",OWL.sameAs.getURI());
		sam = new SameAsMeasure(graph);
		
		assertEquals(4.0,sam.getMeasure("a"),0.0);
		assertEquals(4.0,sam.getMeasure("b"),0.0);
		assertEquals(4.0,sam.getMeasure("c"),0.0);
		
		assertEquals(1.0,sam.getIdealMeasure(),0.0);
	}
	
	@Test
	public void simpleOpenChain(){
		graph.addConnectedNodes("a", "b",OWL.sameAs.getURI());
		graph.addConnectedNodes("b", "c","p");
		graph.addConnectedNodes("b", "d","p");
		graph.addConnectedNodes("d", "e",OWL.sameAs.getURI());
		sam = new SameAsMeasure(graph);
		
		assertEquals(2.0,sam.getMeasure("a"),0.0);
		assertEquals(0.0,sam.getMeasure("b"),0.0);
		assertEquals(2.0,sam.getMeasure("d"),0.0);
		
		assertEquals(0.0,sam.getIdealMeasure(),0.0);
	}
	
	@Test
	public void complexChains(){
		graph.addConnectedNodes("a", "b",OWL.sameAs.getURI());
		graph.addConnectedNodes("b", "c",OWL.sameAs.getURI());
		graph.addConnectedNodes("c", "a",OWL.sameAs.getURI());
		graph.addConnectedNodes("c", "d","p");
		graph.addConnectedNodes("d", "e",OWL.sameAs.getURI());
		graph.addConnectedNodes("e", "f",OWL.sameAs.getURI());
		graph.addConnectedNodes("f", "d",OWL.sameAs.getURI());
		graph.addConnectedNodes("f", "g","p");
		graph.addConnectedNodes("g", "h",OWL.sameAs.getURI());

		sam = new SameAsMeasure(graph);
		
		assertEquals(4.0,sam.getMeasure("a"),0.0);
		assertEquals(4.0,sam.getMeasure("b"),0.0);
		assertEquals(4.0,sam.getMeasure("c"),0.0);
		assertEquals(4.0,sam.getMeasure("d"),0.0);
		assertEquals(4.0,sam.getMeasure("e"),0.0);
		assertEquals(4.0,sam.getMeasure("f"),0.0);
		assertEquals(2.0,sam.getMeasure("g"),0.0);
		assertEquals(0.0,sam.getMeasure("h"),0.0);

		
		assertEquals(0.0,sam.getIdealMeasure(),0.0);
	}
	
	
	
	
	
}
