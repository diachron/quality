/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.interlinking;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.LightGraph;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import eu.diachron.qualitymetrics.accessibility.interlinking.helper.CentralityMeasure;
import eu.diachron.qualitymetrics.accessibility.interlinking.helper.ClusteringCoefficientMeasure;
import eu.diachron.qualitymetrics.accessibility.interlinking.helper.DegreeMeasure;
import eu.diachron.qualitymetrics.accessibility.interlinking.helper.DescriptiveRichnessMeasure;
import eu.diachron.qualitymetrics.accessibility.interlinking.helper.SameAsMeasure;
import eu.diachron.qualitymetrics.utilities.AbstractComplexQualityMetric;

/**
 * @author Jeremy Debattista
 * 
 */
public class EstimatedInterlinkDetectionMetric extends AbstractComplexQualityMetric {
	
	final static Logger logger = LoggerFactory.getLogger(EstimatedInterlinkDetectionMetric.class);

	/**  
	 * Graph corresponding to the RDF resource to be assessed
	 */
	private LightGraph graph = new LightGraph();
	
	/**
	 * Parameter defining the probability of skipping a node of the original graph, thus omitting it from the sample
	 */
	private static double skipNodeProbability = 0.15;
	
	private Random randomGen = new Random();
	
	private boolean afterExecuted = false;
	
	private double metricValue = 0.0; //In order to calculate the metric value, we get the IDEAL value of all other sub-metrics and multiply it by a 0.2 weight
	
	private final Resource METRIC_URI = DQM.InterlinkDetectionMetric;
	
	private List<Quad> _problemList = new ArrayList<Quad>();

	
	public void compute(Quad quad) {
		logger.debug("Computing : {} ", quad.asTriple().toString());
		
		String subject = "";
		if (!quad.getSubject().isBlank()){
			subject = quad.getSubject().getURI();
		} else {
			subject = quad.getSubject().getBlankNodeLabel();
		}
		
		String object = "";
		if (!quad.getObject().isBlank()){
			if (quad.getObject().isURI()){
				object = quad.getObject().getURI();
			} else {
				try{
					object = quad.getObject().getLiteralValue().toString();
				} catch (Exception e){
					object = quad.getObject().toString();
				}
			}
		} else {
			object = quad.getObject().getBlankNodeLabel();
		}
		
		String predicate = quad.getPredicate().getURI();
		
		// The triple may or may not be added to the graph, in accordance with the probability of skipping nodes 
		// and depending of whether the subject and object of the triple already are part of its nodes
		boolean addTripleToGraph = true;
		
		// Check if the subject (source) node is already in the graph...
		if(graph.getIthIndex(subject) == null) {
			logger.trace("New triple's subject: {} to be added, deciding if shall be skipped...", subject);
			
			// if it isn't, randomly decide if the subject will be added as a new node
			if(randomDecideSkipNode()) {
				addTripleToGraph = false;
				logger.trace("Non-existing subject will be skipped, triple ignored");
			}
		} 
		else {
			// The source node did exist. Check if the subject (target) node is already in the graph...
			if(graph.getIthIndex(object) == null) {
				logger.trace("New triple's object: {} to be added, deciding if shall be skipped...", object);
				
				// if it isn't, randomly decide if the target will be added as a new node
				if(randomDecideSkipNode()) {
					addTripleToGraph = false;
					logger.trace("Non-existing object will be skipped, triple ignored");
				}
			}
		}
		
		if(addTripleToGraph) {
			logger.trace("Adding new triple as edge of the graph");
			graph.addEdge(subject, object, predicate);
		} else {
			logger.trace("Triple omitted");
		}
	}
	
	/**
	 * Randomly decide if the next node should be added to the graph or skipped. 
	 * The node will be decided to be skipped with probability: skipNodeProbability
	 * @return True if the node shall be skipped, false otherwise
	 */
	private boolean randomDecideSkipNode() {
		double decision = this.randomGen.nextDouble();
		return (decision < skipNodeProbability);
	}
	
	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		ProblemList<Quad> pl = null;
		try {
			if(this._problemList != null && this._problemList.size() > 0) {
				pl = new ProblemList<Quad>(this._problemList);
			} else {
				pl = new ProblemList<Quad>();
			}
		} catch (ProblemListInitialisationException e) {
			logger.error(e.getMessage());
		}
		return pl;
	}

	public double metricValue() {
		if (!this.afterExecuted) 
			this.after();

		return this.metricValue;
	}

	public void before(Object... arg0) {
	}
	
	// Post-Processing
	public void after(Object... arg0) {
		this.afterExecuted = true;
		
//		//1. DegreeMeasure
//		DegreeMeasure dm = new DegreeMeasure(graph);
//		metricValue += dm.getIdealMeasure() * 0.2;
//		
		//2. Clustering Coefficient
		ClusteringCoefficientMeasure ccm = new ClusteringCoefficientMeasure(graph);
		ClusteringCoefficientMeasure.setMixigTimeFactor(ccmMT);
		//metricValue += ccm.getIdealMeasure() * 0.2;
		metricValue = ccm.getIdealMeasure();
		
		
//		//3. Centrality
//		CentralityMeasure cm = new CentralityMeasure(graph);
//		metricValue += cm.getIdealMeasure() * 0.2;
//		
//		//4. OpenSameAs
//		//	for this we do a ratio of the number of same as triples against the number of open sameas - ideally we have 0..
//		SameAsMeasure sam = new SameAsMeasure(graph);
//		metricValue += (1.0 - sam.getIdealMeasure()) * 0.2;
//		
//		//5. Description Richness
//		DescriptiveRichnessMeasure drm = new DescriptiveRichnessMeasure(graph);
//		metricValue += drm.getIdealMeasure() * 0.2;
		
//		statsLogger.info("EstimatedInterlinkDetectionMetric. Dataset: {} - Degree Measure : {}; Clustering Coef. : {}; " +
//				"Centrality : {}; Open Same As : {}; Description Richness : {};", 
//				this.getDatasetURI(), dm.getIdealMeasure(), ccm.getIdealMeasure(), 
//				cm.getIdealMeasure(), sam.getIdealMeasure(), drm.getIdealMeasure());
	}


	@Override
	public boolean isEstimate() {
		return true;
	}

	@Override
	public Resource getAgentURI() {
		return DQM.LuzzuProvenanceAgent;
	}
	
	// Getters and setters for the estimation parameters
	public static double getSkipNodeProbability() {
		return skipNodeProbability;
	}
	public static void setSkipNodeProbability(double skipNodeProbability) {
		EstimatedInterlinkDetectionMetric.skipNodeProbability = skipNodeProbability;
	}
	
	public static double ccmMT = 0.1;
	public static void setCCMmxtime(double p){
		ccmMT = p;
	}

}
