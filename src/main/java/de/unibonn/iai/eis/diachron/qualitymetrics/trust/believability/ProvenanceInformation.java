package de.unibonn.iai.eis.diachron.qualitymetrics.trust.believability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DCTerms;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;

/**
 * @author Carlos Montoya
 * Verifies whether the dataset are explicitly contained one of the known attributes of authenticity are
 * present into the dataset.
 */
public class ProvenanceInformation extends AbstractQualityMetric {
	
	private final Resource METRIC_URI = DQM.ProvenanceInformationMetric;
	
	private static Logger logger = LoggerFactory.getLogger(ProvenanceInformation.class);
	
	/**
	 * Set of all atributes to check if they are contained in the dataset
	 */	
	private boolean titleProperty;
	private boolean contentPoperty;
	private boolean homeurlProperty;
	private double metricValue;
		
	/**
	 * Processes a single quad being part of the dataset. 
	 * First it try to figure if the quad contain information regarding the title if yes then it store the info in the variable provided to it
	 * If not then it try to whether the quad contains content information (by checking if the property is part of those known to be about the content of the dataset information) and if so, stores in the variable 
	 * If not then it try to whether the quad contains homepage information (by checking if the property is part of those known to be about the homepage of the dataset information) and if so, stores in the variable 
	 * @param quad Quad to be processed and examined to try to extract the dataset's URI
	 */
	@Override
	public void compute(Quad quad) {
		// Extract the predicate (property) of the statement, the described resource (subject) and the value set (object)
		Node subject = quad.getSubject();
		Node predicate = quad.getPredicate();
		Node object = quad.getObject();
				
		// Check if the property of the quad is known to provide licensing information
		if(predicate != null && predicate.isURI() && subject != null) {
			// Search for the predicate's URI in the set of license properties...
			if(predicate.getURI().equals(DCTerms.title.getURI())) {
				titleProperty = true;				
				String curSubjectURI = ((subject.isURI())?(subject.getURI()):(subject.toString()));
				logger.trace("Quad providing title of the dataset info detected. Subject: {}, object: {}", curSubjectURI, object);
				
			}//Check if the quad contain info related with the content of the data set
			else if(predicate.getURI().equals(DCTerms.description.getURI())){
				contentPoperty = true;
				String curSubjectURI = ((subject.isURI())?(subject.getURI()):(subject.toString()));
				logger.trace("Quad providing content of the dataset info detected. Subject: {}, object: {}", curSubjectURI, object);				
			}
			else if(predicate.getURI().equals(FOAF.homepage.getURI())){
				homeurlProperty = true;
				String curSubjectURI = ((subject.isURI())?(subject.getURI()):(subject.toString()));
				logger.trace("Quad providing homepage of the dataset info detected. Subject: {}, object: {}", curSubjectURI, object);
			}
			
		}
	}

	/**
	 * Returns the current value of the Machine-readable indication of a believability of the dataset metric in milliseconds, the value of the metric will be 1, 
	 * if the dataset containing the processed quads contains an annotation providing information about its provenance. 0 otherwise.
	 * @return Current value of the Machine-readable indication of a license metric, measured for the whole dataset. [Range: 0 or 1. Error: -1]
	 */
	@Override
	public double metricValue() {
		//Check if the dataset contain all the information related with its provenance if it has title, the content and the home URL then it return 1 else return 0.
		if(titleProperty && contentPoperty && homeurlProperty) {
			this.setMetricValue(new Double(1));
			return 1;			
		}
		this.setMetricValue(new Double(0));
		
		return 0;
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
