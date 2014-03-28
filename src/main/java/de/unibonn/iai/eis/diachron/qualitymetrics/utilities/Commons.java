package de.unibonn.iai.eis.diachron.qualitymetrics.utilities;

import java.util.Calendar;
import java.util.UUID;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

public class Commons {
	
	/**
	 * Constant holding the expanded URI of the rdf:type property
	 */
	private static final String RDF_TYPE_URI = PrefixMappingImpl.Standard.expandPrefix("rdf:type");

	private Commons(){}
	
	public static Resource generateURI(){
		String uri = "urn:"+UUID.randomUUID().toString();
		Resource r = ModelFactory.createDefaultModel().createResource(uri);
		return r;
	}
	
	public static Literal generateCurrentTime(){
		return ModelFactory.createDefaultModel().createTypedLiteral(Calendar.getInstance());
	}
	
	public static Literal generateDoubleTypeLiteral(double d){
		return ModelFactory.createDefaultModel().createTypedLiteral(d);
	}
	
	/**
	 * Determines whether the specified predicate corresponds to an instance declaration 
	 * statement, that is, whether the statement is of the form: Instance rdf:type Class
	 * @param predicateEdge A node corresponding to a triple's predicate
	 * @return true if the node represents the predicate of an instance declaration, false otherwise
	 */
	public static boolean isInstanceDeclaration(Node predicateEdge) {
		// Predicate edges can only correspond to URIs
		if(predicateEdge != null && predicateEdge.isURI()) {
			return predicateEdge.hasURI(RDF_TYPE_URI);
		}
		
		return false;
	}
}
