/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.licensing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import eu.diachron.qualitymetrics.utilities.AbstractQualityMetric;

/**
 * @author Jeremy Debattista
 * 
 */
public class CorrectLicenceReattribution extends AbstractQualityMetric {

	//The idea behind this metric is to (1) find the licenses used in the 
	//dataset being assessed. (2) check in the void description for void:target
	//predicate. That would mean that the dataset under assessment is a subset 
	//of some other dataset and thus appropriate attribution is needed.
	//(3) if resources with subject PLD different than the base URI, then 
	//we need to check if the "original dataset" has some sort of metadata,
	//though the main problem here is that a "copied" resource in the assessed
	//dataset might not have the same licensing attribution or provenance information
	//as required*. We might need to check the DWBP UC
	
	//*steps:
	//for each resource with a subject URI (X) is different than then base URI
	// 1) find if there is any matching PROV information (such as dct:source) with X as the subject 
	// 2) find if there is any matching license predicates with X as the subject
	// 3) if none of the above, then the "copied" resource might be missing attribution
	// 4) else we have to match the license or prov information with the original dataset!
	
	//http://2014.eswc-conferences.org/sites/default/files/papers/paper_168.pdf
	
	final static Logger logger = LoggerFactory.getLogger(CorrectLicenceReattribution.class);
	
	/* (non-Javadoc)
	 * @see de.unibonn.iai.eis.luzzu.assessment.QualityMetric#compute(com.hp.hpl.jena.sparql.core.Quad)
	 */
	@Override
	public void compute(Quad quad) {
		logger.debug("Computing : {} ", quad.asTriple().toString());
	}

	/* (non-Javadoc)
	 * @see de.unibonn.iai.eis.luzzu.assessment.QualityMetric#metricValue()
	 */
	@Override
	public double metricValue() {
		statsLogger.info("CorrectLicenceReattribution. Dataset: {} - Metric not implemented;", 
				EnvironmentProperties.getInstance().getDatasetURI());
		
		return 0;
	}

	/* (non-Javadoc)
	 * @see de.unibonn.iai.eis.luzzu.assessment.QualityMetric#getMetricURI()
	 */
	@Override
	public Resource getMetricURI() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.unibonn.iai.eis.luzzu.assessment.QualityMetric#getQualityProblems()
	 */
	@Override
	public ProblemList<?> getQualityProblems() {
		// TODO Auto-generated method stub
		return null;
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