package eu.diachron.qualitymetrics.representational.understandability;

import java.util.HashMap;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.semantics.vocabulary.DQM;
/**
 * @author jeremy
 *
 * This measures the percentage of entities having an rdfs:label or rdfs:comment
 */
public class HumanReadableLabelling implements QualityMetric {

	private final Resource METRIC_URI = DQM.HumanReadableLabellingMetric;
	
	private HashMap<Node, Integer> entities = new HashMap<Node, Integer>();
	
	/**
	 * Each entity is checked for a Human Readable label <rdfs:comment> or <rdfs:label>.
	 * In this metric we are assuming that each entity has exactly 1 comment and/or label,
	 * thus we are not checking for contradicting labeling or commenting of entities.
	 */
	
	public void compute(Quad quad) {
		if (quad.getPredicate().getURI().equals(RDF.type.getURI())){
			// we've got an instance!
			if (!(entities.containsKey(quad.getSubject()))) { // an instance might have more than 1 type defined
				entities.put(quad.getSubject(), 0);
			}
		}
	
		if ( (quad.getPredicate().getURI().equals(RDFS.label.getURI())) || (quad.getPredicate().getURI().equals(RDFS.comment.getURI()))){
			// we'll check if the provider is cheating and is publishing empty string labels and comments
			if (!(quad.getObject().getLiteralValue().equals(""))) entities.put(quad.getSubject(), 1);
		}
	}

	
	public double metricValue() {
		double entities = 0.0;
		double humanLabels = 0.0;
		for (Node n : this.entities.keySet()){
			entities+=1;
			humanLabels += this.entities.get(n);
		}
		return humanLabels/entities; // at most we should have 1 label/comment for each entity
	}

	
	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	
	public ProblemList<?> getQualityProblems() {
		// TODO Auto-generated method stub
		return null;
	}

	
}
