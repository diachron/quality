package eu.diachron.qualitymetrics.accessibility.licensing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ResourceBaseURIOracle;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;

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
	 * Allows to determine if a predicate states what is the licensing schema of a resource
	 */
	private LicensingModelClassifier licenseClassifier = new LicensingModelClassifier();
	
	
	/**
	 * We might need to figure out the base URI ourselves if we do not have any voID description
	 */
	private ResourceBaseURIOracle oracle = new ResourceBaseURIOracle();

	
	private List<Quad> _problemList = new ArrayList<Quad>();
	
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
			try {
				dataSetURI = EnvironmentProperties.getInstance().getDatasetURI();
			} catch(Exception ex) {
				logger.error("Error retrieven dataset URI, processor not initialised yet", ex);
				// Try to get the dataset URI from the VOID property, as last resource
				dataSetURI = ResourceBaseURIOracle.extractDatasetURI(quad);
			}
			logger.trace("Trying to get dataset URI, loaded: {}", dataSetURI); 
		}

		//It might be the case that there is no voID description in the dataset or RDF Document, therefore we will try to extract the base URI
		oracle.addHint(quad);
		
		// Check if the property of the quad is known to provide licensing information
		if(predicate != null && predicate.isURI() && subject != null) {
			
			// Search for the predicate's URI in the set of license properties...
			if(licenseClassifier.isLicensingPredicate(predicate)) {
				
				// Yes, this quad provides licensing information, store the subject's URI (or ID) in the map of resources having a license
				String curSubjectURI = ((subject.isURI())?(subject.getURI()):(subject.toString()));
				logger.trace("Quad providing license info detected. Subject: {}, object: {}", curSubjectURI, object);
				
				mapLicensedResources.put(curSubjectURI, object);
			}
		}
	}

	/**
	 * Returns the current value of the Machine-readable indication of a license metric, the value of the metric will be 1, 
	 * if the dataset containing the processed quads contains an annotation providing information about its license. 0 otherwise.
	 * @return Current value of the Machine-readable indication of a license metric, measured for the whole dataset. [Range: 0 or 1. Error: -1]
	 */
	public double metricValue() {
		if(dataSetURI != null) {
			Node dataset = ModelFactory.createDefaultModel().createResource(dataSetURI).asNode();
			if(mapLicensedResources.containsKey(dataSetURI)){
				Node n = mapLicensedResources.get(dataSetURI);
				return hasValidLicence(dataset,n);
			} else {
				Quad q = new Quad(null, dataset, QPRO.exceptionDescription.asNode(), DQM.NoValidLicenceInDataset.asNode());
				this._problemList.add(q);
				return 0.0;
			}
		} else {
			String ds = oracle.getEstimatedResourceBaseURI();
			Node dataset = ModelFactory.createDefaultModel().createResource(ds).asNode();

			if (mapLicensedResources.containsKey(ds)){
				Node n = mapLicensedResources.get(ds);
				return hasValidLicence(dataset,n);
			} else {
				Quad q = new Quad(null, dataset, QPRO.exceptionDescription.asNode(), DQM.NoValidLicenceInDataset.asNode());
				this._problemList.add(q);
				return 0.0;
			}
		}
	}
	
	private double hasValidLicence(Node dataset, Node licence){
		if (licenseClassifier.isCopyLeftLicenseURI(licence)) return 1.0;
		else if (licenseClassifier.isNotRecommendedCopyLeftLicenseURI(licence)){
			Quad q = new Quad(null, dataset, QPRO.exceptionDescription.asNode(), DQM.NotRecommendedLicenceInDataset.asNode());
			this._problemList.add(q);
			return 1.0;
		} 
		else {
			Quad q = new Quad(null, dataset, QPRO.exceptionDescription.asNode(), DQM.NoValidLicenceInDataset.asNode());
			this._problemList.add(q);
			return 0.0;
		}
	}

	public Resource getMetricURI() {
		return METRIC_URI;
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
