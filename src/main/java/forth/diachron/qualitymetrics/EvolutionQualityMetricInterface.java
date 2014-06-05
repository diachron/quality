package forth.diachron.qualitymetrics;

import java.util.List;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;

/**
 * @author hrysakis
 * 
 */
public interface EvolutionQualityMetricInterface {

	/**
	 * This method should compute the metric.
	 * 
	 */
	void compute(); //without quad argument but similar to other Bonn metrics

	/**
	 * @return the value computed by the Quality Metric
	 */
	double metricValue();

	/**
	 * This method will return daQ triples which will be stored in the dataset
	 * QualityGraph.
	 * 
	 * @return a list of daQ triples
	 */
	List<Triple> toDAQTriples();

	/**
	 * @return returns the daQ URI of the Quality Metric
	 */
	Resource getMetricURI();
	
	/**
	 * @return returns a typed ProblemList which will be used to create a "quality report" of the metric.
	 */
	ProblemList<?> getQualityProblems();
}
