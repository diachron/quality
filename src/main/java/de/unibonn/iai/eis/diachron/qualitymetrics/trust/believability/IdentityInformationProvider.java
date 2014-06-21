package de.unibonn.iai.eis.diachron.qualitymetrics.trust.believability;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DCTerms;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;

/**
 * @author Carlos Montoya Verifies whether consumers of the dataset are
 *         explicitly granted permission to re-use it, under defined conditions,
 *         by annotating the resource with a machine-readable indication (e.g. a
 *         VoID description) of the license.
 */
public class IdentityInformationProvider extends AbstractQualityMetric {

	private final Resource METRIC_URI = DQM.IdentityInformationProviderMetric;

	private static Logger logger = LoggerFactory.getLogger(IdentityInformationProvider.class);

	/**
	 * Set of all the URIs of properties known to provide information related
	 * with the provider and/or contributors
	 */
	private static HashSet<String> setProvenanceProperties;

	static {
		// Initialize set of properties known to provide licensing information
		setProvenanceProperties = new HashSet<String>();
		setProvenanceProperties.add(DCTerms.creator.getURI());
		setProvenanceProperties.add(DCTerms.contributor.getURI());
		setProvenanceProperties.add(DCTerms.publisher.getURI());
	}

	/**
	 * Map containing all the resources for which an annotation about their
	 * provenance has been found in the quads. The key of the map corresponds to
	 * the URI of the resource (i.e. subject in the quads) and the value
	 * contains the object node containing the information about the license
	 */
	private ConcurrentHashMap<String, Node> mapProvenanceResources = new ConcurrentHashMap<String, Node>();

	/**
	 * Processes a single quad being part of the dataset. First it try to figure
	 * if the quad contain information regarding the title if yes then it store
	 * the info in the variable provided to it If not then it try to whether the
	 * quad contains content information (by checking if the property is part of
	 * those known to be about the content of the dataset information) and if
	 * so, stores in the variable If not then it try to whether the quad
	 * contains homepage information (by checking if the property is part of
	 * those known to be about the homepage of the dataset information) and if
	 * so, stores in the variable
	 * 
	 * @param quad
	 *            Quad to be processed and examined to try to extract the
	 *            dataset's URI
	 */
	@Override
	public void compute(Quad quad) {
		// Extract the predicate (property) of the statement, the described
		// resource (subject) and the value set (object)
		Node subject = quad.getSubject();
		Node predicate = quad.getPredicate();
		Node object = quad.getObject();

		//Falta crear el archivo que contenga el listado de trust providers.
		
		
		// Check if the property of the quad is known to provide licensing
		// information
		if (predicate != null && predicate.isURI() && subject != null) {
			// Search for the predicate's URI in the set of license
			// properties...
			if (setProvenanceProperties.contains(predicate.getURI())) {
				// Yes, this quad provides licensing information, store the
				// subject's URI (or ID) in the map of resources having a
				// license
				String curSubjectURI = ((subject.isURI()) ? (subject.getURI())
						: (subject.toString()));
				logger.trace(
						"Quad providing authenticity of the dataset info detected. Subject: {}, object: {}",
						curSubjectURI, object);

				mapProvenanceResources.put(curSubjectURI, object);
			}
		}
	}

	/**
	 * Returns the current value of the Machine-readable indication of a
	 * believability of the dataset metric in milliseconds, the value of the
	 * metric will be 1, if the dataset containing the processed quads contains
	 * an annotation providing information about its provenance. 0 otherwise.
	 * 
	 * @return Current value of the Machine-readable indication of a license
	 *         metric, measured for the whole dataset. [Range: 0 or 1. Error:
	 *         -1]
	 */
	@Override
	public double metricValue() {
		// Check if the dataset contain all the information related with its
		// provenance if it has title, the content and the home URL then it
		// return 1 else return 0.
		
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

}
