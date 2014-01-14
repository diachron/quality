package de.unibonn.iai.eis.diachron.qualitymetrics.utilities;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.graph.Triple;

public class TestLoader {

	protected List<Triple> streamingTriples = new ArrayList<Triple>();
	
	public void loadDataSet() {
		//load dataset
		//fill streaming triples
		
	}
	
	//here create a method which will fill some list with all triples which will
	//simulate the "streaming"
	public List<Triple> getStreamingTriples(){
		return this.streamingTriples;
	}
}
