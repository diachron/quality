package de.unibonn.iai.eis.diachron.configuration;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.unibonn.iai.eis.diachron.vocabularies.DAQ;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;


public class InternalModelConf {
	// creates an empty model for the default dataset - a dataset is readonly.
	private static Dataset semanticModel = DatasetFactory.create(ModelFactory.createDefaultModel()); 
	
	static {
		// Loading DAQ ontology into memory
		Model temp = ModelFactory.createDefaultModel();
		temp.read(InternalModelConf.class.getClassLoader().getResourceAsStream("vocabularies/daq/daq.rdf"), "RDF/XML");

		semanticModel.addNamedModel(DAQ.NS, temp);
		
		temp.removeAll();
		temp.read(InternalModelConf.class.getClassLoader().getResourceAsStream("vocabularies/dqm/dqm.trig"), null);
		semanticModel.addNamedModel(DQM.NS, temp);
	}
	
	
	public static Model getDAQModel(){
		return semanticModel.getNamedModel(DAQ.NS);
	}

	public static Model getDQMModel(){
		return semanticModel.getNamedModel(DQM.NS);
	}
}
