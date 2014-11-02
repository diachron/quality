/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.interlinking.helper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.tooling.GlobalGraphOperations;

/**
 * @author Jeremy Debattista
 * 
 */
public class ClusteringCoefficientMeasureN4J {

	private GraphDatabaseService graph;
	
	private DB mapDB = DBMaker.newTempFileDB().closeOnJvmShutdown().deleteFilesAfterClose().make();
	private HTreeMap<String, Double> _map = mapDB.createHashMap("clustering-coefficient-map").make(); 
	private HTreeMap<Long, Long> _factorialmap = mapDB.createHashMap("clustering-coefficient-factorial-map").make(); // maybe we can do this hashmap fixed in the "cache" for later use

	
	public ClusteringCoefficientMeasureN4J(final GraphDatabaseService _graph){
		this.graph = _graph;
		this.compute();
	}
	
	private void compute(){
		ExecutionEngine engine = new ExecutionEngine( graph );
		ExecutionResult result = null;
		
		Iterator<Node> it = GlobalGraphOperations.at(graph).getAllNodes().iterator();
		
		while(it.hasNext()){
			Node n = it.next();
			String name = n.getProperty("value").toString();
			
			String query = "MATCH (a { value: \""+name+"\" })--(b) WITH a, count(DISTINCT b) AS n MATCH (a)--()-[r]-()--(a) RETURN n, count(DISTINCT r) AS r";
			Map<String, Object> params = new HashMap<String, Object>();
			result = engine.execute(query, params);
			
			if (params.size()!= 0){
				int _n = Integer.parseInt(params.get("n").toString());
				int _r = Integer.parseInt(params.get("r").toString());
				
				double _possibleConn = ((double) this.factorial(_n)) / ((double)(2 * (this.factorial(_n-2))));
				_map.put(n.getProperty("value").toString(), (((double)_r)/_possibleConn));

			} 
				else _map.put(n.getProperty("value").toString(), 0.0);
			
		}
	}
	
	
	private long factorial(long i){
		if (i == 0) return 1;
		else if (_factorialmap.containsKey(i)) return _factorialmap.get(i);
		else {
			long ret = i * factorial(i-1);
			_factorialmap.put(i, ret);
			return ret;
		}
	}
	
	
	// We do not want to have resources having clustering coefficient value of 1.. 
	// having all resources having a value of 1 means that every resource is linked together.
	// if ideal is 0, then it means that we have a lot of cohesion, whilst 1 means the exact opposite.
	// In this case, 0 doesn't mean that it is bad, and 1 doesn't mean it is good
	public double getIdealMeasure(){
		double ideal = 0.0;
		double summationCC = 0.0;
		
		for(String n : _map.keySet()){
			summationCC += _map.get(n);
		}
		ideal = 1.0 - ((1.0 / _map.size()) * summationCC);
		return ideal;
	}

	
	@Override
	protected void finalize() throws Throwable {
		// Destroy persistent HashMap and the corresponding database
		try {
			if(_map != null) {
				_map.close();
				_factorialmap.close();
			}
			if(mapDB != null && !mapDB.isClosed()) {
				mapDB.close();
			}
		} catch(Throwable ex) {
		} finally {
			super.finalize();
		}
	}
}
