package de.unibonn.iai.eis.diachron.qualitymetrics.contextual.relevancy;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.DCTerms;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;

/**
 * @author Carlos Montoya
 * Verifies whether terms of the dataset contained explicit Meta Information 
 */
public class RelevantTermsWithinMetaInformation extends AbstractQualityMetric {
	
	private final Resource METRIC_URI = DQM.RelevantTermsWithinMetaInformationMetric;
	
	private static Logger logger = LoggerFactory.getLogger(RelevantTermsWithinMetaInformation.class);
	
	/**
	 * This list are used to save the information that is contained in any subject, to be taking into account and be measured
	 */
	private List<String> setTitleElements = new ArrayList<String>();
	private List<String> setDescriptionElements = new ArrayList<String>();
	private List<String> setSubjectElements = new ArrayList<String>();
	private List<String> setURIs = new ArrayList<String>();
	private double metricValue;
	private int triplesCounter = 0;
		
	/**
	 * Processes a single quad being part of the dataset. 
	 * First in check agains the list of subject if it is contained in the list of subjects, if not then it stored.
	 * Secondly it check that the predicate exist and check the value of the predicate, 
	 * if the predicate is the title, description or subject properties then it store the URI in every corresponding list.
	 * @param quad Quad to be processed and examined to try to extract the dataset's URI
	 */
	@Override
	public void compute(Quad quad) {
		// Extract the predicate (property) of the statement, the described resource (subject) and the value set (object)
		Node subject = quad.getSubject();
		Node predicate = quad.getPredicate();
		Node object = quad.getObject();
				
		String curSubjectURI = ((subject.isURI())?(subject.getURI()):(subject.toString()));
		boolean addToTheFinalSet = false;
		this.triplesCounter++;
			
		// Check if the property of the quad is known to provide licensing information
		if(predicate != null && predicate.isURI() && subject != null) {							
			// Search for the predicate's URI in the set of license properties...
			if(predicate.getURI().equals(DCTerms.title.getURI())) {
				if(!this.isContainedInTheList(curSubjectURI, this.setTitleElements)){					
					this.setTitleElements.add(curSubjectURI);
					addToTheFinalSet = true;					
				}				
				logger.trace("Quad providing title of the dataset info detected. Subject: {}, object: {}", curSubjectURI, object);				
			}//Check if the quad contain info related with the content of the data set
			else if(predicate.getURI().equals(DCTerms.description.getURI())){
				if(!this.isContainedInTheList(curSubjectURI, this.setDescriptionElements)){					
					this.setDescriptionElements.add(curSubjectURI);
					addToTheFinalSet = true;
				}				
				logger.trace("Quad providing description of the dataset info detected. Subject: {}, object: {}", curSubjectURI, object);								
			}
			else if(predicate.getURI().equals(DCTerms.subject.getURI())){
				if(!this.isContainedInTheList(curSubjectURI, this.setSubjectElements)){					
					this.setSubjectElements.add(curSubjectURI);
					addToTheFinalSet = true;
				}				
				logger.trace("Quad providing subject of the dataset info detected. Subject: {}, object: {}", curSubjectURI, object);				
			}
			
		}
		
		//Add the value in the set of URIS to be evaluated
		if(addToTheFinalSet && !this.isContainedInTheList(curSubjectURI, this.setURIs)){
			this.setURIs.add(curSubjectURI);
		}
	}

	/**
	 * Internal Method to find if an element is contained in the system.
	 * @param object To be checked against the List of elementes
	 * @param list To go through and find the element.
	 * @return
	 */
	private boolean isContainedInTheList(String object, List<String> list){
		return  list.contains(object) ;
	}
	
	/**
	 * Returns the porcentage of terms that contained Meta Info related with the dataset.
	 * 0 If any term contained all the information, 1 if all the terms contained Meta-information 
	 * @return The number of terms that contained the Meta-information (title, description, subject) Over the total number of Terms.
	 */
	@Override
	public double metricValue() {
		int cont = 0;
		for (String uri : this.setURIs) {
			if(this.isContainedInTheList(uri, this.setTitleElements) && this.isContainedInTheList(uri, this.setDescriptionElements) && this.isContainedInTheList(uri, this.setSubjectElements)){
				cont++;
			}
		}
		
		double value = new Double(cont)/new Double(this.triplesCounter); 

		//Return the number of terms that contained all the metada information
		return value;
	}

	
	/**
	 * Returns the porcentage of terms that contained Meta Info related with the dataset.
	 * 0 If any term contained all the information, 1 if all the terms contained Meta-information 
	 * @return The number of terms that contained the Meta-information (title, description) Over the total number of Terms.
	 */
	public double metricValueWithoutSubject(){
		int cont = 0;
		for (String uri : this.setURIs) {
			if(this.isContainedInTheList(uri, this.setTitleElements) && this.isContainedInTheList(uri, this.setDescriptionElements)){
				cont++;
			}
		}
		
		int size = this.setURIs.size();
		double value = new Double(cont)/new Double(size); 
		
		//Return the number of terms that contained all the metada information
		return value;
	}
	
	@Override
	public Resource getMetricURI() {
		return METRIC_URI;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		return null;
	}

	/**
	 * @return the metricValue
	 */
	public double getMetricValue() {
		return metricValue;
	}

	/**
	 * @param metricValue the metricValue to set
	 */
	public void setMetricValue(double metricValue) {
		this.metricValue = metricValue;
	}

}
