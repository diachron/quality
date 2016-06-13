package eu.diachron.qualitymetrics.accessibility.licensing;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.OWL;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.semantics.DQMPROB;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.semantics.utilities.Commons;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.utilities.AbstractQualityMetric;

/**
 * @author Santiago Londono
 * Verifies whether consumers of the dataset are explicitly granted permission to re-use it, under defined 
 * conditions, by annotating the resource with a machine-readable indication (e.g. a VoID description) of the license.
 *  
 */
public class MachineReadableLicense extends AbstractQualityMetric {
	
	private final Resource METRIC_URI = DQM.MachineReadableLicenseMetric;
	
	private static Logger logger = LoggerFactory.getLogger(MachineReadableLicense.class);
	
	//TODO: Add in DQMPROB
    private static final Resource NotMachineReadableLicense = ModelFactory.createDefaultModel().createResource( "http://www.diachron-fp7.eu/dqm-prob#NotMachineReadableLicence" );

	
	/**
	 * Map containing all the resources for which an annotation about their license has been found in the quads.
	 * The key of the map corresponds to the URI of the resource (i.e. subject in the quads) and the value contains the 
	 * object node containing the information about the license
	 */
//	private Map<String, Node> mapLicensedResources = new HashMap<String, Node>();
	
	
	/**
	 * Allows to determine if a predicate states what is the licensing schema of a resource
	 */
	private LicensingModelClassifier licenseClassifier = new LicensingModelClassifier();
	
	/**
	 * Mapping all licenses that are attached to a void:Dataset
	 */
//	private Map<String, Node> mapLicensedDatasets = new HashMap<String, Node>();
	
	
	/**
	 * Holds the local resource URIs seen
	 */
	private List<Quad> _problemList = new ArrayList<Quad>();
	
	
	
	private double validLicenses = 0.0d;
	private double totalPossibleLicenses = 0.0d;
	private double nonMachineReadableLicenses = 0.0d;

	
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
		
		if ((subject.isURI()) && (subject.getURI().startsWith(this.getDatasetURI()))){
			if (licenseClassifier.isLicensingPredicate(predicate)) {
				totalPossibleLicenses++;
				
				//is it semantic resource?
				Model licenseModel = ModelFactory.createDefaultModel();
				try{
					licenseModel = RDFDataMgr.loadModel(object.getURI());
				} catch (Exception e) {
					Quad q = new Quad(null, subject, QPRO.exceptionDescription.asNode(), NotMachineReadableLicense.asNode());
					this._problemList.add(q);
					nonMachineReadableLicenses++;
				}
				if (licenseModel.size() > 0){
					// is there an owl:sameAs
					NodeIterator itr = licenseModel.listObjectsOfProperty(Commons.asRDFNode(object).asResource(), OWL.sameAs);
					boolean isValidLicense = false;
					while(itr.hasNext()){
						RDFNode possLicense = itr.next();
						
						if (licenseClassifier.isCopyLeftLicenseURI(possLicense.asNode())){
							isValidLicense = true;
							break;
						}
						
						if (licenseClassifier.isNotRecommendedCopyLeftLicenseURI(possLicense.asNode())){
							Quad q = new Quad(null, subject, QPRO.exceptionDescription.asNode(), DQMPROB.NotRecommendedLicenceInDataset.asNode());
							this._problemList.add(q);
							isValidLicense = true;
							break;
						}
					}
					if (isValidLicense) validLicenses++;
				}
			}
		}
	}

	/**
	 * Returns the current value of the Machine-readable indication of a license metric, the value of the metric will be 1, 
	 * if the dataset containing the processed quads contains an annotation providing information about its license. 0 otherwise.
	 * @return Current value of the Machine-readable indication of a license metric, measured for the whole dataset. [Range: 0 or 1. Error: -1]
	 */
	public double metricValue() {
		double metValue = 0.0d;
		
		
		if ((totalPossibleLicenses == 0) || (validLicenses == 0)) metValue = 0.0d;
		else metValue = (double)validLicenses / ((double)totalPossibleLicenses);
		
		statsLogger.info("MachineReadableLicense. Dataset: {} - Total # Licenses in Dataset : {}; # Total Machine Readable License : {}; Total Non-Machine Readable Licenses: {}", 
				this.getDatasetURI(), totalPossibleLicenses, validLicenses, nonMachineReadableLicenses);
		
		return metValue;
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
