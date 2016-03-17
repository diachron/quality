package eu.diachron.qualitymetrics.accessibility.licensing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mapdb.HTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.Pair;
import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.semantics.DQMPROB;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.VOID;

/**
 * @author Santiago Londono
 * Verifies whether consumers of the dataset are explicitly granted permission to re-use it, under defined 
 * conditions, by annotating the resource with a machine-readable indication (e.g. a VoID description) of the license.
 * 
 * Edits
 * [30/03/2015: Jeremy Debattista] - A dataset should have its licence attached to a void:Dataset description
 * 
 * TODO: we should also ensure that if no void:Dataset is available, we look into local resources and check if 
 * they have some licencing information as described in Issue XIV (Hogan et al.)
 * 
 */
public class MachineReadableLicense implements QualityMetric {
	
	private final Resource METRIC_URI = DQM.MachineReadableLicenseMetric;
	
	private static Logger logger = LoggerFactory.getLogger(MachineReadableLicense.class);
	
	/**
	 * Map containing all the resources for which an annotation about their license has been found in the quads.
	 * The key of the map corresponds to the URI of the resource (i.e. subject in the quads) and the value contains the 
	 * object node containing the information about the license
	 */
	private HTreeMap<String, Pair<String,String>> mapLicensedResources = MapDbFactory.createFilesystemDB().createHashMap("machine-licence").make();
	
	/**
	 * Holds the URI corresponding to the base uri of the assessed dataset. 
	 */
	private String baseURI = EnvironmentProperties.getInstance().getBaseURI();
	
	/**
	 * Allows to determine if a predicate states what is the licensing schema of a resource
	 */
	private LicensingModelClassifier licenseClassifier = new LicensingModelClassifier();
	
	/**
	 * Mapping all licenses that are attached to a void:Dataset
	 */
	private Map<String, Node> mapLicensedDatasets = new HashMap<String, Node>();
	
	
	/**
	 * Holds the local resource URIs seen
	 */
	private Set<String> localURIs =  MapDbFactory.createFilesystemDB().createHashSet("local-uris").make();
	
	
	private List<Quad> _problemList = new ArrayList<Quad>();
	
	
	/**
	 * Processes a single quad being part of the dataset. Firstly, tries to figure out the URI of the dataset whence the quads come. 
	 * If so, the URI is extracted from the corresponding subject and stored to be used in the calculation of the metric. Otherwise, verifies 
	 * whether the quad contains licensing information (by checking if the property is part of those known to be about licensing) and if so, stores 
	 * the URL of the subject in the map of resources confirmed to have licensing information
	 * @param quad Quad to be processed and examined to try to extract the dataset's URI
	 */
	public void compute(Quad quad) {
		logger.debug("Computing : {} ", quad.asTriple().toString());

		// Extract the predicate (property) of the statement, the described resource (subject) and the value set (object)
		Node subject = quad.getSubject();
		Node predicate = quad.getPredicate();
		Node object = quad.getObject();
		
		if (object.matches(VOID.Dataset.asNode())){
			if (subject.isURI()){
				if (subject.getURI().startsWith(this.baseURI)){
					Node licence = ModelFactory.createDefaultModel().createResource().asNode();
					if(this.mapLicensedResources.containsKey(subject.getURI())){
						licence = ModelFactory.createDefaultModel().createResource(this.mapLicensedResources.get(subject.getURI()).getSecondElement()).asNode();
						this.mapLicensedResources.remove(subject.getURI());
					}
					if (!(this.mapLicensedDatasets.containsKey(subject.getURI()))) 
						mapLicensedDatasets.put(subject.getURI(), licence);
				}
			}
		}
		
		if (subject.isURI()){
			if (subject.getURI().startsWith(this.baseURI)){
				if(licenseClassifier.isLicensingPredicate(predicate)) {
					// Yes, this quad provides licensing information, store the subject's URI (or ID) in the map of resources having a license
					logger.trace("Quad providing license info detected. Subject: {}, object: {}", subject.getURI(), object);
					
					if(this.mapLicensedDatasets.containsKey(subject.getURI())){
						this.mapLicensedDatasets.put(subject.getURI(), object);
					} else {
						mapLicensedResources.put(subject.getURI(), new Pair<String,String>(EnvironmentProperties.getInstance().getDatasetURI(), object.toString()));
					}
				}
				localURIs.add(subject.getURI());
			}
		}
	}

	/**
	 * Returns the current value of the Machine-readable indication of a license metric, the value of the metric will be 1, 
	 * if the dataset containing the processed quads contains an annotation providing information about its license. 0 otherwise.
	 * @return Current value of the Machine-readable indication of a license metric, measured for the whole dataset. [Range: 0 or 1. Error: -1]
	 */
	public double metricValue() {
		
		double voidValidLicences = 0.0;
		
		//check the licences per void dataset
		for(String voidDS : this.mapLicensedDatasets.keySet()){
			Node dataset = ModelFactory.createDefaultModel().createResource(voidDS).asNode();
			Node licence = this.mapLicensedDatasets.get(voidDS);
			if (licence != null){
				if (hasValidLicence(dataset,licence)) voidValidLicences++;
			} else {
				Quad q = new Quad(null, dataset, QPRO.exceptionDescription.asNode(), DQMPROB.NoValidLicenceInDataset.asNode());
				this._problemList.add(q);
			}
		}
		
		//TODO: check the licences per resource
//		double resourceValidLicences = 0.0;
//		for(String resURI : this.mapLicensedResources.keySet()){
//			Pair<String,Node> pair = this.mapLicensedResources.get(resURI);
//			
//			Node resource = ModelFactory.createDefaultModel().createResource(resURI).asNode();
//			Node licence = pair.getSecondElement();
//			
//			//if there exists a void:Dataset but no licence attachment then we need to check all resources
//			
//			if (this.mapLicensedDatasets.containsKey(pair.getFirstElement()) && (this.mapLicensedDatasets.get(pair.getFirstElement()) == null)){
//				if (hasValidLicence(resource,licence)) resourceValidLicences++;
//				
//			} else if (!this.mapLicensedDatasets.containsKey(pair.getFirstElement())){
//				if (hasValidLicence(resource,licence)) resourceValidLicences++;
//			} else {
//				this.localURIs.remove(resURI);
//			}
//		}
		
		//calculating the metric
		double metValue = (voidValidLicences) / ((double)this.mapLicensedDatasets.size());
		
		statsLogger.info("MachineReadableLicense. Dataset: {} - Total # Licenses in Dataset : {}; # VOID Valid Licenses : {};", 
				EnvironmentProperties.getInstance().getDatasetURI(), mapLicensedDatasets.size(), voidValidLicences);
		
		return metValue;
	}
	
	private boolean hasValidLicence(Node dataset, Node licence){
		if (licenseClassifier.isCopyLeftLicenseURI(licence)) return true;
		else if (licenseClassifier.isNotRecommendedCopyLeftLicenseURI(licence)){
			Quad q = new Quad(null, dataset, QPRO.exceptionDescription.asNode(), DQMPROB.NotRecommendedLicenceInDataset.asNode());
			this._problemList.add(q);
			return true;
		} else {
			Quad q = new Quad(null, dataset, QPRO.exceptionDescription.asNode(), DQMPROB.NoValidLicenceInDataset.asNode());
			this._problemList.add(q);
		}
		return false;
	}

	public Resource getMetricURI() {
		return METRIC_URI;
	}

	@Override
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
		return false;
	}

	@Override
	public Resource getAgentURI() {
		return 	DQM.LuzzuProvenanceAgent;
	}
	
}
