/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.interlinking.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.tooling.GlobalGraphOperations;

import com.hp.hpl.jena.vocabulary.OWL;

import edu.uci.ics.jung.graph.DirectedGraph;

/**
 * @author Jeremy Debattista
 * 
 */

//The very common owl:sameAs property can be improperly asserted. One way to confirm a given sameAs relation 
//is correct is to find closed chains of sameAs relations between the linking resource and the resource linked. 
//This metric detects whether there are open sameAs chains in the network.
//
//Measure The metric counts the number of sameAs chains that are not closed. 
//Let pik = {eij1,...,ejyk} be a path of length y defined as a sequence of edges with the same label l(pik). 
//The number of open chains is defined as
//mpaths = ∥{pik | l(pik) = ”owl:sameAs”, k ̸= i}∥

public class SameAsMeasureN4J {
	
	private GraphDatabaseService graph;
	
	public SameAsMeasureN4J(final GraphDatabaseService _graph){
		this.graph = _graph;
	}
	

//	the closer to 0 the better
	public double getIdealMeasure(){
		double min = Double.MAX_VALUE;
		double max = 0.0;
		
		double ideal = 0.0;
		
		Iterator<Node> it = GlobalGraphOperations.at(graph).getAllNodes().iterator();
		
		while(it.hasNext()){
			double d = this.getMeasure(it.next());
			max = (max >= d) ? max : d ;
			min = (d < min) ? d : min;
			
			ideal += d;
		}
		
		if ((max-min) != 0) ideal = (ideal - min)/(max-min);
		
		return ideal;
	}
	
	private double getMeasure(Node node){
		double measure = 0.0;
		
		List<Node> path = this.getPath(node, Direction.BOTH); 

		if ((path.size() > 1) && (!path.get(path.size() - 1).equals(node.getProperty("value").toString()))){
			measure++;
		}
		
		return measure;
	}
	
	
	private List<Node> getPath(Node node, Direction d){
		List<Node> path = new ArrayList<Node>();
		path.add(node);
		
		//check if there are any inEdges with owl:SameAs
		if ((d == Direction.INCOMING) || (d == Direction.BOTH)){
			Iterable<Relationship> _inRel = node.getRelationships(Direction.INCOMING);
			Iterator<Relationship> iterator = _inRel.iterator();
			
			while(iterator.hasNext()){
				Relationship rel = iterator.next();
				if (rel.getProperty("value").toString().equals(OWL.sameAs.getURI()))
					path.addAll(0, this.getPath(rel.getStartNode(), Direction.INCOMING));
			}
		}
		
		
		if ((d == Direction.OUTGOING) || (d == Direction.BOTH)){
			Iterable<Relationship> _inRel = node.getRelationships(Direction.OUTGOING);
			Iterator<Relationship> iterator = _inRel.iterator();
			
			while(iterator.hasNext()){
				Relationship rel = iterator.next();
				if (rel.getProperty("value").toString().equals(OWL.sameAs.getURI()))
					path.addAll(0, this.getPath(rel.getEndNode(), Direction.OUTGOING));
			}
		}
		
		return path;
	}
	
	
}