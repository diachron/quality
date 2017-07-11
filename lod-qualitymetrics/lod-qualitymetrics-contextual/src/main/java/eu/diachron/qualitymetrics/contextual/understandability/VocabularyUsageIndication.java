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

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.RDF;

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
 * This metric checks whether vocabularies used in the datasets (ie. predicate or object if predicate is rdf:type) 
 * are indicated in the dataset's metadata, specifically using the void:vocabulary predicate 
 * 
 */
public class VocabularyUsageIndication extends AbstractQualityMetric {

	final static Logger logger = LoggerFactory.getLogger(VocabularyUsageIndication.class);

	private Set<String> differentNamespacesUsed = new HashSet<String>();
	private Set<String> namespacesIndicated = new HashSet<String>();

	private boolean calculated = false;
	private double value  = 0.0d;
	
	private List<Quad> _problemList = new ArrayList<Quad>();

	private Set<String> nsIgnore = new HashSet<String>();
	{
		nsIgnore.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		nsIgnore.add("http://www.w3.org/2002/07/owl#");
		nsIgnore.add("http://rdfs.org/ns/void#");
		nsIgnore.add("http://www.w3.org/2000/01/rdf-schema#");
	}
	
	
	@Override
	public void compute(Quad quad) {
		Node predicate = quad.getPredicate();
		Node object = quad.getObject();
		
		differentNamespacesUsed.add(predicate.getNameSpace());
		if (predicate.getURI().equals(RDF.type.getURI())){
			if (object.isURI()) differentNamespacesUsed.add(object.getNameSpace());
		}
		
		if (predicate.getURI().equals(VOID.vocabulary.getURI())) namespacesIndicated.add(object.getURI());
	}

	@Override
	public double metricValue() {
		
		if (!calculated){
			
			calculated = true;
			
			double totalDiffNs = differentNamespacesUsed.size();
			double totalNsInd = namespacesIndicated.size();
			
			SetView<String> view = Sets.intersection(differentNamespacesUsed, namespacesIndicated); // view of indicated and used
			
			
			statsLogger.info("Dataset: {} - Total # NS used : {}; # NS indicated by void : {} # NS used vis-a-vie indicated : {};"
					, this.getDatasetURI(), totalDiffNs, totalNsInd, view.size()); //TODO: these store in a seperate file

		
			if (view.size() == 0) value = 0.0;
			else if (totalDiffNs == 0) value = 0.0;
			else value = (double)view.size()/(double)totalDiffNs;
			
			
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
