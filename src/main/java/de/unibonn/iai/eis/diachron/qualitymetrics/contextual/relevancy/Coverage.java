/**
 * 
 */
package de.unibonn.iai.eis.diachron.qualitymetrics.contextual.relevancy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.ConfigurationLoader;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;

/**
 * @author Carlos
 *
 */
public class Coverage extends AbstractQualityMetric{

	private final Resource METRIC_URI = DQM.CoverageMetric;
	
	private static Logger logger = LoggerFactory.getLogger(RelevantTermsWithinMetaInformation.class);
	
	private ConfigurationLoader loader = new ConfigurationLoader();
	private List<String> properties = loader.loadAttributes(ConfigurationLoader.COVERAGE_FILE);
	private HashMap<String, List<String>> setPropertiesList;	
	private List<String> setURIs = new ArrayList<String>();
	private double metricValue;
	private int triplesCounter = 0;
	
	/**
	 * Creator Method, It create a HashMap with every of the attributes that was found in the config properties to save the information when is loading.
	 */
	public Coverage(){
		super();
		this.setPropertiesList = new HashMap<String, List<String>>();
		for (String aux: this.properties) {
			this.setPropertiesList.put(aux, new ArrayList<String>());
		}
	}
	
	@Override
	public void compute(Quad quad) {
		Node subject = quad.getSubject();
		Node predicate = quad.getPredicate();
		Node object = quad.getObject();
		
		String curSubjectURI = ((subject.isURI())?(subject.getURI()):(subject.toString()));
		this.triplesCounter++;
					
		// Check if the property of the quad is known to of one of the properties defined in the config file
		if (predicate != null && predicate.isURI() && subject != null) {
			//Goes through all the properties loaded
			for(String aux : this.properties){
				//Compare the predicate against the property
				if(predicate.getURI().equals(aux)){
					//if the subject URI is not contained on the list, then it add the new value to the list
					if(!this.isContainedInTheList(curSubjectURI, this.setPropertiesList.get(aux))){
						this.setPropertiesList.get(aux).add(curSubjectURI);
					}	
					//only if the value is added into one of the list then, it is added into the uris to check the value
					if(!this.isContainedInTheList(curSubjectURI, this.setURIs)){
						this.setURIs.add(curSubjectURI);
					}
					logger.trace("Quad providing " +aux+ " of the dataset info detected. Subject: {}, object: {}", curSubjectURI, object);
				}
			}
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
	 * This method return percentage of URIS that contained all the attributes defined in the configuration file
	 * The number of elements that contained all the information / the total number of elements in the data set
	 */
	@Override
	public double metricValue() {
		int cont = 0;
		
		for (String uri : this.setURIs) {
			boolean allData = true;
			for(String property: this.properties){
				//Check if the value if the URI doesn't have any of the values if yes then not continue and go for the next one
				if(!this.isContainedInTheList(uri, this.setPropertiesList.get(property))){
					allData = false;
					break;
				}				
			}
			//If the URI has all the properties then is counted.
			if(allData){
				cont++;
			}
		}
						
		double value = new Double(cont)/new Double(this.triplesCounter); 

		this.setMetricValue(value);
		
		//Return the number of terms that contained all the metadata information
		return value;
		
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Resource getMetricURI() {
		return METRIC_URI;
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
