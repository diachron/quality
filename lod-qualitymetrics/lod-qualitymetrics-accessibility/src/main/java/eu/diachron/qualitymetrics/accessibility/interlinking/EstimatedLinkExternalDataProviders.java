package eu.diachron.qualitymetrics.accessibility.interlinking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ReservoirSampler;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ResourceBaseURIOracle;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import eu.diachron.qualitymetrics.accessibility.availability.helper.ModelParser;
import eu.diachron.qualitymetrics.utilities.AbstractQualityMetric;

/**
 * In this metric we identify the total number of external linked used in the dataset. An external link
 * is identified if the subject URI is from one data source and an object URI from ￼another data source.
 * The data source should return RDF data to be considered as 'linked'.
 * In this metric rdf:type triples are skipped since these are not normally considered as part of the
 * Data Level Constant (or Data Level Position). 
 * The value returned by this metric is the number of valid external links a dataset has (i.e. the number
 * of resource links not the number of links to datasets)
 *  
 * In the estimated version of this metric, each PLD found will be tested for RDF data by a sample
 * of the resources used for linking. (See reservoirsize)
 * 
 * Based on: [1] Hogan Aidan, Umbrich Jürgen. An empirical survey of Linked Data conformance. Section 5.2, 
 * Linking, Issue VI: Use External URIs (page 26).
 * @author Santiago Londoño 
 */
public class EstimatedLinkExternalDataProviders extends AbstractQualityMetric {
	
	private final Resource METRIC_URI = DQM.LinksToExternalDataProvidersMetric;
	
	final static Logger logger = LoggerFactory.getLogger(EstimatedLinkExternalDataProviders.class);
	
	/**
	 * Parameter: default size for the reservoir 
	 */
	private static int reservoirsize = 10000;
	
	
	/**
	 * A set that holds all unique PLDs together with a sampled set of resources
	 */
	private Map<String, ReservoirSampler<String>> mapPLDs =  new HashMap<String, ReservoirSampler<String>>();
	
	
	/**
	 * A set that holds all unique PLDs that return RDF data
	 */
	private Set<String> setPLDsRDF = new HashSet<String>();

	
	private boolean computed = false;
	private List<Quad> _problemList = new ArrayList<Quad>();
	
	
	/**
	 * Processes a single quad making part of the dataset. Determines whether the subject and/or object of the quad 
	 * are data-level URIs, if so, extracts their pay-level domain and adds them to the set of TLD URIs.
	 * @param quad Quad to be processed as part of the computation of the metric
	 */
	public void compute(Quad quad) {
		logger.debug("Computing : {} ", quad.asTriple().toString());
		
		if (!(quad.getPredicate().getURI().equals(RDF.type.getURI()))){
			if ((quad.getObject().isURI()) && (!(quad.getObject().getURI().startsWith(this.getDatasetURI())))){
				this.addUriToSampler(quad.getObject().toString());
			}
		}
		
//		String objectPLD = "";
//		if (quad.getObject().isURI()) objectPLD = ResourceBaseURIOracle.extractPayLevelDomainURI(quad.getObject().toString());
		
//		if(!quad.getSubject().toString().contains(this.baseURI)) {
//			this.addUriToSampler(quad.getSubject().toString());
//		}

//		if ((objectPLD != "") ) {
//			if(!quad.getObject().toString().contains(this.getDatasetURI())) {
//				this.addUriToSampler(quad.getObject().toString());
//			}
//		}
		
	}
	
	private void addUriToSampler(String uri) {
		String pld = ResourceBaseURIOracle.extractPayLevelDomainURI(uri);
		
		if(pld != null) {
			if (this.mapPLDs.containsKey(pld)){
				ReservoirSampler<String> res = this.mapPLDs.get(pld);
				res.add(uri);
				mapPLDs.put(pld, res);
			} else {
				ReservoirSampler<String> res = new ReservoirSampler<String>(reservoirsize, true);
				res.add(uri);
				mapPLDs.put(pld, res);
			}
		}
	}

	/**
	 * Compute the value of the metric as the ratio between the number of different TLDs found among the data-level 
	 * constants of the resource that are different of the resource's TLD and the total number of 
	 * data-level constant URIs found in the resource.
	 * @return value of the existence of links to external data providers metric computed on the current resource
	 */	
	public double metricValue() {
		if (!computed){
			this.checkForRDFLinks();
			computed = true;
			
			statsLogger.info("EstimatedLinkExternalDataProviders. Dataset: {} - # Top Level Domains : {};", 
					this.getDatasetURI(), mapPLDs.size());
		}
		
		return setPLDsRDF.size();
	}
	
	private void checkForRDFLinks() {
		for (ReservoirSampler<String> curPldUris : this.mapPLDs.values()) {
			for (String s : curPldUris.getItems()){
				if (ModelParser.snapshotParser(s)) {
					setPLDsRDF.add(ResourceBaseURIOracle.extractPayLevelDomainURI(s));
					break; // stop when at least one resource is LD dereferenceable
				}
			}
		}
	}

	public Resource getMetricURI() {
		return this.METRIC_URI;
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
			logger.error(e.getMessage());
		}
		return pl;
	}
		
	@Override
	public boolean isEstimate() {
		return true;
	}

	@Override
	public Resource getAgentURI() {
		return 	DQM.LuzzuProvenanceAgent;
	}
	
	/**
	 * Sets the reservoir size parameter
	 * @param reservoirSize Approximation parameter
	 */
	public static void setReservoirSize(int reservoirSize) {
		EstimatedLinkExternalDataProviders.reservoirsize = reservoirSize;		
	}

}