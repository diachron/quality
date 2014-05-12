package de.unibonn.iai.eis.diachron.qualitymetrics.utilities;

import java.util.Calendar;
import java.util.UUID;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

public class Commons {

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
}
