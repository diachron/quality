package de.unibonn.iai.eis.diachron.qualitymetrics.representational.understandability;

import java.util.HashMap;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;

/**
 * @author jeremy
 *
 * This measures the percentage of entities having an rdfs:label or rdfs:comment
 */
public class HumanReadableLabelling extends AbstractQualityMetric {

	private final Resource METRIC_URI = DQM.HumanReadableLabellingMetric;
	
	private HashMap<Resource, Integer> entities = new HashMap<Resource, Integer>();
	
	/**
	 * Each entity is checked for a Human Readable label <rdfs:comment> or <rdfs:label>.
	 * In this metric we are assuming that each entity has exactly 1 comment and/or label,
	 * thus we are not checking for contradicting labeling or commenting of entities.
	 */
	@Override
	public void compute(Quad quad) {
		if (quad.getPredicate().getURI().equals(RDF.type)){
			// we've got an instance!
			if (!(entities.containsKey(quad.getSubject()))) { // an instance might have more than 1 type defined
				entities.put((Resource) quad.getSubject(), 0);
			}
			return;
		}
	
		if ( (quad.getPredicate().getURI().equals(RDFS.label)) || (quad.getPredicate().getURI().equals(RDFS.comment))){
			// we'll check if the provider is cheating and is publishing empty string labels and comments
			if (!(quad.getObject().getLiteralValue().equals(""))) entities.put((Resource) quad.getSubject(), 1);
		}
	}

	@Override
	public double metricValue() {
		double entities = 0.0;
		double humanLabels = 0.0;
		for (Resource r : this.entities.keySet()){
			entities+=1;
			humanLabels += this.entities.get(r);
		}
		return entities/humanLabels;
	}

	@Override
	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		// TODO Auto-generated method stub
		return null;
	}

	
}
