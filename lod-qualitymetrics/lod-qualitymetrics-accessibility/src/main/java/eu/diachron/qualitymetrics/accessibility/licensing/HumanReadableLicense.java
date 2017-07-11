package eu.diachron.qualitymetrics.accessibility.licensing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDFS;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.semantics.DQMPROB;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.utilities.AbstractQualityMetric;

/**
 * @author Santiago Londono
 * Verifies whether a human-readable text, stating the of licensing model attributed to the resource, has been provided as part of the dataset.
 * In contrast with the Machine-readable Indication of a License metric, this one looks for objects containing literal values and 
 * analyzes the text searching for key, licensing related terms. Also, additional to the license related properties this metric examines comment 
 * properties such as rdfs:label, dcterms:description, rdfs:comment.
 */
public class HumanReadableLicense extends AbstractQualityMetric {
	
	private final Resource METRIC_URI = DQM.HumanReadableLicenseMetric;
	
	private static Logger logger = LoggerFactory.getLogger(HumanReadableLicense.class);
	
	/**
	 * Determines if an object contains a human-readable license
	 */
	private LicensingModelClassifier licenseClassifier = new LicensingModelClassifier();
	
	
	private List<Quad> _problemList = new ArrayList<Quad>();


//	private double validLicenses = 0.0d;
//	private double totalPossibleLicenses = 0.0d;
	
	private boolean hasValidLicense = false;
	
	private static HashSet<String> setLicensingDocumProps;		
	static {		
 		setLicensingDocumProps = new HashSet<String>();		
		setLicensingDocumProps.add(DCTerms.description.getURI());		
		setLicensingDocumProps.add(RDFS.comment.getURI());	
		setLicensingDocumProps.add(RDFS.label.getURI());
		setLicensingDocumProps.add("http://schema.org/description");
		setLicensingDocumProps.add("http://www.w3.org/2004/02/skos/core#altLabel");
	}		

	
	/**
	 * Processes a single quad being part of the dataset. Detect triples containing as subject, the URI of the resource and as 
	 * predicate one of the license properties listed on the previous metric, or one of the documentation properties (rdfs:label, 
	 * dcterms:description, rdfs:comment) when found, evaluates the object contents to determine if it matches the features expected on 
	 * a licensing statement.
	 * @param quad Quad to be processed and examined to try to extract the text of the licensing statement
	 */
	public void compute(Quad quad) {
		logger.debug("Computing : {} ", quad.asTriple().toString());

		// Extract the predicate (property) of the statement, the described resource (subject) and the value set (object)
		Node subject = quad.getSubject();
		Node predicate = quad.getPredicate();
		Node object = quad.getObject();
		
		if ((subject.isURI()) && (subject.getURI().startsWith(this.getDatasetURI()))){
			if ((licenseClassifier.isLicensingPredicate(predicate)) || (setLicensingDocumProps.contains(predicate.getURI()))) {
				if (object.isLiteral()){
					if (licenseClassifier.isLicenseStatement(object)){
						this.hasValidLicense = true;
					}
				}
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
		double metValue = (this.hasValidLicense) ? 1.0d : 0.0d;
		
		if (!this.hasValidLicense){
			Quad q = new Quad(null, 
					ModelFactory.createDefaultModel().createResource(this.getDatasetURI()).asNode(), 
					QPRO.exceptionDescription.asNode(), 
					DQMPROB.NoValidLicenceInDatasetForHumans.asNode());
			this._problemList.add(q);
		}
		
		statsLogger.info("HumanReadableLicense. Dataset: {} - Has Valid License : {}", 
				this.getDatasetURI(), this.hasValidLicense);
		
		return metValue;
	}
	
	
//	private boolean hasValidLicence(Node dataset, Node licence){
//		if (licenseClassifier.isCopyLeftLicenseURI(licence)) return true;
//		else if (licenseClassifier.isNotRecommendedCopyLeftLicenseURI(licence)){
//			Quad q = new Quad(null, dataset, QPRO.exceptionDescription.asNode(), DQMPROB.NotRecommendedLicenceInDatasetForHumans.asNode());
//			this._problemList.add(q);
//			return true;
//		} else {
//			Quad q = new Quad(null, dataset, QPRO.exceptionDescription.asNode(), DQMPROB.NoValidLicenceInDatasetForHumans.asNode());
//			this._problemList.add(q);
//		}
//		
//		if (licence.isLiteral()){
//			if (licenseClassifier.isLicenseStatement(licence)) return true;
//			else if (licenseClassifier.isNotRecommendedLicenseStatement(licence)){
//				Quad q = new Quad(null, dataset, QPRO.exceptionDescription.asNode(), DQMPROB.NotRecommendedLicenceInDatasetForHumans.asNode());
//				this._problemList.add(q);
//				return true;
//			} 
//			else {
//				Quad q = new Quad(null, dataset, QPRO.exceptionDescription.asNode(), DQMPROB.NoValidLicenceInDatasetForHumans.asNode());
//				this._problemList.add(q);
//				return false;
//			}
//		}
//		return false;
//	}

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
