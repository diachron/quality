/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.interlinking.helper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.unibonn.iai.eis.diachron.mapdb.MapDBGraph;

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

public class SameAsMeasure {
	
	//private DirectedGraph<String,RDFEdge> _graph;
	
	private MapDBGraph _graph;
	
	public SameAsMeasure(MapDBGraph _graph){
		this._graph = _graph;
	}
	

	//the closer to 0 the better
	public double getIdealMeasure(){
		double openChains = 0.0;
		double totalChains = 0.0;
		
		Set<String> sameAsKeySet = _graph.getSameAs().keySet();
		for(String v : sameAsKeySet){
			//get all chains for vertex V
			List<LinkedList<String>> chains = this.getAllPossibleChainsForNode(v);
			totalChains += chains.size();
			
			for(LinkedList<String> chain : chains){
				if (this.isOpenChain(chain)) {
					openChains++;
					//TODO: do some problem reporting
					//return chain of resources that do not "close" or maybe the last item in the chain not having sameAs the first item in chain
					//e.g a->b->c then report that c does not have the property/object "sameAs a" therefore it is not a closed chain 
				}
			}
		}
		
		//TODO: better return of value.. for the time, a value closer to 1 means that we are near the ideal as described by gueret et al. to 0 
		
		double ratio = openChains / totalChains;
		return (1.0 - ratio);
	}
	
	private boolean isOpenChain(LinkedList<String> chain){
		if ((chain.size() > 1) && (!(chain.getFirst().equals(chain.getLast())))){
			return true;
		}
		return false;
	}
	
	public double getMeasure(String vertex){
		double totalPath = 0.0d;
		List<LinkedList<String>> chains = this.getAllPossibleChainsForNode(vertex); 
		for(LinkedList<String> chain : chains){
			totalPath += (double)chain.size();
		}
		return totalPath;
	}
	
	
	// TODO: idea, we have one chain representing all nodes in a closed chain instead of multiple
	// Each node can have one or more sameAsChains
	private List<LinkedList<String>> getAllPossibleChainsForNode(String vertex){
		List<LinkedList<String>> llist = new ArrayList<LinkedList<String>>(); 
		
		Set<String> sameAsPathsForNode = _graph.getSameAsNodes(vertex);
		
		if (sameAsPathsForNode.size() > 0) {
			for(String n : sameAsPathsForNode) {
				LinkedList<String> _chain = new LinkedList<String>();
				_chain.add(vertex);
				_chain.addAll(getChainForNode(vertex, n));
				llist.add(_chain);
			}
		}
		return llist;
	}
	
	private LinkedList<String> getChainForNode(String startVertex, String nextVertex){
		LinkedList<String> _list = new LinkedList<String>();
		
		Set<String> sameAsPathsForNode = _graph.getSameAsNodes(nextVertex);

		if (sameAsPathsForNode.size() > 0) {
			_list.add(nextVertex);
			for(String n : sameAsPathsForNode) {
				if (n.equals(startVertex)) _list.add(n);
				else _list.addAll(getChainForNode(startVertex, n));
			}
		} else _list.add(nextVertex);
		return _list;
	}
	
	
}