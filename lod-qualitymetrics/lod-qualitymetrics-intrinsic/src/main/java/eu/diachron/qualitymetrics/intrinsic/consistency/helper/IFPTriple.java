/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.consistency.helper;

import java.io.Serializable;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

import de.unibonn.iai.eis.luzzu.semantics.utilities.Commons;

/**
 * @author Jeremy Debattista
 * 
 * This data structure defines triples that have an
 * Inverse Funtional Property predicate
 */
public class IFPTriple implements Serializable {

	private static final long serialVersionUID = -5330823594542978241L;
	
	private String subject = "";
	private String predicate = "";
	private String object = "";
	
	private String genURI = "";
	
	public IFPTriple(Triple triple){
		Node _sbj = triple.getSubject();
		if (_sbj.isBlank()) this.subject = _sbj.getBlankNodeLabel();
		else this.subject = _sbj.getURI();
		
		this.predicate = triple.getPredicate().getURI();
		
		Node _obj = triple.getObject();
		if (_obj.isBlank()) this.object = _obj.getBlankNodeLabel();
		else if (_obj.isLiteral()) this.object = _obj.getLiteral().getValue().toString();
		else this.object = _obj.getURI();
		
		this.genURI = Commons.generateURI().getURI();
	}
	
	public Resource getProblemURI(){
		return ModelFactory.createDefaultModel().createResource(this.genURI);
	}
	
	 @Override
	 public int hashCode() {
		 StringBuilder sb = new StringBuilder();
		 sb.append(predicate);
		 sb.append(object);
		 
		 return sb.toString().hashCode();
	 }

	 @Override
	 public boolean equals(Object obj) {
	    	
		 if (obj instanceof IFPTriple){
			 IFPTriple other = (IFPTriple) obj;
			 if ((!this.subject.equals(other.subject)) && this.object.equals(other.object) && this.predicate.equals(other.predicate)) return true;
			 else return false;
		 }
		 
		 return false;
	 }
	 
	 public Resource getSubject(){
		 return ModelFactory.createDefaultModel().createResource(this.subject);
	 }
}
