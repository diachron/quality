package de.unibonn.iai.eis.diachron.streamprocesor;

import org.junit.Assert;
import org.junit.Test;

import de.unibonn.iai.eis.diachron.io.streamprocessor.Consumer;
import de.unibonn.iai.eis.diachron.io.streamprocessor.Producer;
import de.unibonn.iai.eis.diachron.io.streamprocessor.SequentialStreaming;
import de.unibonn.iai.eis.diachron.io.streamprocessor.StreamManager;

public class StreamProcesorTest extends Assert {

	//protected String urlDataSet = "http://localhost:8081/openrdf-sesame/repositories/test";
	//protected String urlDataSet = "http://omim.bio2rdf.org/sparql";
	//protected String urlDataSet = "http://geo.linkeddata.es/sparql";check
	//protected String urlDataSet = "http://resource.geolba.ac.at/PoolParty/sparql/GeologicUnit";check 
	protected String urlDataSet = "http://ndc.bio2rdf.org/sparql";
	
	
	@Test
	public void testSequentialStreaming() {
		SequentialStreaming sequentialStreaming = new SequentialStreaming();
		sequentialStreaming.setServiceUrl(urlDataSet);
		sequentialStreaming.run();
	}

	@Test
	public void testConsumerProducerStreaming() {

		StreamManager streamQuads = new StreamManager();
		Producer p1 = new Producer(streamQuads, 10000, urlDataSet);
		Consumer c1 = new Consumer(streamQuads, p1);
		p1.start();
		c1.start();

		while (c1.isAlive()) {
			System.out.print("");
		}
	}
}
