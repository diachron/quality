package eu.diachron.qualitymetrics.intrinsic.consistency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.semantics.DQMPROB;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ReservoirSampler;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.utilities.AbstractQualityMetric;
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
public class MisusedOwlDatatypeOrObjectProperties extends AbstractQualityMetric {

	private final Resource METRIC_URI = DQM.MisusedOwlDatatypeOrObjectPropertiesMetric;

	private static Logger logger = LoggerFactory.getLogger(MisusedOwlDatatypeOrObjectProperties.class);
	
//	private static DB mapDb = MapDbFactory.getMapDBAsyncTempFile();
//	protected Set<SerialisableModel> problemList =  MapDbFactory.createHashSet(mapDb, UUID.randomUUID().toString());
	
	// Sampling of problems - testing for LOD Evaluation
	ReservoirSampler<ProblemReport> problemSampler = new ReservoirSampler<ProblemReport>(50000, false);
		
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
		logger.debug("Assessing {}", quad.asTriple());

		Node subject = quad.getSubject();
		Node predicate = quad.getPredicate();
		Node object = quad.getObject();
		
		if (!(predicate.getURI().equals(RDF.type.getURI()))){
			if (VocabularyLoader.getInstance().checkTerm(predicate)){
				this.validPredicates++;
				
				if (object.isLiteral()){
					// predicate should not be an owl:ObjectProperty
					if (VocabularyLoader.getInstance().isObjectProperty(predicate)){
						this.misuseObjectProperties++;
						this.createProblemModel(subject, predicate, DQMPROB.MisusedObjectProperty);
					}
				} else if(object.isURI()){
					// predicate should not be an owl:DataProperty
					if (VocabularyLoader.getInstance().isDatatypeProperty(predicate)){
						this.misuseDatatypeProperties++;
						this.createProblemModel(subject, predicate, DQMPROB.MisusedDatatypeProperty);
					}
				}
			}
		}
	}

	
//	private void createProblemModel(Node resource, Node property, Resource type){
//		Model m = ModelFactory.createDefaultModel();
//		
//		Resource subject = m.createResource(resource.toString());
//		m.add(new StatementImpl(subject, QPRO.exceptionDescription, type));
//		
//		if (type.equals(DQM.MisusedDatatypeProperty))
//			m.add(new StatementImpl(subject, DQM.hasMisusedDatatypeProperty, m.asRDFNode(property)));		
//		else
//			m.add(new StatementImpl(subject, DQM.hasMisusedObjectProperty, m.asRDFNode(property)));		
//		
//
//		this.problemList.add(new SerialisableModel(m));
//	}
	
	private void createProblemModel(Node resource, Node property, Resource type){
		ProblemReport pr = new ProblemReport(resource, property, type);
		Boolean isAdded = this.problemSampler.add(pr);
		if (!isAdded) pr = null;
	}
	
	
	/**
	 * This method computes metric value for the object of this class
	 * 
	 * @return (total misuse properties) / (total properties)
	 */
	
	public double metricValue() {
		
		double metricValue = 1.0;
		
		double misused = this.misuseDatatypeProperties + this.misuseObjectProperties;
		if (misused > 0.0) 
			metricValue = 1.0 - (misused / this.validPredicates);
		
		statsLogger.info("Dataset: {}; Number of Misused Datatype Properties: {}, Number of Misused Object Property : {}, Metric Value: {}",this.getDatasetURI(),this.misuseDatatypeProperties, this.misuseObjectProperties, metricValue);
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
	
//	public ProblemList<?> getQualityProblems() {
//		ProblemList<SerialisableModel> tmpProblemList = null;
//		try {
//			if(this.problemList != null && this.problemList.size() > 0) {
//				tmpProblemList = new ProblemList<SerialisableModel>(new ArrayList<SerialisableModel>(this.problemList));
//			} else {
//				tmpProblemList = new ProblemList<SerialisableModel>();
//			}		} catch (ProblemListInitialisationException problemListInitialisationException) {
//			logger.error(problemListInitialisationException.getMessage());
//		}
//		return tmpProblemList;
//	}

	
	public ProblemList<?> getQualityProblems() {
		ProblemList<Model> tmpProblemList = new ProblemList<Model>();
		if(this.problemSampler != null && this.problemSampler.size() > 0) {
			for(ProblemReport pr : this.problemSampler.getItems()){
				tmpProblemList.getProblemList().add(pr.createProblemModel());
			}
		} else {
			tmpProblemList = new ProblemList<Model>();
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
	
	private class ProblemReport{
		
		private Resource type;
		private Node resource;
		private Node property;
		
		ProblemReport(Node resource, Node property, Resource type){
			this.resource = resource;
			this.property = property;
			this.type = type;
		}
		
		Model createProblemModel(){
			Model m = ModelFactory.createDefaultModel();
			
			Resource subject = m.createResource(resource.toString());
			m.add(new StatementImpl(subject, QPRO.exceptionDescription, type));
			
			if (type.equals(DQMPROB.MisusedDatatypeProperty))
				m.add(new StatementImpl(subject, DQMPROB.hasMisusedDatatypeProperty, m.asRDFNode(property)));		
			else
				m.add(new StatementImpl(subject, DQMPROB.hasMisusedObjectProperty, m.asRDFNode(property)));		
			

			return m;
		}
	}
}
