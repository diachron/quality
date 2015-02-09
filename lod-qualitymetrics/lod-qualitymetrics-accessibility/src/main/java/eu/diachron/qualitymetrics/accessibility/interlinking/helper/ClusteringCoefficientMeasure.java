/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.interlinking.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import de.unibonn.iai.eis.diachron.mapdb.MapDBGraph;


/**
 * @author Jeremy Debattista
 * 
 */
public class ClusteringCoefficientMeasure {
	
	private MapDBGraph _graph;
	private double estimateMeasure = Double.MIN_VALUE;
	
	public ClusteringCoefficientMeasure(MapDBGraph _graph){
		this._graph = _graph;
	}
	
	// We do not want to have resources having clustering coefficient value of 1.. 
	// having all resources having a value of 1 means that every resource is linked together.
	// if ideal is 0, then it means that we have a lot of cohesion, whilst 1 means the exact opposite.
	// In this case, 0 doesn't mean that it is bad, and 1 doesn't mean it is good
	// This will be the ideal over the estimated measure
	public double getIdealMeasure(){
		if (estimateMeasure == Double.MIN_VALUE) this.getEstimatedMeasure();
		
		return (1.0 - ((1.0/((double)this.resourcesInRandomPath.size()))* this.estimateMeasure));
	}

	
	// Estimation - do not care about direction
	
	private List<String> randomWalkPath = new ArrayList<String>(); // This contains a list of nodes in a random walk path
	private Set<String> resourcesInRandomPath = new HashSet<String>();
	private int degreeRandomPath = 0;
	private double mixingTime = 0.0; 

	
	private String getRandomStartNode(){
		mixingTime = 10* (Math.log(_graph.getVertexCount()) * Math.log(_graph.getVertexCount())); //this is the most important factor.. the larger the mixing
																									//time, the more nodes will be available in the random walk
		//Get Start Node
		Iterator<String> iterNodes = _graph.getVertices().iterator();
		String node = iterNodes.next();
		while (_graph.outDegree(node) <= 0){
			node = iterNodes.next();
		}
		
		return node;
	}
	
	private void randomWalk(){
		// Get Start Node
		String startNode = this.getRandomStartNode();
		randomWalkPath.add(startNode);
		resourcesInRandomPath.add(startNode);
		
		double probabilityCounter = 0.0;
		
		Map<String, Integer> vertexDegree = new HashMap<String, Integer>();
		
		String currentNode = startNode;
		while(probabilityCounter < mixingTime){
			int totalDegree = 0;
			if (vertexDegree.containsKey(currentNode)) {
				totalDegree = vertexDegree.get(currentNode);
			} else {
				totalDegree = _graph.degree(currentNode);
				degreeRandomPath += totalDegree;
				vertexDegree.put(currentNode, totalDegree);
			}
			probabilityCounter += (1.0/(double)totalDegree);
			
			//walk to next node random
			Random rand = new Random();
			int randomNumber = rand.nextInt(totalDegree);
			currentNode = (String) (_graph.getNeighbors(currentNode).toArray()[randomNumber]);
			
			randomWalkPath.add(currentNode);
			resourcesInRandomPath.add(currentNode);
		}
	}
	
	
	
	private double calculateClusteringCoefficient() {
		Map<String, Double> vertexCC = new HashMap<String, Double>();
		
		double summationCoeffValue = 0.0;
		
		for(String vertex : randomWalkPath){
			if (vertexCC.containsKey(vertex)) continue;
			
			double ccValue = 0.0d;
			Collection<String> neighbours = _graph.getNeighbors(vertex);
			if (neighbours.size() < 2) vertexCC.put(vertex, ccValue);
			else {
				double totEdgesBetweenNeighbours_l = 0.0;

				for(String n : neighbours){
					Collection<String> n_neighbours = _graph.getNeighbors(n);
					Collection<String> intersection = null;
					Collection<String> other = null;

					if (neighbours.size() < n_neighbours.size()) {
						intersection = new HashSet<String>(neighbours);
						other = new HashSet<String>(n_neighbours);
					} else {
						intersection = new HashSet<String>(n_neighbours);
						other = new HashSet<String>(neighbours);
					}
					
					intersection.retainAll(other);
					totEdgesBetweenNeighbours_l += intersection.size();
				}
				
				double possible_edges = (neighbours.size() * (neighbours.size() - 1))/2.0;
				ccValue = (totEdgesBetweenNeighbours_l / 2.0) / possible_edges; //division by 2.0 because we check an edge connection twice
				vertexCC.put(vertex, ccValue); 
			}
			
			summationCoeffValue += ccValue;
		}
		return summationCoeffValue;
	}
	
	
	private double summationReciprocalValue(){
		double val = 0.0;
		for(String vertex : randomWalkPath){
			val += 1.0/(double)_graph.degree(vertex);
		}
		return val;
	}
	
	//estimate ideal - a more accurate mixing time would give us a better estimated result
	public double getEstimatedMeasure(){
		if (this.estimateMeasure != Double.MIN_VALUE) return this.estimateMeasure; 
		this.randomWalk();
		
		double phi = 0.0; //the weighted sum
		double psi = 0.0; // the sum of the sampled nodes
		
		double cc = calculateClusteringCoefficient();
		
		phi = (1.0/((double)degreeRandomPath)) * cc;
		psi = (1.0/((double)randomWalkPath.size())) * this.summationReciprocalValue();
		
		this.estimateMeasure = phi / psi;
		
		return this.estimateMeasure;
	}
}
