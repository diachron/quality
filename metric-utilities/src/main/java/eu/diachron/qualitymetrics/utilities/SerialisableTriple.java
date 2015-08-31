/**
 * 
 */
package eu.diachron.qualitymetrics.utilities;

import java.io.Serializable;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Jeremy Debattista
 * 
 */
public class SerialisableTriple  implements Serializable{
	private static final long serialVersionUID = 1886611930455854430L;

	private transient Triple triple;
	
	private boolean isSubjectBlank = false;
	private String subject = "";
	
	private String predicate = "";
	
	private boolean isObjectBlank = false;
	private boolean isObjectLiteral = false;
	private String object = "";
	
	public SerialisableTriple(){}
	
	public SerialisableTriple(Triple triple){
		this.triple = triple;
		
		Node _sbj = this.triple.getSubject();
		if (_sbj.isBlank()) isSubjectBlank = true;
		this.subject = _sbj.toString();
		
		this.predicate = this.triple.getPredicate().toString();
		
		Node _obj = this.triple.getObject();
		if (_obj.isBlank()) isObjectBlank = true;
		if (_obj.isLiteral()) isObjectLiteral = true;
		this.object = _obj.toString();
	}
	
	public Triple getTriple(){
		Resource _sbj, _prd;
		RDFNode _obj;
		
		if (isSubjectBlank) _sbj = ModelFactory.createDefaultModel().createResource(new AnonId());
		else _sbj = ModelFactory.createDefaultModel().createResource(subject);
			
		_prd = ModelFactory.createDefaultModel().createProperty(predicate);
		
		if (isObjectBlank) _obj = ModelFactory.createDefaultModel().createResource(new AnonId());
		else if (isObjectLiteral) _obj = ModelFactory.createDefaultModel().createLiteral(object);
		else _obj = ModelFactory.createDefaultModel().createResource(object);
		
		return new Triple(_sbj.asNode(), _prd.asNode(), _obj.asNode());
	}
	
	@Override
	public boolean equals(Object other){
		if (!(other instanceof SerialisableTriple)) return false;
		
		SerialisableTriple _otherSerialisableTriple = (SerialisableTriple) other;
		Triple _otherTriple = _otherSerialisableTriple.getTriple();
		
		return _otherTriple.equals(this.getTriple());
	}
	
	@Override
	public int hashCode(){
		StringBuilder sb = new StringBuilder();
		sb.append(this.subject);
		sb.append(this.predicate);
		sb.append(this.object);
		
		return sb.toString().hashCode();
	}
}
	
	