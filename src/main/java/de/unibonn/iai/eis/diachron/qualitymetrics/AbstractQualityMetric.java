package de.unibonn.iai.eis.diachron.qualitymetrics;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.Commons;
import de.unibonn.iai.eis.diachron.vocabularies.DAQ;

public abstract class AbstractQualityMetric implements QualityMetric{

	// This method is generic to all metrics
	public List<Triple> toDAQTriples() {
		List<Triple> lst = new ArrayList<Triple>();
		Node generatedURI = Commons.generateURI().asNode();
		Triple type = new Triple(generatedURI, RDF.type.asNode(), this.getMetricURI().asNode());
		Triple dc = new Triple(generatedURI, DAQ.dateComputed.asNode(), Commons.generateCurrentTime().asNode());
		Triple val = new Triple(generatedURI, DAQ.doubleValue.asNode(), Commons.generateDoubleTypeLiteral(this.metricValue()).asNode());
		
		lst.add(type);
		lst.add(dc);
		lst.add(val);
		
		return lst;
	}
	
	// The following classes are implemented by the metric itself
	public abstract void compute(Quad quad);
	public abstract double metricValue();
	public abstract Resource getMetricURI();
	

}
