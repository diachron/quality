package eu.diachron.qualitymetrics.accessibility.performance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ResourceBaseURIOracle;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;

/**
 * @author slondono
 * 
 * This metric measures one of the aspects that may negatively affect the performance of a resource: the usage of slash URIs. 
 * The performance of applications accessing the dataset will generally be improved by the usage of hash-URIs instead of slash-URIs, since 
 * with slash, the server may need to be set up to do a 303 redirect from the URI of the described element to the URI of the resource about it 
 * and with more request, performance degrades.
 *
 */
@Deprecated
public class NoUsageSlashURIs implements QualityMetric {
	
	private final Resource METRIC_URI = DQM.NoUsageOfSlashURIsMetric;
	
	final static Logger logger = LoggerFactory.getLogger(NoUsageSlashURIs.class);
	
	/**
	 * Object used to determine the base URI of the resource based on its contents and to count the number of 
	 * subjects being part of it
	 */
	private ResourceBaseURIOracle baseURIOracle = new ResourceBaseURIOracle();
	
	/**
	 * A table holding the set of URIs detected as used in the resource, at either subjects or objects of triples.
	 * The URIs used as key are obtained by taking the substring behind the last appearance of "/" in the subject's URI. As value,
	 * the table has an array containing the total number of times the key URI has been used in the resource (first element) 
	 * and the number of times this URI has been used as a hash URI (second element)
	 */
	private ConcurrentHashMap<String, Integer[]> tblUsedURIs = new ConcurrentHashMap<String, Integer[]>();

	/**
	 * Processes a single quad making part of the resource. If the subject and/or object of the quad are URIs, these are 
	 * analyzed to determine if are hierarchical, if so, they are accounted in by the metric calculation and thus, are 
	 * further examined to decide whether they are hash or slash URIs. For each analyzed URI, the corresponding stats are updated 
	 * in the table.
	 */
	public void compute(Quad quad) {

		// Feed the base URI oracle, which will be used to determine the resources base URI and to count outlinks
		this.baseURIOracle.addHint(quad);

		// Extract the URIs of current subject and object of the quad, if they are URIs
		String[] arrURIsUsed = new String[2];
		arrURIsUsed[0] = (quad.getSubject().isURI())?(quad.getSubject().getURI()):("");
		arrURIsUsed[1] = (quad.getObject().isURI())?(quad.getObject().getURI()):("");
		logger.debug("Processing triple with subject URI: {}. Object URI: {}", arrURIsUsed[0], arrURIsUsed[1]);
		
		// Process all URIs used at either subjects or objects
		for(String curUsedURI : arrURIsUsed) {
			
			// URIs ending in slash are valid, yet would be problematic to dissect. Remove trailing slash if found
			if(curUsedURI.endsWith("/")) {
				curUsedURI = curUsedURI.substring(0, curUsedURI.length() - 1);
			}
			
			if(!curUsedURI.equals("")) {

				// Only hierarchical URIs will be considered in the computation of the metric. Non-hierarchical URIs are not accounted for, 
				// as the fact that they do not represent a hierachy of resources, elicits that they cannot involve several de-reference steps
				int lastIndexOfSlash = curUsedURI.lastIndexOf('/');
				logger.debug("Analyzing hierarchical URI: {}. Last Index of /: {}", curUsedURI, lastIndexOfSlash);
				
				if(lastIndexOfSlash >= 0) {
					
					// Extract the resource name part and the scheme+path from the URI
					String schemePath = curUsedURI.substring(0, lastIndexOfSlash);
					String resourceName = curUsedURI.substring(lastIndexOfSlash + 1);
															
					// Decide whether the URI is a hash or slash URI: hash URIs are those containing a # character before the last word
					boolean isHashURI = (resourceName.lastIndexOf('#') >= 0);
					logger.debug("Hierarchical URI with path: {} and resource name: {}. Is Hash URI: {}", schemePath, resourceName, isHashURI);
					
					// Get the entry corresponding to this URI from the stats table, or initialize it if no yet there
					Integer[] curURIStats = this.tblUsedURIs.get(schemePath);
					
					if(curURIStats == null) {
						curURIStats = new Integer[2];
						curURIStats[0] = 1;
						curURIStats[1] = ((isHashURI)?(1):(0));
					} else {
						curURIStats[0] = curURIStats[0] + 1;
						curURIStats[1] = curURIStats[1] + ((isHashURI)?(1):(0));
					}
					
					// Update or add the stats for the current URI
					this.tblUsedURIs.put(schemePath, curURIStats);
					logger.debug("URI stats for key: {}. Set to: {}", schemePath, curURIStats);
				}
			}
		}

	}
	
	/**
	 * Computes the value of the metric. To do so, the base URI of the resource is estimated, as it's required to determine what URIs are 
	 * actually part of the dataset being assessed. 
	 */
	public double metricValue() {
		
		// Determine the base URI of the resource
		String resourceBaseURI = this.baseURIOracle.getEstimatedResourceBaseURI();
		logger.debug("Estimated resource base URI: {}", resourceBaseURI);
		
		int totalUsedURIs = 0;
		int totalUsedHashURIs = 0;
		
		// Check which URIs in the table are minted in the resource, as these are the ones it should be considered by the metric computation
		for(Map.Entry<String, Integer[]> curURIStats : this.tblUsedURIs.entrySet()) {
			
			logger.debug("Metric computation on URI stats entry: {}, value: {}", curURIStats.getKey(), curURIStats.getValue());
			
			// If the URI is minted as part of the resource base, it should be accounted for in the metric
			if(curURIStats.getKey().contains(resourceBaseURI)) {
				totalUsedURIs += curURIStats.getValue()[0];
				totalUsedHashURIs += curURIStats.getValue()[1];
			}
		}

		if(totalUsedURIs > 0) {
			return ((double)totalUsedHashURIs/(double)totalUsedURIs);
		} else {
			return 0;
		}
	}

	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	public ProblemList<?> getQualityProblems() {
		// TODO Auto-generated method stub
		return null;
	}

}
