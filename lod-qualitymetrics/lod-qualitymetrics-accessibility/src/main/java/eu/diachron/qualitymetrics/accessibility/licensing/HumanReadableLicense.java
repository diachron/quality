package eu.diachron.qualitymetrics.accessibility.licensing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ResourceBaseURIOracle;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;

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
//	private Set<String> setLicensedURIs = Collections.synchronizedSet(new HashSet<String>());
	private Map<String,Node> possibleHumanReadableLicense = new ConcurrentHashMap<String,Node>();
	
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
	
	private List<Quad> _problemList = new ArrayList<Quad>();

	
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
		
		// Check whether the predicate corresponds to a documentation property...  
		if(setLicensingDocumProps.contains(predicate.getURI())) {
			logger.debug("Evaluating human-readable license candidate: {} with object: {}", predicate, object);
			
			// ... and check if the object contains text recognized to be of a human-readable license
			if(this.licenseModClassifier.isLicenseStatement(object) && subject.isURI()) {
				this.possibleHumanReadableLicense.put(subject.getURI(),object);
				logger.debug("Human-readable license detected for subject: {}", subject);
			} 
			else if (this.licenseModClassifier.isNotRecommendedLicenseStatement(object) && subject.isURI()) {
				// we should also check if the licence used is a non-recommended one
				this.possibleHumanReadableLicense.put(subject.getURI(),object);
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
		Node dataset = ModelFactory.createDefaultModel().createResource(resourceBaseURI).asNode();

		if(resourceBaseURI != null) {
			if (this.possibleHumanReadableLicense.containsKey(resourceBaseURI)){
				Node n = this.possibleHumanReadableLicense.get(resourceBaseURI);
				return hasValidLicence(dataset,n);
			} else {
				Quad q = new Quad(null, dataset, QPRO.exceptionDescription.asNode(), DQM.NoValidLicenceInDatasetForHumans.asNode());
				this._problemList.add(q);
				return 0.0;
			}
		}
		
		//this should not happen, but at worse!
		Quad q = new Quad(null, dataset, QPRO.exceptionDescription.asNode(), DQM.NoValidLicenceInDatasetForHumans.asNode());
		this._problemList.add(q);
		return 0.0;
	}
	
	private double hasValidLicence(Node dataset, Node literalObject){
		if (licenseModClassifier.isLicenseStatement(literalObject)) return 1.0;
		else if (licenseModClassifier.isNotRecommendedLicenseStatement(literalObject)){
			Quad q = new Quad(null, dataset, QPRO.exceptionDescription.asNode(), DQM.NotRecommendedLicenceInDatasetForHumans.asNode());
			this._problemList.add(q);
			return 1.0;
		} 
		else {
			Quad q = new Quad(null, dataset, QPRO.exceptionDescription.asNode(), DQM.NoValidLicenceInDatasetForHumans.asNode());
			this._problemList.add(q);
			return 0.0;
		}
	}

	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	public ProblemList<?> getQualityProblems() {
		ProblemList<Quad> pl = null;
		try {
			pl = new ProblemList<Quad>(this._problemList);
		} catch (ProblemListInitialisationException e) {
//			logger.debug(e.getStackTrace());
			logger.error(e.getMessage());
		}
		return pl;	
	}

}