/**
 * 
 */
package de.unibonn.iai.eis.diachron.commons.graphs;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.vocabulary.OWL;

import de.unibonn.iai.eis.diachron.commons.bigdata.MapDbFactory;

/**
 * @author Jeremy Debattista
 * 
 * A Class for creating a MapDB Graph
 */
public class MapDBGraph {
	
	final static Logger logger = LoggerFactory.getLogger(MapDBGraph.class);

	private DB mapDB = MapDbFactory.createAsyncFilesystemDB();
	  
	private HTreeMap<String, Set<String>> adjListInEdges = this.mapDB.createHashMap("graph-inedges-map").make();
	private HTreeMap<String, Set<String>> adjListOutEdges = this.mapDB.createHashMap("graph-outedges-map").make();
	private HTreeMap<String, Set<String>> sameAsNodes = this.mapDB.createHashMap("sameAsNodes").make();
	
	//we need something to store the edge name (i.e property name)

	private Set<String> hashSet = this.mapDB.getHashSet("nodes-hashSet");
	private int vertexCount = 0;
			
	public void addConnectedNodes(String inNode, String outNode, String edge) {
		Set<String> adjOut = null;
		if (this.adjListOutEdges.containsKey(inNode)){
			adjOut = this.adjListOutEdges.get(inNode);
			adjOut.add(outNode);
		} else {
			adjOut = new ConcurrentSkipListSet<String>();
			adjOut.add(outNode);
		}
		this.adjListOutEdges.put(inNode, adjOut);
		
		
		Set<String> adjIn = null;
		if (this.adjListInEdges.containsKey(outNode)){
			adjIn = this.adjListInEdges.get(outNode);
			adjIn.add(inNode);
		} else {
			adjIn = new ConcurrentSkipListSet<String>();
			adjIn.add(inNode);
		}
		this.adjListInEdges.put(outNode, adjIn);
		
		Set<String> set = null;
		if (edge.equals(OWL.sameAs.getURI())){
			if(this.sameAsNodes.containsKey(inNode)) set = sameAsNodes.get(inNode);
			else set = new ConcurrentSkipListSet<String>();
			set.add(outNode);
			this.sameAsNodes.put(inNode, set);
		}
		
		vertexCount = (this.hashSet.add(inNode)) ? vertexCount + 1 : vertexCount;
		vertexCount = (this.hashSet.add(outNode)) ? vertexCount + 1 : vertexCount;
	}
	
	public HTreeMap<String, Set<String>> getGraphInEdges(){
		return this.adjListInEdges;
	}
	
	public HTreeMap<String, Set<String>> getGraphOutEdges(){
		return this.adjListOutEdges;
	}
	
	public HTreeMap<String, Set<String>> getSameAs(){
		return this.sameAsNodes;
	}
	
	public Set<String> getSameAsNodes(String node){
		if (this.sameAsNodes.containsKey(node)) return this.sameAsNodes.get(node);
		else return new HashSet<String>();
	}
	
	public Set<String> getVertices(){
		return this.hashSet;
	}
	
	public Set<String> getNeighbors(String node){
		Set<String> hashSet = new HashSet<String>();
		if (this.adjListOutEdges.containsKey(node)) hashSet.addAll(this.adjListOutEdges.get(node));
		if (this.adjListInEdges.containsKey(node)) hashSet.addAll(this.adjListInEdges.get(node));
		
		return hashSet;
	}
	
	/**
	 * set of all (source) nodes where the passed node is the edge's destination  
	 * @param node
	 * @return
	 */
	public Set<String> getOutEdgeNodes(String node){
		if (this.adjListOutEdges.containsKey(node)) return this.adjListOutEdges.get(node);
		else return new HashSet<String>();
	}
	
	public Set<String> getInEdgeNodes(String node){
		if (this.adjListInEdges.containsKey(node)) return this.adjListInEdges.get(node);
		else return new HashSet<String>();
	}
	
//	private String hashString(String str){
//		HashFunction hf = Hashing.md5();
//		HashCode hc = hf.newHasher().putString(str, Charsets.UTF_8).hash();
//		return hc.toString();
//	}
	
	/**
	 * Gets total degree for node (in-edges + out-edges)
	 * @param node
	 * @return
	 */
	public int degree(String node){
		int i = 0;
		i = this.outDegree(node) + this.inDegree(node);
		return i;
	}
	
	public int getVertexCount(){
		return vertexCount;
	}
	
	public int outDegree(String node){
		return (this.adjListOutEdges.containsKey(node)) ? this.adjListOutEdges.get(node).size() : 0;
	}
	
	public int inDegree(String node){
		return (this.adjListInEdges.containsKey(node)) ? this.adjListInEdges.get(node).size() : 0;
	}
	
	@Override
	protected void finalize() throws Throwable {
		// Destroy persistent HashMap and the corresponding database
		try {
			if(this.adjListInEdges != null) {
				this.adjListInEdges.close();
				this.adjListOutEdges.close();
			}
			if(this.mapDB != null && !this.mapDB.isClosed()) {
				this.mapDB.close();
			}
		} catch(Throwable ex) {
			logger.warn("Persistent HashMap or backing database could not be closed", ex);
		} finally {
			try {
				super.finalize();
			} catch(Throwable ex) {
				logger.warn("Persistent HashMap or backing database could not be closed", ex);
			}
		}
	}
}
