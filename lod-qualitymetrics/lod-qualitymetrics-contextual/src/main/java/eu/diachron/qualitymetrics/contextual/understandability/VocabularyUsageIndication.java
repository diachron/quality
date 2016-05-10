/**
 * 
 */
package eu.diachron.qualitymetrics.contextual.understandability;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.semantics.DQMPROB;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.VOID;

/**
 * @author Jeremy Debattista
 * 
 */
public class VocabularyUsageIndication implements QualityMetric {

	final static Logger logger = LoggerFactory.getLogger(VocabularyUsageIndication.class);

	private Set<String> differentNamespacesUsed = new HashSet<String>();
	private Set<String> namespacesIndicated = new HashSet<String>();

	private boolean calculated = false;
	private double value  = 0.0d;
	
	private List<Quad> _problemList = new ArrayList<Quad>();

	
	
	@Override
	public void compute(Quad quad) {
		Node predicate = quad.getPredicate();
		Node object = quad.getObject();
		
		differentNamespacesUsed.add(predicate.getNameSpace());
		if (object.isURI()) differentNamespacesUsed.add(object.getNameSpace());
		
		if (predicate.getURI().equals(VOID.vocabulary.getURI())) namespacesIndicated.add(object.getNameSpace());
	}

	@Override
	public double metricValue() {
		
		if (!calculated){
			calculated = true;
			
			double totalDiffNs = differentNamespacesUsed.size();
			double totalNsInd = namespacesIndicated.size();
			
			
			Set<String> _ns = new HashSet<String>();
			_ns.addAll(namespacesIndicated);
			
			_ns.removeAll(differentNamespacesUsed);
			
			double totalIndicated = totalNsInd - _ns.size() ; //making sure that if a NS was indicated but never used, is not part of the value
			
			statsLogger.info("Dataset: {} - Total # NS used : {}; # NS indicated by void : {} # NS indicated : {};"
					, EnvironmentProperties.getInstance().getDatasetURI(), totalDiffNs, totalNsInd, totalIndicated); //TODO: these store in a seperate file

		
			value = (double)totalIndicated/(double)totalDiffNs;
			
			
			//for problem report
			differentNamespacesUsed.removeAll(namespacesIndicated);
			
			for(String s : differentNamespacesUsed) this.createProblemQuad(s);
			
		}
		
		
		return value;
	}

	@Override
	public Resource getMetricURI() {
		return DQM.VocabularyUsageIndication;
	}

	private void createProblemQuad(String resource){
		Quad q = new Quad(null, ModelFactory.createDefaultModel().createResource(resource).asNode(), QPRO.exceptionDescription.asNode(), DQMPROB.NoVocabularyIndication.asNode());
		this._problemList.add(q);
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
		return DQM.LuzzuProvenanceAgent;
	}

}
