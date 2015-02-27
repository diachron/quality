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
	
	private Map<String, Byte> k_param = new HashMap<String, Byte>();
	
	/**
	 * Multiplying factor for the computation of the mixing time
	 */
	private static double mixingTimeFactor = 1.0; 
	
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
	private double mixingTime = 0.0; 

	
	private String getRandomStartNode(){
		mixingTime = mixingTimeFactor * (Math.log(_graph.getVertexCount()) * Math.log(_graph.getVertexCount())); //this is the most important factor.. the larger the mixing
																									//time, the more nodes will be available in the random walk
		//Get Start Node
		Iterator<String> iterNodes = _graph.getVertices().iterator();
		String node = iterNodes.next();
		while (_graph.outDegree(node) <= 0){
			node = iterNodes.next();
		}
		
		return node;
	}
	
	private Map<String, Integer> vertexDegree; //vertex degree for random walk nodes
	
	
	private void randomWalk(){
		// Get Start Node
		String startNode = this.getRandomStartNode();
		randomWalkPath.add(startNode);
		resourcesInRandomPath.add(startNode);
		
		
		vertexDegree = new HashMap<String, Integer>();
		
		String currentNode = startNode;
		
		int numSteps = 0;
		while(numSteps < mixingTime){
			int totalDegree = 0;
			Object[] arrCurNodeNeighbors = _graph.getNeighbors(currentNode).toArray();

			if (vertexDegree.containsKey(currentNode)) {
				totalDegree = vertexDegree.get(currentNode);
			} else {
				totalDegree = arrCurNodeNeighbors.length;
				vertexDegree.put(currentNode, totalDegree);
			}
			numSteps++;
			
			this.addKParam(currentNode, arrCurNodeNeighbors);
			
			//walk to next node random
			Random rand = new Random();
			int randomNumber = rand.nextInt(totalDegree);
			String nextNode = (String)(arrCurNodeNeighbors[randomNumber]);
			
			
			currentNode = nextNode;
						
			randomWalkPath.add(currentNode);
			resourcesInRandomPath.add(currentNode);
		}
	}
	
	private void addKParam(String node, Object[] neighbours){
		Integer kN = this._graph.getIthIndex(node);
		
		String n1 = this._graph.getNodeFromIndex(kN + 1);
		String n2 = this._graph.getNodeFromIndex(kN - 1);
		
		this.k_param.put(node, (byte) ((this._graph.isNeighborOf(n1, n2)) ? 1 : 0));
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
	
	private double calculateWeightedSum(){
		double val = 0.0;
		for(int k = 1; k < (randomWalkPath.size() - 1); k++){
//			int k = _graph.getIthIndex(this.randomWalkPath.get(_k));
					
//			if ((k < 1) || (k >= _graph.getVertexCount())) continue;
			//double _adjM = (double) (this._graph.getAMapping((k-1), (k+1)));
			double _adjM = (double) this.k_param.get(this.randomWalkPath.get(k));
			val += (_adjM) * (1.0 / ((double) _graph.degree(randomWalkPath.get(k))));
		}
		return val;
	}
	
	//estimate ideal - a more accurate mixing time would give us a better estimated result
	public double getEstimatedMeasure(){
		if (this.estimateMeasure != Double.MIN_VALUE) return this.estimateMeasure; 
		this.randomWalk();
		
		Double phi = null; //the weighted sum
		Double psi = null; // the sum of the sampled nodes
		
		//double cc = calculateClusteringCoefficient();
		
		//phi = (1.0/((double)degreeRandomPath)) * cc;
		phi = (1.0/((double)randomWalkPath.size() - 2.0)) * this.calculateWeightedSum();
		psi = (1.0/((double)randomWalkPath.size())) * this.summationReciprocalValue();
		

		this.estimateMeasure = (phi.isNaN()) ? 0.0 : (phi / psi);
		
		return this.estimateMeasure;
	}

}