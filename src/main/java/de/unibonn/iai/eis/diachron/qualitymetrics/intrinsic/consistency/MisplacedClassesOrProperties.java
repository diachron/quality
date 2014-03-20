package de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.consistency;

import java.util.List;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.unibonn.iai.eis.diachron.qualitymetrics.QualityMetric;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.VocabularyReader;

/**
 * 
 * @author Muhammad Ali Qasmi
 * @date 13th March 2014
 */
public class MisplacedClassesOrProperties implements QualityMetric{

	static Logger logger = Logger.getLogger(MisplacedClassesOrProperties.class);
	
	protected long misplacedClassesCount = 0;
	protected long totalClassesCount = 0;
	protected long misplacedPropertiesCount = 0;
	protected long totalPropertiesCount = 0;
	
	public long getMisplacedClassesCount() {
		return misplacedClassesCount;
	}

	public long getTotalClassesCount() {
		return totalClassesCount;
	}

	public long getMisplacedPropertiesCount() {
		return misplacedPropertiesCount;
	}

	public long getTotalPropertiesCount() {
		return totalPropertiesCount;
	}
	

	public void compute(Quad quad) {
		logger.trace("compute() --Started--");
		
		try {
			
			Node subject = quad.getSubject(); //retrieve subject
			Node predicate = quad.getPredicate(); //retrieve predicate
			Node object = quad.getObject(); //retrieve object
			
			
			if (subject.isURI()){ //check if subject is URI (not Blank)
				this.totalClassesCount++;
				//load model
				Model subjectModel = VocabularyReader.read(subject.getURI());
				if (subjectModel != null){ //check if system is able to retrieve model
					// search for URI resource from Model
					if (subjectModel.getResource(subject.getURI()).isURIResource()){
						// search for its domain and range properties
						// if it has one then it is a property not class.
						if ( subjectModel.getResource(subject.getURI()).hasProperty(RDFS.domain) || 
							 subjectModel.getResource(subject.getURI()).hasProperty(RDFS.range)) {
							this.misplacedClassesCount++;
						}
					}
				}
			}
			
			if(predicate.isURI()){ //check if predicate is URI
				this.totalPropertiesCount++;
				//load model
				Model predicateModel = VocabularyReader.read(predicate.getURI());
				if (predicateModel  != null){ //check if system is able to retrieve model
					// search for URI resource from Model					
					if(predicateModel.getResource(predicate.getURI()).isURIResource()) {
						// search for its domain and range properties
						// if it does NOT have some domain and range than its NOT a property
						if (!( predicateModel.getResource(predicate.getURI()).hasProperty(RDFS.domain) && 
							 predicateModel.getResource(predicate.getURI()).hasProperty(RDFS.range))) {
							this.misplacedPropertiesCount++;
						}
					}
				}
			}
			
			
			if (object.isURI()){ //check if object is URI (not blank or literal)
				this.totalClassesCount++;
				//load model
				Model objectModel =  VocabularyReader.read(object.getURI());
				if (objectModel != null){ //check if system is able to retrieve model
					// search for URI resource from Model
					if (objectModel.getResource(object.getURI()).isURIResource()){
						// search for its domain and range properties
						// if it has one then it is a property not class.
						if ( objectModel.getResource(object.getURI()).hasProperty(RDFS.domain) || 
								objectModel.getResource(object.getURI()).hasProperty(RDFS.range)) {
							this.misplacedClassesCount++;
						}
					}
				}
			}
		
		}
		catch (Exception exception){
			logger.debug(exception);
        	logger.error(exception.getMessage());
        	exception.printStackTrace();
		}
		
		logger.trace("compute() --Ended--");
	}

	public String getName() {
		return "MisplacedClassesOrProperties";
	}


	public double metricValue() {
		logger.trace("metricValue() --Started--");
		logger.debug("Number of Misplaced Classes :: " +  this.misplacedClassesCount);
		logger.debug("Number of Classes :: " +  this.totalClassesCount);
		logger.debug("Number of Misplaced Properties :: " +  this.misplacedPropertiesCount);
		logger.debug("Number of Properties :: " +  this.totalPropertiesCount);
		
		long tmpTotalUndefinedClassesAndUndefinedProperties = this.misplacedClassesCount + this.misplacedPropertiesCount;
		long tmpTotalClassesAndProperties = this.totalClassesCount + this.totalPropertiesCount;
		//return ZERO if total number of RDF literals are ZERO [WARN]
		if (tmpTotalClassesAndProperties <= 0) {
			logger.warn("Total number of classes and properties in given document is found to be zero.");
			return 0.0;
		}
		
		double metricValue = (double) tmpTotalUndefinedClassesAndUndefinedProperties / tmpTotalClassesAndProperties;
		logger.debug("Metric Value :: " +  metricValue);
		logger.trace("metricValue() --Ended--");
		return metricValue;
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