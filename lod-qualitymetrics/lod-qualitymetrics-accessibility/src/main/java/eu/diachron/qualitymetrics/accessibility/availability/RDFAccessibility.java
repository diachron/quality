/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.availability;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.semantics.DQMPROB;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.VOID;
import eu.diachron.qualitymetrics.utilities.AbstractQualityMetric;

/**
 * @author Jeremy Debattista
 * 
 * Check if ALL data dumps (void:dataDump) exist, are reachable and parsable.
 *     
 */
public class RDFAccessibility extends AbstractQualityMetric {
	
	static Logger logger = LoggerFactory.getLogger(RDFAccessibility.class);
	
	protected List<Quad> problemList = new ArrayList<Quad>();

	private final Resource METRIC_URI = DQM.RDFAvailabilityMetric;
	
	private double metricValue = 0.0d;
	
	private double totalDataDumps = 0.0d;
	

	public void compute(Quad quad) {
		logger.debug("Computing : {} ", quad.asTriple().toString());

		if (quad.getPredicate().getURI().equals(VOID.dataDump.getURI())) {
			totalDataDumps++;
		}
	}

	public double metricValue() {
		metricValue = (totalDataDumps > 0) ? 1.0 : 0.0;
		statsLogger.info("RDFAccessibility. Dataset: {} - Total # Datadumps : {};", this.getDatasetURI(), totalDataDumps);
		
		return metricValue;
	}


	public Resource getMetricURI() {
		return this.METRIC_URI;
	}
	
	@Override
	public ProblemList<?> getQualityProblems() {
		ProblemList<Quad> tmpProblemList = null;
		
		if (this.metricValue() == 0){
			String resource = this.getDatasetURI();
			Resource subject = ModelFactory.createDefaultModel().createResource(resource);
			Quad q = new Quad(null, subject.asNode() , QPRO.exceptionDescription.asNode(), DQMPROB.NoRDFAccessibility.asNode());
			this.problemList.add(q);
		}
		
		try {
			if(this.problemList != null && this.problemList.size() > 0) {
				tmpProblemList = new ProblemList<Quad>(this.problemList);
			} else {
				tmpProblemList = new ProblemList<Quad>();
			}
		} catch (ProblemListInitialisationException problemListInitialisationException) {
			//TODO
		}
		return tmpProblemList;
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
