package eu.diachron.qualitymetrics.accessibility.licensing;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.qualitymetrics.accessibility.availability.ResourceBaseURIOracle;
import eu.diachron.semantics.vocabulary.DQM;

/**
 * @author Santiago Londono
 * Verifies whether a human-readable text, stating the of licensing model attributed to the resource, has been provided as part of the dataset.
 * In contrast with the Machine-readable Indication of a License metric, this one looks for objects containing literal values and 
 * analyzes the text searching for key, licensing related terms. Also, additional to the license related properties this metric examines comment 
 * properties such as rdfs:label, dcterms:description, rdfs:comment.
 */
public class HumanReadableLicense implements QualityMetric {
	
	private final Resource METRIC_URI = DQM.HumanReadableLicenseMetric;
	
	private static Logger logger = LoggerFactory.getLogger(HumanReadableLicense.class);
	
	/**
	* A set containing the URIs of the subjects for which a human-readable license statement was found in the resource 
	*/
	private Set<String> setLicensedURIs = Collections.synchronizedSet(new HashSet<String>());

	/**
     * Object used to determine the base URI of the resource based on its contents and to count the number of 
     * subjects being part of it
     */
	private ResourceBaseURIOracle baseURIOracle = new ResourceBaseURIOracle();
	
	/**
	 * Determines if an object contains a human-readable license
	 */
	private LicensingModelClassifier licenseModClassifier = new LicensingModelClassifier();
	
	/**
	 * Set of some documentation properties commonly used, which might contain human-readable information about the
	 * licensing model of the resource. Objects of these properties will also be evaluated
	 */
	private static HashSet<String> setLicensingDocumProps;
	
	static {
		// Documentation properties considered to be potential containers of human-readable license information
		setLicensingDocumProps = new HashSet<String>();
		setLicensingDocumProps.add(RDFS.label.getURI());
		setLicensingDocumProps.add(DCTerms.description.getURI());
		setLicensingDocumProps.add(RDFS.comment.getURI());
	}

	/**
	 * Processes a single quad being part of the dataset. Detect triples containing as subject, the URI of the resource and as 
	 * predicate one of the license properties listed on the previous metric, or one of the documentation properties (rdfs:label, 
	 * dcterms:description, rdfs:comment) when found, evaluates the object contents to determine if it matches the features expected on 
	 * a licensing statement.
	 * @param quad Quad to be processed and examined to try to extract the text of the licensing statement
	 */
	public void compute(Quad quad) {
		
		// Extract the predicate (property) of the statement, the described resource (subject) and the value set (object)
		Node subject = quad.getSubject();
		Node predicate = quad.getPredicate();
		Node object = quad.getObject();
		
		// Feed the base URI oracle, which will be used to determine the resource's base URI
		this.baseURIOracle.addHint(quad);
		
		// Check whether the predicate corresponds to a licensing property or a documentation property...  
		if(this.licenseModClassifier.isLicensingPredicate(predicate) || 
				(predicate.isURI() && setLicensingDocumProps.contains(predicate.getURI()))) {
			logger.debug("Evaluating human-readable license candidate: {} with object: {}", predicate, object);
			
			// ... and check if the object contains text recognized to be of a human-readable license
			if(this.licenseModClassifier.isLicenseStatement(object) && subject.isURI()) {
				// add the subject's URI, described by this quad, to the set of resources with a provided human-readable license
				this.setLicensedURIs.add(subject.getURI());
				logger.debug("Human-readable license detected for subject: {}", subject);
			}
		}
	}
	
	/**
	 * Returns the current value of the Human-readable indication of a license metric. The value of the metric will be 1, 
	 * if the dataset containing the processed quads contains an annotation about the evaluated resource that provides a 
	 * human-readable text about the licensing model. 0 otherwise.
	 * @return Current value of the Human-readable indication of a license metric, measured for the whole dataset. [Range: 0 or 1. Error: -1]
	 */
	public double metricValue() {
		// Determine the base URI of the resource
		String resourceBaseURI = this.baseURIOracle.getEstimatedResourceBaseURI();

		if(resourceBaseURI != null) {
			
			logger.debug("Searching human-readable license for base URI: {}", resourceBaseURI);
		
			// As pointed out in the Java documentation (Collections.synchronizedSet method), although the setPayLevelDomainURIs  
			// set is thread-safe with respect to read/write access, iteration over the set should be manually synchronized. 
			// The computational cost of the synchronized keyword is no issue here, as invocation of the metricValue method is not massive
			synchronized(this.setLicensedURIs) {
				
				// Check if the resource base URI is is part of the set of subject URIs attributed with a human-readable license
				for(String curLicensedResURI : this.setLicensedURIs) {
					
					if(resourceBaseURI.contains(curLicensedResURI)) {
						logger.debug("Human-readable license found for: {}", curLicensedResURI);
						return 1.0;
					}
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
