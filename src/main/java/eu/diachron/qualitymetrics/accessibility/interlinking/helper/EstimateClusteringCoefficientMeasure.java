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

import de.unibonn.iai.eis.diachron.commons.graphs.MapDBGraph;


/**
 * @author Jeremy Debattista
 * 
 */
public class EstimateClusteringCoefficientMeasure {
	
	private MapDBGraph _graph;
	private double estimateMeasure = Double.MIN_VALUE;
	
	/**
	 * Multiplying factor for the computation of the mixing time
	 */
	private static double mixingTimeFactor = 0.3; 
	
	public EstimateClusteringCoefficientMeasure(MapDBGraph _graph){
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
	
	private Map<Integer, List<Integer>> A = new HashMap<Integer, List<Integer>>();
	private Map<String, Integer> A_index = new HashMap<String, Integer>(); // holds the ith index of the node in the adjacency matrix
	private Map<String, Integer> vertexDegree; //vertex degree for random walk nodes
	
	
	private void randomWalk(){
		// Get Start Node
		String startNode = this.getRandomStartNode();
		randomWalkPath.add(startNode);
		resourcesInRandomPath.add(startNode);
		
//		double probabilityCounter = 0.0;
		
		vertexDegree = new HashMap<String, Integer>();
		
		String currentNode = startNode;
		int i = 0;
		this.A_index.put(startNode, new Integer(i));
		
		int numSteps = 0;
		while(numSteps < mixingTime){
			int totalDegree = 0;
			if (vertexDegree.containsKey(currentNode)) {
				totalDegree = vertexDegree.get(currentNode);
			} else {
				totalDegree = _graph.degree(currentNode);
				degreeRandomPath += totalDegree;
				vertexDegree.put(currentNode, totalDegree);
			}
			numSteps++;// probabilityCounter = (1.0/(double)totalDegree);
			
			//walk to next node random
			Random rand = new Random();
			int randomNumber = rand.nextInt(totalDegree);
			Object[] arrCurNodeNeighbors = _graph.getNeighbors(currentNode).toArray();
			String nextNode = (String)(arrCurNodeNeighbors[randomNumber]);
			
			// fill adj matrix
			int curI = this.A_index.get(currentNode);
			int nextI = (this.A_index.containsKey(nextNode)) ? this.A_index.get(nextNode) : ++i;
			this.A_index.put(nextNode, new Integer(nextI));
			this.addToMatrix(curI, nextI);
			//this._A[curI][nextI] = 1;
			//this._A[nextI][curI] = 1; // because we are assuming that nodes are not directed according to Hardiman and Katzir
			currentNode = nextNode;
						
			randomWalkPath.add(currentNode);
			resourcesInRandomPath.add(currentNode);
		}
	}
	
	private void addToMatrix(int i, int j){
		List<Integer> lst = new ArrayList<Integer>();
		if (this.A.containsKey(i)) lst = this.A.get(i);
		if (lst.size() < j) lst = this.fillArrayList(lst, j);
		else {
			if (lst.size() > j) lst.remove(j);
			lst.add(j, 1);
		}
		this.A.put(i, lst);
		
		// Only if nodes are bi-directional
//		lst = new ArrayList<Integer>();
//		if (this.A.containsKey(j)) lst = this.A.get(j);
//		if (lst.size() < i) lst = this.fillArrayList(lst, i);
//		else {
//			if (lst.size() > i) lst.remove(i);
//			lst.add(i, 1);
//		}
//		this.A.put(j, lst);
	}
	
	private List<Integer> fillArrayList(List<Integer> lst, int upTo){
		List<Integer> retList = new ArrayList<Integer>(lst);
		for(int i = lst.size() ; i < upTo; i++)
			retList.add(0);
		retList.add(1);
				
		return retList;
	}
	
	private void fillRestOfMatrix(){
		for(Integer i : this.A.keySet()){
			List<Integer> lst = this.A.get(i);	
			for(int j = lst.size() ; j <= this.A.keySet().size(); j++)
				lst.add(0);
			this.A.put(i, lst);
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
	
	private double calculateWeightedSum(){
		double val = 0.0;
		for(int _k = 1; _k <= (randomWalkPath.size() - 1); _k++){
			int k = this.A_index.get(this.randomWalkPath.get(_k));
			if ((k < 1) || (k >= this.A.keySet().size() - 1)) continue;
			double _adjM = (double) (this.A.get(k-1).get(k+1));//(this._A[k-1][k+1]);
			val += (_adjM) * (1.0 / ((double) _graph.degree(randomWalkPath.get(k))));
		}
		return val;
	}
	
	//estimate ideal - a more accurate mixing time would give us a better estimated result
	public double getEstimatedMeasure(){
		if (this.estimateMeasure != Double.MIN_VALUE) return this.estimateMeasure; 
		this.randomWalk();
		this.fillRestOfMatrix();
		
		Double phi = null; //the weighted sum
		Double psi = null; // the sum of the sampled nodes
		
		//double cc = calculateClusteringCoefficient();
		
		//phi = (1.0/((double)degreeRandomPath)) * cc;
		phi = (1.0/((double)randomWalkPath.size() - 2.0)) * this.calculateWeightedSum();
		psi = (1.0/((double)randomWalkPath.size())) * this.summationReciprocalValue();
		

		this.estimateMeasure = (phi.isNaN()) ? 0.0 : (phi / psi);
		
		return this.estimateMeasure;
	}

	/**
	 * Returns the current value set for the mixing time factor, the computed mixing time will be directly proportional
	 * to this factor. Get the larger the mixing time, the more nodes will be available in the random walk
	 * @return current mixing time factor
	 */
	public static double getMixigTimeFactor() {
		return mixingTimeFactor;
	}

	/**
	 * Sets the current value set for the mixing time factor, the computed mixing time will be directly proportional
	 * to this factor. Get the larger the mixing time, the more nodes will be available in the random walk
	 * @param mixigTimeFactor current mixing time factor
	 */
	public static void setMixigTimeFactor(double mixingTimeFactor) {
		EstimateClusteringCoefficientMeasure.mixingTimeFactor = mixingTimeFactor;
	}

}
