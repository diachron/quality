package de.unibonn.iai.eis.diachron.io.sequentialstream;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.lang.PipedQuadsStream;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.apache.jena.riot.lang.PipedTriplesStream;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.Object2Quad;
import de.unibonn.iai.eis.diachron.exceptions.ProcessorNotInitialised;
import de.unibonn.iai.eis.diachron.io.IOProcessor;


public class StreamProcessor implements IOProcessor {

	//http://jena.apache.org/documentation/io/rdf-input.html

	protected String datasetURI; // this variable holds the filename or uri of datasets which needs to be processed
	private PipedRDFIterator<?> iterator;
	protected PipedRDFStream<?> rdfStream;
	
	private ExecutorService executor = Executors.newSingleThreadExecutor(); // PipedRDFStream and PipedRDFIterator need to be on different threads

	private boolean isInitalised = false;
	
	public StreamProcessor(String datasetURI){
		this.datasetURI = datasetURI;
	}

	@SuppressWarnings("unchecked")
	public void setUpProcess() {
		Lang lang  = RDFLanguages.filenameToLang(datasetURI);

		// Here we are creating an PipedRDFStream, which accepts triples or quads
		// according to the dataset. A PipedRDFIterator is also created to consume 
		// the input accepted by the PipedRDFStream.
		if ((lang == Lang.NQ) || (lang == Lang.NQUADS)){
			this.iterator = new PipedRDFIterator<Quad>();
			this.rdfStream = new PipedQuadsStream((PipedRDFIterator<Quad>) iterator);
		} else {
			this.iterator = new PipedRDFIterator<Triple>();
			this.rdfStream = new PipedTriplesStream((PipedRDFIterator<Triple>) iterator);
		}
		
		this.isInitalised = true;
	}

	public void startProcessing() throws ProcessorNotInitialised{
		//TODO: if setUp is not called, throw an error
		if(this.isInitalised == false) throw new ProcessorNotInitialised("Streaming will not start as processor has not been initalised");
		
		Runnable parser = new Runnable(){
			public void run() {
				RDFDataMgr.parse(rdfStream, datasetURI);
			}
		};
		
		executor.submit(parser);  // Start the parser on another thread
	

		// loop which will go through the statements one by one
		while (this.iterator.hasNext()){
			Object2Quad stmt = new Object2Quad(this.iterator.next());
			
			System.out.println(stmt.getStatement().toString());
			//pass it to metrics
		}
	}


	public void cleanUp() throws ProcessorNotInitialised{
		if(this.isInitalised == false) throw new ProcessorNotInitialised("Streaming will not start as processor has not been initalised");
		
		if (!this.executor.isShutdown()){
			this.executor.shutdown();
		}
	}
	
	public static void main(String[] args) throws ProcessorNotInitialised{
		String uri = "http://dbpedia.org/sparql";
		//String filename = "D:\\Users\\jdebattist\\Desktop\\raw_infobox_properties_en.nq";
		
		StreamProcessor sp = new StreamProcessor(uri);
		sp.setUpProcess();
		sp.startProcessing();
		sp.cleanUp();
	}
}
