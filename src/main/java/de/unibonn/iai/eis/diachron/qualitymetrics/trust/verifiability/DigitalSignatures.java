package de.unibonn.iai.eis.diachron.qualitymetrics.trust.verifiability;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;

/**
 * @author Carlos Montoya
 * Verifies by signing a document containing an RDF serialization or signing an RDF graph 
 */
public class DigitalSignatures extends AbstractQualityMetric {
	
	private final Resource METRIC_URI = DQM.DigitalSignatureMetric;
	
	private static Logger logger = LoggerFactory.getLogger(DigitalSignatures.class);

	private double metricValue;
	/**
	 * Map containing all the resources for which an annotation about their signature has been found in the quads.
	 * The key of the map corresponds to the URI of the resource (i.e. subject in the quads) and the value contains the 
	 * object node containing the information about the signature RDF.
	 */
	private ConcurrentHashMap<String, Node> mapSignatureResources = new ConcurrentHashMap<String, Node>();
	
	/**
	 * Set of all the URIs of properties known to provide Provenance information
	 */
	private static HashSet<String> setSignatureProperties;
	
	static {
		// Initialize set of properties known to provide licensing information
		setSignatureProperties = new HashSet<String>();
		setSignatureProperties.add("http://www.w3.org/2004/03/trix/swp-2/assertedBy"); //Attribute find in the paper SWP-UserManual.pdf and Named Graphs, Provenance and Trust
		setSignatureProperties.add("http://www.w3.org/2004/03/trix/swp-2/authority"); //Attribute find in the paper SWP-UserManual.pdf and Named Graphs, Provenance and Trust
		setSignatureProperties.add("http://www.w3.org/2004/03/trix/swp-2/signature"); //Attribute find in the paper SWP-UserManual.pdf and Named Graphs, Provenance and Trust
		setSignatureProperties.add("http://www.w3.org/2004/03/trix/swp-2/signatureMethod"); //Attribute find in the paper SWP-UserManual.pdf and Named Graphs, Provenance and Trust
		setSignatureProperties.add("http://www.w3.org/2004/03/trix/swp-2/certificate"); //Attribute find in the paper SWP-UserManual.pdf and Named Graphs, Provenance and Trust
		setSignatureProperties.add("http://www.w3.org/2004/03/trix/swp-1/assertedBy"); //Attribute find in the paper SWP-UserManual.pdf and Named Graphs, Provenance and Trust
		setSignatureProperties.add("http://www.w3.org/2004/03/trix/swp-1/authority"); //Attribute find in the paper SWP-UserManual.pdf and Named Graphs, Provenance and Trust
		setSignatureProperties.add("http://www.w3.org/2004/03/trix/swp-1/signature"); //Attribute find in the paper SWP-UserManual.pdf and Named Graphs, Provenance and Trust
		setSignatureProperties.add("http://www.w3.org/2004/03/trix/swp-1/signatureMethod"); //Attribute find in the paper SWP-UserManual.pdf and Named Graphs, Provenance and Trust
		setSignatureProperties.add("http://www.w3.org/2004/03/trix/swp-1/certificate"); //Attribute find in the paper SWP-UserManual.pdf and Named Graphs, Provenance and Trust
	}

	/**
	 * Processes a single quad being part of the dataset. Firstly, tries to figure out the URI of the dataset whence the quads come. 
	 * If so, the URI is extracted from the corresponding subject and stored to be used in the calculation of the metric. Otherwise, verifies 
	 * whether the quad contains signature information (by checking if the property is part of those known to be about the signature of the dataset information) and if so, stores 
	 * the URL of the subject in the map of resources confirmed to have signature information
	 * @param quad Quad to be processed and examined to try to extract the dataset's URI
	 */
	@Override
	public void compute(Quad quad) {
		// Extract the predicate (property) of the statement, the described resource (subject) and the value set (object)
		Node subject = quad.getSubject();
		Node predicate = quad.getPredicate();
		Node object = quad.getObject();
		
		// Check if the property of the quad is known to provide licensing information
		if(predicate != null && predicate.isURI() && subject != null) {
			// Search for the predicate's URI in the set of license properties...
			if(setSignatureProperties.contains(predicate.getURI())) {
				// Yes, this quad provides licensing information, store the subject's URI (or ID) in the map of resources having a license
				String curSubjectURI = ((subject.isURI())?(subject.getURI()):(subject.toString()));
				logger.trace("Quad providing authenticity of the dataset info detected. Subject: {}, object: {}", curSubjectURI, object);
				
				mapSignatureResources.put(curSubjectURI, object);
			}
		}
	}

	/**
	 * Returns the current value of the Machine-readable indication of a authenticity of the dataset metric in milliseconds, the value of the metric will be 1, 
	 * if the dataset containing the processed quads contains an annotation providing information about its provenance. 0 otherwise.
	 * @return Current value of the Machine-readable indication of a license metric, measured for the whole dataset. [Range: 0 or 1. Error: -1]
	 */
	@Override
	public double metricValue() {
		if(mapSignatureResources != null) {
			// Check that the dataset has provenance information. 
			if(mapSignatureResources.size() > 0) {
				return 1;
			}
		}
		return 0;
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
	 * @return the metricValue
	 */
	public double getMetricValue() {
		return metricValue;
	}

	/**
	 * @param metricValue the metricValue to set
	 */
	public void setMetricValue(double metricValue) {
		this.metricValue = metricValue;
	}

}
