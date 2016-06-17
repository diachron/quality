package eu.diachron.qualitymetrics.accessibility.performance;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ReservoirSampler;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import eu.diachron.qualitymetrics.utilities.AbstractQualityMetric;
import eu.diachron.qualitymetrics.utilities.HTTPRetriever;

/**
 * @author Santiago Londono
 * Estimates the efficiency with which a system can bind to the dataset, by measuring the number of 
 * answered HTTP requests responded by the source of the dataset, per second.
 */
public class HighThroughput extends AbstractQualityMetric {
	
	private final Resource METRIC_URI = DQM.HighThroughputMetric;
	
	private static Logger logger = LoggerFactory.getLogger(HighThroughput.class);
	
	/**
	 * Amount of HTTP requests that will be sent to the data source in order to estimate how many requests are served per second. 
	 */
	private static final int NUM_HTTP_REQUESTS = 10;
	
	/**
	 * Number of requests per second that ideally, should be served by a data source. In other words, its the amount of served requests 
	 * per second above of which a resource will get a perfect score of 1.0. 
	 */
	private static final double NORM_SERVED_REQS_PER_MILLISEC = 0.0025;
	
	/**
	 * Holds the total delay as currently calculated by the compute method
	 */
	private long totalDelay = -1;
	
	/**
	 * Holds the metric value
	 */
	private Double metricValue = null;
	
	private List<Quad> _problemList = new ArrayList<Quad>();
	

	/**
	 * Processes a single quad making part of the dataset. Firstly, tries to figure out the URI/PLD of the dataset where from the quads were obtained. 
	 * A burst HTTP requests is sent to the dataset's URI and the number of requests sent is divided by the total time required to serve them,  
	 * thus obtaining the estimated number of requests server per second
	 * @param quad Quad to be processed and examined to try to extract the dataset's URI
	 */
	
	ReservoirSampler<String> resSamp = new ReservoirSampler<String>(50,true);

	public void compute(Quad quad) {
		if (quad.getSubject().isURI()){
			if (quad.getSubject().getURI().startsWith(this.getDatasetURI())) 
				resSamp.add(quad.getSubject().getURI());
			else if (this.getDatasetURI().equals(""))
				resSamp.add(quad.getSubject().getURI());
		}
	}

	/**
	 * Returns the current value of the High Throughput Metric as a ranking in the range [0, 1], with 1.0 the top ranking. 
	 * First estimates the number of served requests per second, computed as the ration between the total number of requests 
	 * sent to the dataset's endpoint and the sum of their response times. Then this estimate is normalized by dividing it 
	 * by NORM_SERVED_REQS_PER_SEC, the ideal amount of requests a resource is expected to serve per second, to get a raking of 1.0
	 * @return Current value of the High Throughput metric, measured with respect to the dataset's URI
	 */
	public double metricValue() {
		if (this.metricValue == null){
			for(String s : resSamp.getItems()){
				totalDelay += HTTPRetriever.measureReqsBurstDelay(s, NUM_HTTP_REQUESTS);
				logger.trace("Total delay for dataset {} was {}", s, totalDelay);	
			}

			double servedReqsPerMilliSec = ((double)NUM_HTTP_REQUESTS)/((double)totalDelay / (double)resSamp.getItems().size());
			this.metricValue = Math.min(1.0, servedReqsPerMilliSec / NORM_SERVED_REQS_PER_MILLISEC);
			
			statsLogger.info("HighThroughput. Dataset: {}; - Total Delay (millisecs) : {}; " +
					"# HTTP Requests Sent : {}; Norm Served Requests per Millisecond : {};", 
					this.getDatasetURI(), totalDelay, NUM_HTTP_REQUESTS, NORM_SERVED_REQS_PER_MILLISEC);
		}
				
		return this.metricValue;
	}

	public Resource getMetricURI() {
		return METRIC_URI;
	}

	public ProblemList<?> getQualityProblems() {
		ProblemList<Quad> pl = null;
		try {
			if(this._problemList != null && this._problemList.size() > 0) {
				pl = new ProblemList<Quad>(this._problemList);
			} else {
				pl = new ProblemList<Quad>();
			}
		} catch (ProblemListInitialisationException e) {
			logger.error("Error building problems list for metric High Throughput", e);
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
