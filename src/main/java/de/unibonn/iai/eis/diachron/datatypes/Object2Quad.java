package de.unibonn.iai.eis.diachron.datatypes;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 *
 */
public class Object2Quad {
	
	private Quad quad;
	private Triple triple;
	
	/**
	 * This method receive an Object and tranlate its value into a Quad or a Triple
	 * @param iterator Object to be transform
	 */
	public Object2Quad(Object iterator){
		//The object come as a Quad
		if (iterator instanceof Quad){
			this.quad = (Quad) iterator;
		}
		
		//The object come as a Triple
		if (iterator instanceof Triple){
			this.triple = (Triple) iterator;
		}
		
		//The object come as a QuerySolution
		if(iterator instanceof QuerySolution){
			Node subject;
			Node predicate;
			Node object;
			Node graph;
			
			subject = this.getValue((QuerySolution) iterator, "s");
			predicate = this.getValue((QuerySolution) iterator, "p");
			object = this.getValue((QuerySolution) iterator, "o");
			graph = this.getValue((QuerySolution) iterator, "g");
			this.quad = new Quad(graph, subject, predicate, object);
		}
	}
	
	/**
	 * This method return the Quad that is transfer
	 * @return
	 */
	public Quad getStatement(){
		if (quad == null){
			quad = new Quad(null, triple);
		}
		
		return quad;
	}
	 
	/**
	 * This private metod receive the value as a QuerySolution and then return the value of the resource specify
	 * s = subject, p = predicate, o = object, g = graph.
	 * @param solution, Value to be transform
	 * @param resource, Resource to be obtained
	 * @return the Node of the resource specify
	 */
	private Node getValue(QuerySolution solution, String resource){
		RDFNode ret = solution.get(resource);
		if(ret != null)
			return ret.asNode();
		else
			return null;
	}
}
