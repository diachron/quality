package de.unibonn.iai.eis.diachron.qualitymetrics.utilities;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Quad;

public class TestLoader {

	protected List<Quad> streamingQuads = new ArrayList<Quad>();
	
	/**
	 * Loads a sample dataset which can be used to test metrics
	 */
	public void loadDataSet() {
		
		String filename = this.getClass().getClassLoader().getResource("testdumps/160114.ttl").toExternalForm();
		
		Model m = ModelFactory.createDefaultModel();
		m.read(filename, "TTL");
		
		StmtIterator si = m.listStatements();
		while(si.hasNext()){
			this.streamingQuads.add(new Quad(null, si.next().asTriple()));
		}
	}
	
	/**
	 * 
	 * @param fileName
	 */
	public void loadDataSet(String fileName){
		String filename = this.getClass().getClassLoader().getResource(fileName).toExternalForm();
		
		Model m = ModelFactory.createDefaultModel();
		m.read(filename, null);
		
		StmtIterator si = m.listStatements();
		while(si.hasNext()){
			this.streamingQuads.add(new Quad(null, si.next().asTriple()));
		}
	}
	/**
	 * Returns a list of triples from the loaded dataset. This can be used 
	 * to simulate the streaming of triples
	 * @return list of Triples
	 */
	public List<Quad> getStreamingQuads(){
		return this.streamingQuads;
	}	
}
