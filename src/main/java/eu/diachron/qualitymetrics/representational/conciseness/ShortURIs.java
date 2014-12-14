package eu.diachron.qualitymetrics.representational.conciseness;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.semantics.vocabulary.DQM;
/**
 * @author Santiago Londono
 * Detects long URIs or those that contains query parameters, thereby providing a 
 * measure of how compactly is information represented in the dataset
 */
public class ShortURIs implements QualityMetric {
	
//	private static Logger logger = Logger.getLoggesr(ShortURIs.class);
	
	private final Resource METRIC_URI = DQM.ShortURIsMetric;
	
	/**
	 * Accumulator holding the sum of the lengths of all URIs locally defined in the dataset
	 */
	private long acumLocalURIsLen = 0;
	
	/**
	 * Counts the total number of URIs locally defined in the dataset
	 */
	private long countLocalDefURIs = 0;

	
	public void compute(Quad quad) {
		// Check whether current triple corresponds to an instance declaration (defining a local URI).
//		logger.trace("Computing triple with predicate: " + quad.getPredicate().getURI());
		Node predicateEdge = quad.getPredicate();
		
		// Determines whether the specified predicate corresponds to an instance declaration 
		// statement, that is, whether the statement is of the form: Instance rdf:type Class
		if(predicateEdge != null && predicateEdge.isURI() && predicateEdge.hasURI(RDF.type.getURI())) {
			// Determine if the subject is identified by an URI
			Node subject = quad.getSubject();
			
			if(subject.isURI()) {
				String subjectIdURI = (subject.getURI() != null)?(subject.getURI()):("");
//				logger.trace("Current subject's URI: " + subjectIdURI);
				
				// Having the subject's URI, add it's length to the accumulator and count it as a defined URI
				acumLocalURIsLen += subjectIdURI.length();
				countLocalDefURIs++;
			}
		}
	}

	
	public double metricValue() {
		// Calculate metric as the average length of the locally defined URIs
		return (double)acumLocalURIsLen / (double)countLocalDefURIs;
	}

	
	public Resource getMetricURI() {
		return METRIC_URI;
	}

	
	public ProblemList<?> getQualityProblems() {
		// TODO Auto-generated method stub
		return null;
	}

}
