package de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.consistency;

import java.util.List;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.unibonn.iai.eis.diachron.qualitymetrics.QualityMetric;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.VocabularyReader;

public class MisuseOwlDatatypeOrObjectProperties implements QualityMetric{

	static Logger logger = Logger.getLogger(MisuseOwlDatatypeOrObjectProperties.class);
	
	public void compute(Quad quad) {
		Node predicate = quad.getPredicate(); //retrieve predicate
		
		if(predicate.isURI()){ //check if predicate is URI
			Model predicateModel = VocabularyReader.read(predicate.getURI());
			if (predicateModel  != null){ //check if system is able to retrieve model
				// search for URI resource from Model					
				if (predicateModel.getResource(predicate.getURI()).isURIResource()) {
					
				}
			}
		}
		
	}

	public double metricValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<Triple> toDAQTriples() {
		// TODO Auto-generated method stub
		return null;
	}

	public Resource getMetricURI() {
		// TODO Auto-generated method stub
		return null;
	}

}
