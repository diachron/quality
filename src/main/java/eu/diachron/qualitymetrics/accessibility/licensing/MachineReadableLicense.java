package eu.diachron.qualitymetrics.accessibility.licensing;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;

import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.qualitymetrics.accessibility.performance.LowLatency;
import eu.diachron.semantics.vocabulary.DQM;

/**
 * @author Santiago Londono
 * Verifies whether consumers of the dataset are explicitly granted permission to re-use it, under defined 
 * conditions, by annotating the resource with a machine-readable indication (e.g. a VoID description) of the license.
 */
public class MachineReadableLicense implements QualityMetric {
	
	private final Resource METRIC_URI = DQM.MachineReadableLicenseMetric;
	
	private static Logger logger = LoggerFactory.getLogger(MachineReadableLicense.class);
	
	/**
	 * Map containing all the resources for which an annotation about their license has been found in the quads.
	 * The key of the map corresponds to the URI of the resource (i.e. subject in the quads) and the value contains the 
	 * object node containing the information about the license
	 */
	private ConcurrentHashMap<String, Node> mapLicensedResources = new ConcurrentHashMap<String, Node>();
	
	/**
	 * Holds the URI corresponding to the dataset. The URI of the subject representing the dataset   
	 * will be set here as soon as it is found in the processed quads
	 */
	private String dataSetURI = null;
	
	/**
	 * Set of all the URIs of properties known to provide licensing information
	 */
	private static HashSet<String> setLicenseProperties;
	
	static {
		// Initialize set of properties known to provide licensing information
		setLicenseProperties = new HashSet<String>();
		setLicenseProperties.add(DCTerms.license.getURI());
		setLicenseProperties.add(DCTerms.accessRights.getURI());
		setLicenseProperties.add(DCTerms.rights.getURI());
		setLicenseProperties.add(DC.rights.getURI());
		setLicenseProperties.add("http://www.w3.org/1999/xhtml/vocab#license");
		setLicenseProperties.add("http://creativecommons.org/ns#license");
	}

	/**
	 * Processes a single quad being part of the dataset. Firstly, tries to figure out the URI of the dataset whence the quads come. 
	 * If so, the URI is extracted from the corresponding subject and stored to be used in the calculation of the metric. Otherwise, verifies 
	 * whether the quad contains licensing information (by checking if the property is part of those known to be about licensing) and if so, stores 
	 * the URL of the subject in the map of resources confirmed to have licensing information
	 * @param quad Quad to be processed and examined to try to extract the dataset's URI
	 */
	public void compute(Quad quad) {
		// Extract the predicate (property) of the statement, the described resource (subject) and the value set (object)
		Node subject = quad.getSubject();
		Node predicate = quad.getPredicate();
		Node object = quad.getObject();
		
		// If not found yet, try to obtain the dataset's URI from the current quad, if succeded store it in the dataSetURI attribute for future use
		if(dataSetURI == null) {
			logger.trace("URI of the dataset detected: {}", dataSetURI);
			dataSetURI = LowLatency.extractDatasetURI(quad);
		}

		// Check if the property of the quad is known to provide licensing information
		if(predicate != null && predicate.isURI() && subject != null) {
			// Search for the predicate's URI in the set of license properties...
			if(setLicenseProperties.contains(predicate.getURI())) {
				// Yes, this quad provides licensing information, store the subject's URI (or ID) in the map of resources having a license
				String curSubjectURI = ((subject.isURI())?(subject.getURI()):(subject.toString()));
				logger.trace("Quad providing license info detected. Subject: {}, object: {}", curSubjectURI, object);
				
				mapLicensedResources.put(curSubjectURI, object);
			}
		}
	}

	/**
	 * Returns the current value of the Machine-readable indication of a license metric in milliseconds, the value of the metric will be 1, 
	 * if the dataset containing the processed quads contains an annotation providing information about its license. 0 otherwise.
	 * @return Current value of the Machine-readable indication of a license metric, measured for the whole dataset. [Range: 0 or 1. Error: -1]
	 */
	public double metricValue() {
		// Verify that the URI of the dataset could be determined
		if(dataSetURI != null) {
			// Check if the set of subjects for which a license was detected corresponds to the dataset
			if(mapLicensedResources.containsKey(dataSetURI)) {
				return 1;
			}
		}
		return 0;
	}

	public Resource getMetricURI() {
		return METRIC_URI;
	}

	public ProblemList<?> getQualityProblems() {
		return null;
	}

}
