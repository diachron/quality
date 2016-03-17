package de.unibonn.iai.eis.diachron.ebi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.OWL;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.semantics.EBIQM;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;

/**
 * @author Jeremy Debattista
 * @category EBI
 * 
 * Provides a measure for an Ontology checking the usage of 
 * <pobo:def> in defined classes.
 * 
 */
public class POBODefinitionUsage implements QualityMetric{

	private final Resource METRIC_URI = EBIQM.POBODefinitionUsageMetric;
	
	private int entities = 0;
	private Map<Node, Set<String>> defMap = new HashMap<Node, Set<String>>();
	
	private final String DEF_TERM = "http://purl.obolibrary.org/obo/def";
	
	
	public void compute(Quad quad) {
		if ((quad.getObject().isURI()) && (quad.getObject().getURI().equals(OWL.Class.getURI()))){
			if (!(this.defMap.containsKey(quad.getSubject()))) this.defMap.put(quad.getSubject(), new HashSet<String>());
			entities++;
		}
		if (quad.getPredicate().getURI().equals(DEF_TERM)){
			if (!(this.defMap.containsKey(quad.getSubject()))) this.defMap.put(quad.getSubject(), new HashSet<String>());
			
			Set<String> def = this.defMap.get(quad.getSubject());
			def.add(quad.getObject().getLiteralValue().toString());
			this.defMap.put(quad.getSubject(), def);
		}
	}

	/**
	 * The metric value is calculated by checking if there is at least 1 definition term for every class entity 
	 * @return metric value
	 */
	
	public double metricValue() {
		int entitiesWithoutTerms = 0;
		for (Node n : defMap.keySet()){
			entitiesWithoutTerms += (this.defMap.get(n).size() > 0) ? 1 : 0;
		}
		return  (double) entitiesWithoutTerms / (double) entities;
	}

	
	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	
	public ProblemList<?> getQualityProblems() {
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
