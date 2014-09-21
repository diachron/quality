/**
 * 
 */
package de.unibonn.iai.eis.diachron.qualitymetrics.trust.reputation.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.ConfigurationLoader;

/**
 * @author Carlos
 *
 */
public class ReputationUtil {
	
	private List<Rating> storeRatings;

	public ReputationUtil(){
		this.storeRatings = new ArrayList<Rating>();
	}
	
	/**
	 * @return the storeRatings
	 */
	public List<Rating> getStoreRatings() {
		return storeRatings;
	}

	/**
	 * @param storeRatings the storeRatings to set
	 */
	public void setStoreRatings(List<Rating> storeRatings) {
		this.storeRatings = storeRatings;
	}
	
	public static void main(String[] args) {
		ReputationUtil rep = new  ReputationUtil();
		Rating rating1 = new Rating();
		rating1.setEvaluator("Evaluator 1");
		rating1.getRecomendedDataSets().add("http://omim.bio2rdf.org/sparql");
		
		Rating rating2 = new Rating();
		rating2.setEvaluator("Evaluator 2");
		rating2.getRecomendedDataSets().add("http://omim.bio2rdf.org/sparql");
		rating2.getRecomendedDataSets().add("http://genage.bio2rdf.org/sparql");
		
		Rating rating3 = new Rating();
		rating3.setEvaluator("Evaluator 3");
		rating3.getRecomendedDataSets().add("http://omim.bio2rdf.org/sparql");
		rating3.getRecomendedDataSets().add("http://genage.bio2rdf.org/sparql");
		rating3.getRecomendedDataSets().add("http://goa.bio2rdf.org/sparql");
		
		rep.getStoreRatings().add(rating1);
		rep.getStoreRatings().add(rating2);
		rep.getStoreRatings().add(rating3);
		
		ConfigurationLoader conf = new ConfigurationLoader();
		try {
			ReputationHelper.write(rep, conf.loadByKey("savedRatings", ConfigurationLoader.CONFIGURATION_FILE));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
