package de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.consistency;

import java.util.List;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.unibonn.iai.eis.diachron.qualitymetrics.QualityMetric;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.VocabularyReader;

public class MisuseOwlDatatypeOrObjectProperties implements QualityMetric{

	private static String NAMESPACE_MATCH_SUBSTRING = "/owl#";
	private static String OWL_DATA_TYPE_PROPERTY = "datatypeproperty";
	private static String OWL_OBJECT_PROPERTY = "objectproperty";
	
	protected long misuseDatatypeProperties = 0;
	protected long totalDatatypeProperties = 0;
	
	protected long misuseObjectProperties = 0;
	protected long totalObjectProperties = 0;
	
	public long getMisuseDatatypeProperties() {
		return misuseDatatypeProperties;
	}

	public long getTotalDatatypeProperties() {
		return totalDatatypeProperties;
	}

	public long getMisuseObjectProperties() {
		return misuseObjectProperties;
	}

	public long getTotalObjectProperties() {
		return totalObjectProperties;
	}
	
	protected static Logger logger = Logger.getLogger(MisuseOwlDatatypeOrObjectProperties.class);
	
	public void compute(Quad quad) {
		
		Node predicate = quad.getPredicate(); //retrieve predicate
		
		if(predicate.isURI()){ //check if predicate is URI
			// check if predicate refers to OWL namespace
			if ( predicate.getNameSpace().contains(NAMESPACE_MATCH_SUBSTRING) &&
				 predicate.getURI().split("#").length > 1){

				// retrieve predicate value
				String tmpPropertyName = predicate.getURI().split("#")[1];
				if (tmpPropertyName.toLowerCase().equals(OWL_DATA_TYPE_PROPERTY.toLowerCase())){
					
					this.totalDatatypeProperties++;
					
					// for data property Subject is Resource and Object is Literal
					if (!quad.getSubject().isURI() || !quad.getObject().isLiteral()) {
						logger.debug("Misuse Owl Datatype Property Found ::" + quad.getSubject() + " -- " + quad.getPredicate() + " --> " + quad.getObject());
						this.misuseDatatypeProperties++;
					}
				}
				else if (tmpPropertyName.toLowerCase().equals(OWL_OBJECT_PROPERTY.toLowerCase())){
					
					this.totalObjectProperties++;
					
					// for object property both Subject and Object are Resource
					if (!quad.getSubject().isURI() || !quad.getObject().isURI()) {
						logger.debug("Misuse Owl Object Property Found ::" + quad.getSubject() + " -- " + quad.getPredicate() + " --> " + quad.getObject());
						this.misuseObjectProperties++;
					}
				}
			}
		}
		
	}

	public double metricValue() {
		logger.trace("metricValue() --Started--");
		logger.debug("Number of Misuse Owl Datatype Properties :: " +  this.misuseDatatypeProperties);
		logger.debug("Total Owl Datatype Properties :: " +  this.totalDatatypeProperties);
		logger.debug("Number of Misuse Owl Object Properties :: " +  this.misuseObjectProperties);
		logger.debug("Total Owl Object Properties :: " +  this.totalObjectProperties);
		
		long tmpTotalMisusedProperties = this.misuseDatatypeProperties + this.misuseObjectProperties;
		long tmpTotalProperties = this.totalDatatypeProperties + this.totalObjectProperties;
		//return ZERO if total number of RDF literals are ZERO [WARN]
		if (tmpTotalProperties <= 0) {
			logger.warn("Total number of classes and properties in given document is found to be zero.");
			return 0.0;
		}
		
		double metricValue = (double) tmpTotalMisusedProperties / tmpTotalProperties;
		logger.debug("Metric Value :: " +  metricValue);
		logger.trace("metricValue() --Ended--");
		return metricValue;	}

	public List<Triple> toDAQTriples() {
		// TODO Auto-generated method stub
		return null;
	}

	public Resource getMetricURI() {
		// TODO Auto-generated method stub
		return null;
	}

}
