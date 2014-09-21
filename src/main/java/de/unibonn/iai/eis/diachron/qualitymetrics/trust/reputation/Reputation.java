package de.unibonn.iai.eis.diachron.qualitymetrics.trust.reputation;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.qualitymetrics.trust.reputation.util.Rating;
import de.unibonn.iai.eis.diachron.qualitymetrics.trust.reputation.util.ReputationHelper;
import de.unibonn.iai.eis.diachron.qualitymetrics.trust.reputation.util.ReputationUtil;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.ConfigurationLoader;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;

/**
 * @author Carlos Montoya
 * Verifies If the given data set is recomended by some of the expert  
 */
public class Reputation extends AbstractQualityMetric {
	
	private final Resource METRIC_URI = DQM.ReputationMetric;
	
	private static Logger logger = LoggerFactory.getLogger(Reputation.class);
	
	/**
	 * Set of all atributes to check if they are contained in the dataset
	 */	
	private boolean dataSetEvaluated=false;
	private ReputationUtil util;	
	private String uriDataset;
	private int numberOfRecomendations;
	
	/**
	 * @param quad Quad to be processed and examined to try to extract the dataset's URI
	 */
	@Override
	public void compute(Quad quad) {
		
		if(!dataSetEvaluated){
			try {
				this.init();
				for(Rating rating : this.util.getStoreRatings()){
					for(String recomendedUri: rating.getRecomendedDataSets()){
						if(recomendedUri.equals(this.uriDataset)){
							numberOfRecomendations++;
							logger.debug("The dataset is recomended by: " + rating.getEvaluator());
							break;
						}
					}
				}
				dataSetEvaluated = true;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	public void init() throws IOException, Exception{
		// Load the set of ratings from the disk
		ReputationUtil ratings = new ReputationUtil();
		ConfigurationLoader conf = new ConfigurationLoader();
		ratings = ReputationHelper.read(conf.loadByKey("savedRatings", ConfigurationLoader.CONFIGURATION_FILE));
		this.util = ratings;
	}
	
	@Override
	public double metricValue() {
		return new Double(this.numberOfRecomendations)/new Double(this.util.getStoreRatings().size());
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
	 * @return the dataSetEvaluated
	 */
	public boolean isDataSetEvaluated() {
		return dataSetEvaluated;
	}

	/**
	 * @param dataSetEvaluated the dataSetEvaluated to set
	 */
	public void setDataSetEvaluated(boolean dataSetEvaluated) {
		this.dataSetEvaluated = dataSetEvaluated;
	}

	/**
	 * @return the uriDataset
	 */
	public String getUriDataset() {
		return uriDataset;
	}

	/**
	 * @param uriDataset the uriDataset to set
	 */
	public void setUriDataset(String uriDataset) {
		this.uriDataset = uriDataset;
	}

}
