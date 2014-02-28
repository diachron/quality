package de.unibonn.iai.eis.diachron.configuration;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.unibonn.iai.eis.diachron.vocabularies.DAQ;


public class InternalModelConf {

	// creates an empty model for the default dataset - a dataset is readonly.
	private static Dataset semanticModel = DatasetFactory.create(ModelFactory.createDefaultModel()); 
	
	static {
		// Loading DAQ ontology into memory
		Model temp = ModelFactory.createDefaultModel();
		temp.read(InternalModelConf.class.getClassLoader().getResourceAsStream("vocabularies/daq/daq.rdf"), "RDF/XML");

		semanticModel.addNamedModel(DAQ.NS, temp);
	}
	
	
	public static Model getDAQModel(){
		return semanticModel.getNamedModel(DAQ.NS);
	}
}
