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
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
/**
 * @author Jeremy Debattista
 * @category EBI
 *
 * Measures the number of classes which has a synonym <efo:alternative_term> 
 * described.
 * 
 * This is only required for the EBI use case
 */
public class SynonymUsage extends AbstractQualityMetric {

	private final Resource METRIC_URI = EBIQM.SynonymUsageMetric;
	
	private int entities = 0;
	private Map<Node, Set<String>> altTermsMap = new HashMap<Node, Set<String>>();
	
	private final String ALT_TERM = "http://www.ebi.ac.uk/efo/alternative_term";
	
	
	public void compute(Quad quad) {
		if ((quad.getObject().isURI()) && (quad.getObject().getURI().equals(OWL.Class.getURI())))
		{
			if (!(this.altTermsMap.containsKey(quad.getSubject()))) this.altTermsMap.put(quad.getSubject(), new HashSet<String>());
			entities++;
		}
		
		if (quad.getPredicate().getURI().equals(ALT_TERM)){
			if (!(this.altTermsMap.containsKey(quad.getSubject()))) this.altTermsMap.put(quad.getSubject(), new HashSet<String>());
			
			Set<String> synonoms = this.altTermsMap.get(quad.getSubject());
			if (!(synonoms.contains(quad.getObject().getLiteralValue().toString()))){ // making sure that the same synonom is not repeated in a class
				synonoms.add(quad.getObject().getLiteralValue().toString());
			}
			this.altTermsMap.put(quad.getSubject(), synonoms);
		}
	}

	/**
	 * The metric value is calculated by checking if there is at least 1 synonym term for every class entity 
	 * @return metric value
	 */
	
	public double metricValue() {
		int entitiesWithoutTerms = 0;
		for (Node n : altTermsMap.keySet()){
			entitiesWithoutTerms += (this.altTermsMap.get(n).size() > 0) ? 1 : 0;
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
