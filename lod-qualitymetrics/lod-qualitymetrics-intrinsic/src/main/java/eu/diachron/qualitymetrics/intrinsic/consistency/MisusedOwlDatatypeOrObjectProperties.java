package eu.diachron.qualitymetrics.intrinsic.consistency;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.utilities.VocabularyLoader;

/**
 * 
 * This metric is based on the metric defined by Hogan et al.
 * Weaving the Pedantic Web. Detect properties that are defined as a owl:datatype 
 * property but is used as object property and properties defined as a owl:object 
 * property and used as datatype property The metric is computed as a ratio of 
 * misused properties. Undefined properties are ignored
 * 
 * @author Jeremy Debattista
 * 
 */
public class MisusedOwlDatatypeOrObjectProperties implements QualityMetric {

	private final Resource METRIC_URI = DQM.MisusedOwlDatatypeOrObjectPropertiesMetric;

	private static Logger logger = LoggerFactory.getLogger(MisusedOwlDatatypeOrObjectProperties.class);
	
	private List<Model> problemList = new ArrayList<Model>();

	private double misuseDatatypeProperties = 0.0;
	private double misuseObjectProperties = 0.0;
	private double validPredicates = 0.0;

	/**
	 * This method computes identified a given quad is a misuse owl data type
	 * property or object property.
	 * 
	 * @param quad - to be identified
	 */
	
	public void compute(Quad quad) {
		logger.debug("Assessing {}", quad.asTriple().toString());

		Node subject = quad.getSubject();
		Node predicate = quad.getPredicate();
		Node object = quad.getObject();
		
		if (!(predicate.getURI().equals(RDF.type.getURI()))){
			if (VocabularyLoader.checkTerm(predicate)){
				this.validPredicates++;
				Model m = VocabularyLoader.getModelForVocabulary(predicate.getNameSpace());
				
				if (object.isLiteral()){
					// predicate should not be an owl:ObjectProperty
					if (m.contains((m.asRDFNode(predicate)).asResource(), RDF.type, OWL.ObjectProperty)){
						this.misuseObjectProperties++;
						this.createProblemModel(subject, predicate, DQM.MisusedObjectProperty);
					}
				} else if(object.isURI()){
					// predicate should not be an owl:DataProperty
					if (m.contains((m.asRDFNode(predicate)).asResource(), RDF.type, OWL.DatatypeProperty)){
						this.misuseDatatypeProperties++;
						this.createProblemModel(subject, predicate, DQM.MisusedDatatypeProperty);
					}
				}
			}
		}
	}

	
	private void createProblemModel(Node resource, Node property, Resource type){
		Model m = ModelFactory.createDefaultModel();
		
		Resource subject = m.createResource(resource.toString());
		m.add(new StatementImpl(subject, QPRO.exceptionDescription, type));
		
		if (type.equals(DQM.MisusedDatatypeProperty))
			m.add(new StatementImpl(subject, DQM.hasMisusedDatatypeProperty, m.asRDFNode(property)));		
		else
			m.add(new StatementImpl(subject, DQM.hasMisusedObjectProperty, m.asRDFNode(property)));		
		

		this.problemList.add(m);
	}
	/**
	 * This method computes metric value for the object of this class
	 * 
	 * @return (total misuse properties) / (total properties)
	 */
	
	public double metricValue() {
		
		logger.info("Number of Misused Datatype Properties: {}", this.misuseDatatypeProperties);
		logger.info("Number of Misused Object Property : {}", this.misuseObjectProperties);

		double metricValue = 1.0;
		
		double misused = this.misuseDatatypeProperties + this.misuseObjectProperties;
		if (misused > 0.0) 
			metricValue = 1.0 - (misused / this.validPredicates);
		
		logger.info("Metric Value: {}", metricValue);
		return metricValue;
	}

	/**
	 * Returns Metric URI
	 * 
	 * @return metric URI
	 */
	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	/**
	 * Returns list of problematic Quads
	 * 
	 * @return list of problematic quads
	 */
	
	public ProblemList<?> getQualityProblems() {
		ProblemList<Model> tmpProblemList = null;
		try {
			if(this.problemList != null && this.problemList.size() > 0) {
				tmpProblemList = new ProblemList<Model>(this.problemList);
			} else {
				tmpProblemList = new ProblemList<Model>();
			}		} catch (ProblemListInitialisationException problemListInitialisationException) {
			logger.error(problemListInitialisationException.getMessage());
		}
		return tmpProblemList;
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
