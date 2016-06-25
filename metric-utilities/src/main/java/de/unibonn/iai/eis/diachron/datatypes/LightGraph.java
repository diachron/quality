package de.unibonn.iai.eis.diachron.datatypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.mapdb.DB;
import org.mapdb.HTreeMap;

import com.hp.hpl.jena.vocabulary.OWL;

import de.unibonn.iai.eis.diachron.mapdb.MapDBGraph;
import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;

/**
 * @author slondono
 * 
 * A memory stored, light-weight implementation of a directed, edge-labelled graph, as 
 * required by the EstimatedClusteringCoefficient metric to assess a large resource
 */
public class LightGraph extends MapDBGraph {
	
	// List containing all the node IDs existing in the graph. Intended as an integer-based index of the nodes
	private ArrayList<String> indexNodes;
	
	// Set of nodes, as a map relating each node ID (i.e. subject/object URI) to an object containing its outgoing and incoming edges
	private HashMap<String, Node> mapNodes;
	
	private DB mapDB = MapDbFactory.createHeapDB();
	private HTreeMap<String, Set<String>> sameAsNodes;
	
	// Constructor
	public LightGraph() {
		this.indexNodes = new ArrayList<String>();
		this.mapNodes = new HashMap<String, Node>();
		this.sameAsNodes = this.mapDB.createHashMap("sameAsNodes").make();
	}
	
	/**
	 * Adds a new edge to the graph and the corresponding source and target nodes, if they don't exist already
	 * @param sourceNodeId ID of the node from which the edge starts
	 * @param targetNodeId ID of the node into which the edge ends
	 * @param edgeType ID of the edge connecting the nodes
	 */
	public synchronized void  addEdge(String sourceNodeId, String targetNodeId, String edgeType) {
		// Variables holding the adjacencies of the source and target nodes, respectively
		Node sourceNodeAdj = this.mapNodes.get(sourceNodeId);
		Node targetNodeAdj = this.mapNodes.get(targetNodeId);
		
		// Check whether the source node already exists, if it doesn't add it 
		if(sourceNodeAdj == null) {
			// Instantiate and add the source node
			sourceNodeAdj = new Node();
			sourceNodeAdj.nodeIndex = this.indexNodes.size();
			
			this.indexNodes.add(sourceNodeId);
			this.mapNodes.put(sourceNodeId, sourceNodeAdj);
		}
		// Check whether the target node already exists, if it doesn't add it 
		if(targetNodeAdj == null) {
			// Instantiate and add the target node
			targetNodeAdj = new Node();
			targetNodeAdj.nodeIndex = this.indexNodes.size();
			
			this.indexNodes.add(targetNodeId);
			this.mapNodes.put(targetNodeId, targetNodeAdj);
		}
		
		Set<String> set = null;
		if (edgeType.equals(OWL.sameAs.getURI())){
			if(this.sameAsNodes.containsKey(sourceNodeId)) set = sameAsNodes.get(sourceNodeId);
			else set = new ConcurrentSkipListSet<String>();
			set.add(targetNodeId);
			this.sameAsNodes.put(sourceNodeId, set);
		}
		
		// Perform the connections corresponding to the edge, in both, the source and target nodes
		sourceNodeAdj.outEdges.add(new Pair<String,String>(edgeType, targetNodeId));
		targetNodeAdj.inEdges.add(new Pair<String,String>(edgeType, sourceNodeId));
	}
	
	public Set<String> getNeighbors(String nodeId){
		Set<String> allNeighNodeIds = new HashSet<String>();
		Node nodeAdjs = this.mapNodes.get(nodeId);
		
		// First, ensure that the node exists
		if(nodeAdjs != null) {
			// Add all incoming and outgoing neighbors of the queried node
//			allNeighNodeIds.addAll(nodeAdjs.inEdges.values());
//			allNeighNodeIds.addAll(nodeAdjs.outEdges.values());
			allNeighNodeIds.addAll(nodeAdjs.inEdgesValues());
			allNeighNodeIds.addAll(nodeAdjs.outEdgesValues());
		}

		return allNeighNodeIds;
	}
	
	public Integer getIthIndex(String nodeId){
		// Get the node (if it exists)
		Node queriedNode = this.mapNodes.get(nodeId);
		return ((queriedNode != null)?(queriedNode.nodeIndex):(null));
	}
	
	public String getNodeFromIndex(Integer i) {
		if(this.indexNodes.size() > 0) {
			int ri = Math.max(0, i);
			ri = Math.min(ri, this.indexNodes.size() - 1);
			return this.indexNodes.get(ri);
		}
		
		return null;
	}
	
	public boolean isNeighborOf(String firstNodeId, String secondNodeId){
		// Find the first node
		Node firstNode = this.mapNodes.get(firstNodeId);
		
		if(firstNode != null) {
//			return (firstNode.inEdges.containsValue(secondNodeId) || firstNode.outEdges.containsValue(secondNodeId));
			return (firstNode.inEdgeContainsValue(secondNodeId) || firstNode.outEdgeContainsValue(secondNodeId));
		} else {
			return false;
		}
	}
	
	public int degree(String nodeId){
		return (this.outDegree(nodeId) + this.inDegree(nodeId));
	}
	
	public int getVertexCount(){
		return this.mapNodes.size();
	}
	
	public Set<String> getVertices(){
		return this.mapNodes.keySet();
	}
	
	public int outDegree(String nodeId){
		// Find the node
		Node queriedNode = this.mapNodes.get(nodeId);
		
		if(queriedNode != null) {
			return (queriedNode.outEdges.size());
		} else {
			return 0;
		}
	}
	
	public int inDegree(String nodeId){
		// Find the node
		Node queriedNode = this.mapNodes.get(nodeId);
		
		if(queriedNode != null) {
			return (queriedNode.inEdges.size());
		} else {
			return 0;
		}
	}
	
	public int getCountNodes() {
		return this.indexNodes.size();
	}
	
	public HTreeMap<String, Set<String>> getSameAs(){
		return this.sameAsNodes;
	}
	
	public Set<String> getSameAsNodes(String node){
		if (this.sameAsNodes.containsKey(node)) return this.sameAsNodes.get(node);
		else return new HashSet<String>();
	}
	
	/**
	 * Represents a single node, including its adjacencies that is, the edges originating at and targeting a specific node
	 */
	private static class Node {
		// Sequential index of the node
		public int nodeIndex;
		
		// Edges originating at the node, the key is the edge type relating the nodes, the value is the ID of the target node
		public Set<Pair<String, String>>  outEdges = new HashSet<Pair<String, String>>();
		// Edges arriving at the node, the key is the edge type relating the nodes, the value is the ID of the source node
		public Set<Pair<String, String>> inEdges = new HashSet<Pair<String, String>>();
		
		
		public Collection<String> inEdgesValues(){
			Collection<String> coll = new HashSet<String>();
			for(Pair<String,String> p : inEdges){
				coll.add(p.getSecondElement());
			}
			return coll;
		}
		
		public Collection<String> outEdgesValues(){
			Collection<String> coll = new HashSet<String>();
			for(Pair<String,String> p : outEdges){
				coll.add(p.getSecondElement());
			}
			return coll;
		}
		
		public boolean inEdgeContainsValue(String id){
			for(Pair<String,String> p : inEdges){
				if (p.getSecondElement().equals(id)) return true;
			}
			return false;
		}
		
		public boolean outEdgeContainsValue(String id){
			for(Pair<String,String> p : outEdges){
				if (p.getSecondElement().equals(id)) return true;
			}
			return false;
		}
	}
}
