package eu.diachron.qualitymetrics.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.apache.jena.riot.lang.PipedTriplesStream;

import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.properties.PropertyManager;

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
		
		// Set the dataset URI into the datasetURI property, so that it's retrieved by EnvironmentProperties
		PropertyManager.getInstance().addToEnvironmentVars("datasetURI", "http://aksw.org/");
	}
	
	/**
	 * 
	 * @param fileName
	 */
	public void loadDataSet(String fileName){
		//String filename = this.getClass().getClassLoader().getResource(fileName).toExternalForm();
		
		Model m = ModelFactory.createDefaultModel();
		m.read(fileName, null);
		
		StmtIterator si = m.listStatements();
		while(si.hasNext()){
			this.streamingQuads.add(new Quad(null, si.next().asTriple()));
		}
		
		// Set the dataset URI into the datasetURI property, so that it's retrieved by EnvironmentProperties
		PropertyManager.getInstance().addToEnvironmentVars("datasetURI", fileName);
	}
	
	/**
	 * Sets a specific URI as base of the dataset to be evaluated
	 */
	public void loadDataSet(String fileName, String baseURI){
		//String filename = this.getClass().getClassLoader().getResource(fileName).toExternalForm();
		
		Model m = ModelFactory.createDefaultModel();
		m.read(fileName, null);
		
		StmtIterator si = m.listStatements();
		while(si.hasNext()){
			this.streamingQuads.add(new Quad(null, si.next().asTriple()));
		}
		
		// Set the dataset URI into the datasetURI property, so that it's retrieved by EnvironmentProperties
		PropertyManager.getInstance().addToEnvironmentVars("datasetURI", fileName);
		// Set the dataset's base URI into the baseURI property, so that it's retrieved by EnvironmentProperties
		PropertyManager.getInstance().addToEnvironmentVars("baseURI", baseURI);
	}
	
	/**
	 * Returns a list of triples from the loaded dataset. This can be used 
	 * to simulate the streaming of triples
	 * @return list of Triples
	 */
	public List<Quad> getStreamingQuads(){
		return this.streamingQuads;
	}	
	
	@SuppressWarnings("unchecked")
	public PipedRDFIterator<?> streamParser(final String fileName){
		PipedRDFIterator<?> iterator = new PipedRDFIterator<Triple>();
		final PipedRDFStream<?> rdfStream = new PipedTriplesStream((PipedRDFIterator<Triple>) iterator);
		
		ExecutorService executor = Executors.newSingleThreadExecutor();

		Runnable parser = new Runnable(){
			@Override
			public void run() {
				try{
					RDFDataMgr.parse(rdfStream, fileName);
				} catch (Exception e){
					rdfStream.finish();
				}
			}			
		};
		executor.submit(parser);

		
		return iterator;
	}
}
