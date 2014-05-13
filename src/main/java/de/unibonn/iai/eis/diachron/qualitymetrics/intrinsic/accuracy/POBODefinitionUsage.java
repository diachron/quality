package de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.accuracy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.OWL;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;

/**
 * @author jeremy
 *
 * Provides a measure for an Ontology checking the usage of 
 * <pobo:def> in defined classes.
 * 
 * This is only required for the EBI use case
 */
public class POBODefinitionUsage extends AbstractQualityMetric{

	private final Resource METRIC_URI = DQM.POBODefinitionUsageMetric;
	
	private int entities = 0;
	private Map<Node, Set<String>> defMap = new HashMap<Node, Set<String>>();
	
	private final String DEF_TERM = "http://purl.obolibrary.org/obo/def";
	
	@Override
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
	@Override
	public double metricValue() {
		int entitiesWithoutTerms = 0;
		for (Node n : defMap.keySet()){
			entitiesWithoutTerms += (this.defMap.get(n).size() > 0) ? 1 : 0;
		}
		return  (double) entitiesWithoutTerms / (double) entities;
	}

	@Override
	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		return null;
	}

}
