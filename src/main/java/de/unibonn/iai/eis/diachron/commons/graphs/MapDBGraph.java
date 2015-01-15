/**
 * 
 */
package de.unibonn.iai.eis.diachron.commons.graphs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
	
	//Adjacency Matrix
	private HTreeMap<Integer, List<Byte>> A = this.mapDB.createHashMap("graph-adjacency-matrix").make();
	private HTreeMap<String, Integer> A_index = this.mapDB.createHashMap("graph-adjacency-matrix-index").make(); // holds the ith index of the node in the adjacency matrix
	
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
		synchronized(this.adjListOutEdges) {
			this.adjListOutEdges.put(inNode, adjOut);
		}
		
		
		Set<String> adjIn = null;
		if (this.adjListInEdges.containsKey(outNode)){
			adjIn = this.adjListInEdges.get(outNode);
			adjIn.add(inNode);
		} else {
			adjIn = new ConcurrentSkipListSet<String>();
			adjIn.add(inNode);
		}
		synchronized(this.adjListInEdges) {
			this.adjListInEdges.put(outNode, adjIn);
		}
		
		Set<String> set = null;
		if (edge.equals(OWL.sameAs.getURI())){
			if(this.sameAsNodes.containsKey(inNode)) set = sameAsNodes.get(inNode);
			else set = new ConcurrentSkipListSet<String>();
			set.add(outNode);
			this.sameAsNodes.put(inNode, set);
		}
		
		if (!A_index.containsKey(inNode)) A_index.put(inNode, vertexCount);
		int ith =  A_index.get(inNode);
		vertexCount = (this.hashSet.add(inNode)) ? vertexCount + 1 : vertexCount;
		
		if (!A_index.containsKey(outNode)) A_index.put(outNode, vertexCount);
		int jth =  A_index.get(outNode);
		vertexCount = (this.hashSet.add(outNode)) ? vertexCount + 1 : vertexCount;

		this.addToMatrix(ith, jth);
	}
	
	
	private void addToMatrix(int i, int j){
		List<Byte> lst = new ArrayList<Byte>();
		if (this.A.containsKey(i)) lst = this.A.get(i);
		if (lst.size() < j) lst = this.fillArrayList(lst, j, true);
		else {
			if (lst.size() > j) lst.remove(j);
			lst.add(j, (byte) 1);
		}
		synchronized(this.A) {
			this.A.put(i, lst);
		}
		
		lst = new ArrayList<Byte>();
		if (this.A.containsKey(j)) lst = this.A.get(j);
		if (lst.size() < j) lst = this.fillArrayList(lst, j, false);
		synchronized(this.A) {
			this.A.put(j, lst);
		}
	}
	
	private List<Byte> fillArrayList(List<Byte> lst, int upTo, boolean setLastBit){
		List<Byte> retList = new ArrayList<Byte>(lst);
		for(int i = lst.size() ; i < upTo; i++)
			retList.add((byte) 0);
		if (setLastBit) retList.add((byte) 1);
		else retList.add((byte) 0);
				
		return retList;
	}
	
	public void fillRestOfMatrix(){
		for(Integer i : this.A.keySet()){
			List<Byte> lst = new ArrayList<Byte>(this.A.get(i));	
			for(int j = lst.size() ; j <= vertexCount; j++)
			{
				lst.add((byte) 0);
			}
			synchronized(this.A) {
				this.A.put(i, lst);
			}
		}
	}
	
	public void printAM() throws FileNotFoundException{
		File file = new File("test.csv");  
		FileOutputStream fis = new FileOutputStream(file);  
		PrintStream out = new PrintStream(fis);  
		System.setOut(out);  
		
		for(int i = 0; i < this.A.keySet().size(); i++){
			List<Byte> lst = this.A.get(i);	
			for(Byte b : lst) System.out.print(b + ";");
			System.out.println();
		}
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
	
	public byte getAMapping(int i, int j){
		return this.A.get(i).get(j);
	}
	
	public Integer getIthIndex(String node){
		return this.A_index.get(node);
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
