/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.interlinking.helper;

import java.util.Iterator;
import java.util.Set;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.tooling.GlobalGraphOperations;


/**
 * @author Jeremy Debattista
 * 
 */
public class CentralityMeasureN4J {
	
	private GraphDatabaseService graph;
	private DB mapDB = DBMaker.newTempFileDB().closeOnJvmShutdown().deleteFilesAfterClose().make();
	private HTreeMap<String, Double> _map = mapDB.createHashMap("centrality-measure-map").make(); 

	
	public CentralityMeasureN4J(final GraphDatabaseService _graph){
		this.graph = _graph;
	}
	
	//The lower the centrality measure, the better as having a high number of central points is prone to critical failure
	public double getIdealMeasure(){
		double min = Double.MAX_VALUE;
		double max = 0.0;
		
		Iterator<Node> it = GlobalGraphOperations.at(graph).getAllNodes().iterator();
		
		int vertexCount = 0;
		while(it.hasNext()){
			double d = this.getMeasure(it.next());
			max = (max >= d) ? max : d ;
			min = (d < min) ? d : min;
			vertexCount++;
		}
		
		int totalVer = vertexCount- 1;
		double dCentrality = 0.0;
		for(Double d : _map.values()){
			dCentrality += ((max - d)/totalVer);
		}
		
		//normalise
		if (min-max != 0) 
			dCentrality = (dCentrality - min)/(max-min);
		else
			dCentrality = 0.5;
		
		return dCentrality;
	}

	private double getMeasure(Node node){
		double in = 0;
		double out = 0;
		
		Iterable<Relationship> _inRel = node.getRelationships(Direction.INCOMING);
		Iterator<Relationship> iterator = _inRel.iterator();
		
		while(iterator.hasNext()){
			Relationship rel = iterator.next();
			in += Math.max(1, rel.getStartNode().getDegree(Direction.INCOMING));
		}
		
		Iterable<Relationship> _outRel = node.getRelationships(Direction.OUTGOING);
		iterator = _outRel.iterator();
		
		while(iterator.hasNext()){
			Relationship rel = iterator.next();
			in += Math.max(1, rel.getEndNode().getDegree(Direction.OUTGOING));
		}		
		
		double ret = (in > 0) ? out / in : 0;
		_map.put(node.getProperty("value").toString(), ret);
		return ret;
	}
	
	@Override
	protected void finalize() throws Throwable {
		// Destroy persistent HashMap and the corresponding database
		try {
			if(_map != null) {
				_map.close();
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
