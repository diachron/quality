package de.unibonn.iai.eis.diachron.qualitymetrics;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.Commons;
import de.unibonn.iai.eis.diachron.vocabularies.DAQ;

public abstract class AbstractQualityMetric implements QualityMetric{

	// This method is generic to all metrics
	public List<Statement> toDAQTriples() {
		List<Statement> lst = new ArrayList<Statement>();
		
		Resource generatedURI = Commons.generateURI();
		
		Statement type = new StatementImpl(generatedURI, RDF.type, this.getMetricURI().asResource());
		Statement dc = new StatementImpl(generatedURI, DAQ.dateComputed, Commons.generateCurrentTime());
		Statement val = new StatementImpl(generatedURI, DAQ.doubleValue, Commons.generateDoubleTypeLiteral(this.metricValue()));
		
		
		lst.add(type);
		lst.add(dc);
		lst.add(val);
		
		return lst;
	}
	
	// The following classes are implemented by the metric itself
	public abstract void compute(Quad quad);
	public abstract double metricValue();
	public abstract Resource getMetricURI();
	public abstract ProblemList<?> getQualityProblems();
}
