package eu.diachron.qualitymetrics.accessibility.performance;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ReservoirSampler;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import eu.diachron.qualitymetrics.utilities.AbstractQualityMetric;
import eu.diachron.qualitymetrics.utilities.HTTPRetriever;

/**
 * @author Santiago Londono
 * Estimates the efficiency with which a system can bind to the dataset, by measuring the delay between 
 * the submission of a request for that very dataset and reception of the respective response (or part of it)
 */
public class LowLatency extends AbstractQualityMetric {
	
	private final Resource METRIC_URI = DQM.LowLatencyMetric;
	
	private static Logger logger = LoggerFactory.getLogger(LowLatency.class);
	
	/**
	 * Amount of HTTP requests that will be sent to the data source in order to determine its latency, the 
	 * resulting delays of all of these requests will be averaged to obtain the final latency measure
	 */
	private static final int NUM_HTTP_SAMPLES = 1;
	
	/**
	 * Holds the total delay as currently calculated by the compute method
	 */
	private long totalDelay = -1;
	
	
	/**
	 * Holds the metric value
	 */
	private Double metricValue = null;
	
	/**
	 * Response time that is considered to be the ideal for a resource. In other words, its the amount of time in milliseconds below 
	 * which response times for resources will get a perfect score of 1.0. 
	 */
	private static final double NORM_TOTAL_RESPONSE_TIME = 1000.0;
	
	private List<Resource> _problemList = new ArrayList<Resource>();

	ReservoirSampler<String> resSamp = new ReservoirSampler<String>(15,true);

	public void compute(Quad quad) {
		if (quad.getSubject().isURI()){
			if (this.getDatasetURI().equals("")){
				if (!(quad.getSubject().getURI().startsWith("file"))) 
					resSamp.add(quad.getSubject().getURI());
			}
			else if (quad.getSubject().getURI().startsWith(this.getDatasetURI())) 
				resSamp.add(quad.getSubject().getURI());
		}
	}
	
	/**
	 * Returns the current value of the Low Latency Metric as a ranking in the range [0, 1], with 1.0 the top ranking. 
	 * It does so by computing the average of the time elapsed between the instant when a request is sent to the URI 
	 * of the dataset and the instant when any response is received. Then this average response time is normalized by dividing 
	 * NORM_TOTAL_RESPONSE_TIME, the ideal response time, by it
	 * @return Current value of the Low Latency metric, measured with respect to the dataset's URI
	 */
	public double metricValue() {
		
		if (this.metricValue == null){
			for (String s : resSamp.getItems()){
				totalDelay += HTTPRetriever.measureReqsBurstDelay(s, NUM_HTTP_SAMPLES);
				logger.trace("Total delay for dataset {} was {}", s, totalDelay);
			}

			double avgRespTime = ((double)totalDelay / resSamp.getItems().size()) / ((double)NUM_HTTP_SAMPLES);
			this.metricValue = Math.min(1.0, Math.max(0, NORM_TOTAL_RESPONSE_TIME / avgRespTime));
			
			statsLogger.info("LowLatency. Dataset: {}; - Total Delay (millisecs) : {}; " +
					"# HTTP Samples : {}; Norm Total Response Milliseconds : {};", 
					this.getDatasetURI(), 
					totalDelay, NUM_HTTP_SAMPLES, NORM_TOTAL_RESPONSE_TIME);
		}
		return this.metricValue;
	}

	public Resource getMetricURI() {
		return METRIC_URI;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		ProblemList<Resource> pl = null;
		try {
			if(this._problemList != null && this._problemList.size() > 0) {
				pl = new ProblemList<Resource>(this._problemList);
			} else {
				pl = new ProblemList<Resource>();
			}
		} catch (ProblemListInitialisationException e) {
			logger.error("Error building problems list for metric Low Latency", e);
		}
		return pl;
	}

	@Override
	public boolean isEstimate() {
		return false;
	}

	@Override
	public Resource getAgentURI() {
		return 	DQM.LuzzuProvenanceAgent;
	}
	
}
