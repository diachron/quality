package eu.diachron.qualitymetrics.accessibility.licensing;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.technques.probabilistic.ResourceBaseURIOracle;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.semantics.vocabulary.DQM;

/**
 * @author Santiago Londono
 * Detects whether the resource is attributed by the author or licensor under a 
 * CopyLeft license, which entails that the data will always be freely available, 
 * even after being modified by a thrid party.
 */
public class IndicationAttributionCopyLeft implements QualityMetric {
	
	private final Resource METRIC_URI = DQM.IndicationOfAttributionCopyLeftMetric;
	
	private static Logger logger = LoggerFactory.getLogger(IndicationAttributionCopyLeft.class);
	
	/**
     * Object used to determine the base URI of the resource based on its contents and to count the number of 
     * subjects being part of it
     */
	private ResourceBaseURIOracle baseURIOracle = new ResourceBaseURIOracle();
	
	/**
	 * Determines if a predicate or object provides information about the licensing model of the resource
	 */
	private LicensingModelClassifier licenseModClassifier = new LicensingModelClassifier();
	
	/**
	* A list holding the set of URIs of subjects for which a statement about their licensing model  
	* was recognized as CopyLeft compliant
	*/
	private ArrayList<String> lstCopyLeftResources = new ArrayList<String>();
	
	/**
	 * Processes a single quad being part of the dataset. Verifies if the URI of the predicate corresponding to each 
	 * triple is known to provide information about the resource's license. If that's the case, the object is checked,  
	 * to determine whether its an URI providing information about a CopyLeft license
	 * @param quad Quad to be processed and examined to determine if it states that the resource is attributed under the CopyLeft model
	 */
	public void compute(Quad quad) {
		
		// Extract the predicate (property) of the statement, the described resource (subject) and the value set (object)
		Node subject = quad.getSubject();
		Node predicate = quad.getPredicate();
		Node object = quad.getObject();
		
		// Feed the base URI oracle, which will be used to determine the resource's base URI
		this.baseURIOracle.addHint(quad);
		
		// Check if the current predicate corresponds to a licensing model statement
		if(licenseModClassifier.isLicensingPredicate(predicate)) {
			
			logger.debug("Processing licensing model predicate: {} with subject: {} and object: {}", predicate, subject, object);
			
			// Verify whether the licensing model attributed to the subject is recognized as CopyLeft
			if(licenseModClassifier.isCopyLeftLicenseURI(object)) {

				// Add licensed subject to the table of CopyLeft-ed resources
				logger.debug("CopyLeft license recognized for: {}", subject);
				this.lstCopyLeftResources.add(subject.getURI());
			}
		}
	}
	
	/**
	 * Compute the value of the metric as the ratio between the total amount of inlinks and the total amount of 
	 * data-level objects in the resource (i.e. objects of triples with predicate different to rdf:type)
	 * @return value of the dereferencibility back links metric computed on the current resource
	 */	
	public double metricValue() {
		
		// Determine the base URI of the resource
		String resourceBaseURI = this.baseURIOracle.getEstimatedResourceBaseURI();
		               
		if(resourceBaseURI != null) {
			
			logger.debug("Searching CopyLeft license for base URI: {}", resourceBaseURI);
		
			// Check if the resource base URI is is part of any of the subjects for which a license was specified
			for(String curLicensedResURI : this.lstCopyLeftResources) {
				
				if(resourceBaseURI.contains(curLicensedResURI)) {
					logger.debug("CopyLeft license found for: {}", curLicensedResURI);
					return 1.0;
				}
			}
		}
		return 0.0;
	}
	
	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	public ProblemList<?> getQualityProblems() {
		// TODO Auto-generated method stub
		return null;
	}

}
