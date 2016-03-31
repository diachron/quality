package eu.diachron.qualitymetrics.intrinsic.consistency;

import java.util.UUID;

import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.semantics.DQMPROB;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ReservoirSampler;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.utilities.VocabularyLoader;

/**
 * This metric is based on the metric defined by Hogan et al.
 * Weaving the Pedantic Web. This metric checks if the assessed
 * dataset has a defined classed placed in the triple's predicate
 * and defined property in the object position. If an undefined
 * class or property is used, then it is ignored
 *  
 * Best Case : 1
 * Worst Case : 0
 * 
 * @author Jeremy Debattista
 */
public class MisplacedClassesOrProperties implements QualityMetric {

	private final Resource METRIC_URI = DQM.MisplacedClassesOrPropertiesMetric;
	private static Logger logger = LoggerFactory.getLogger(MisplacedClassesOrProperties.class);
	
	private HTreeMap<String, Boolean> seenProperties = MapDbFactory.createHashMap(mapDb, UUID.randomUUID().toString());
	private HTreeMap<String, Boolean> seenClasses = MapDbFactory.createHashMap(mapDb, UUID.randomUUID().toString());

	private double misplacedClassesCount = 0.0;
	private double totalClassesCount = 0.0;
	private double misplacedPropertiesCount = 0.0;
	private double totalPropertiesCount = 0.0;
	
	private static DB mapDb = MapDbFactory.getMapDBAsyncTempFile();
//	protected Set<SerialisableModel> problemList =  MapDbFactory.createHashSet(mapDb, UUID.randomUUID().toString());

	// Sampling of problems - testing for LOD Evaluation
	ReservoirSampler<ProblemReport> problemSampler = new ReservoirSampler<ProblemReport>(1000, false);
	
	
	
	public void compute(Quad quad) {
		logger.debug("Assessing {}", quad.asTriple());

		Node predicate = quad.getPredicate(); // retrieve predicate
		Node object = quad.getObject(); // retrieve object
		
		//checking if classes are found in the property position
//		logger.info("Is the used predicate {} actually a class?", predicate.getURI());
		this.totalPropertiesCount++;
		if (seenProperties.containsKey(predicate.toString())){
			if (!(seenProperties.get(predicate.toString()))){
				this.misplacedPropertiesCount++;
				this.createProblemModel(quad.getSubject(), predicate, DQMPROB.MisplacedClass);
			}
		} else {
			if(VocabularyLoader.checkTerm(predicate)){ //if the predicate does not exist, then do not count it as misplaced
				if ((VocabularyLoader.isClass(predicate))){
					this.misplacedPropertiesCount++;
					this.createProblemModel(quad.getSubject(), predicate, DQMPROB.MisplacedClass);
					seenProperties.put(predicate.toString(), false);
				}
				seenProperties.put(predicate.toString(), true);
			}
		}
		
		//checking if properties are found in the object position
		if ((object.isURI()) && (predicate.getURI().equals(RDF.type.getURI()))){
			if (VocabularyLoader.checkTerm(object)){
				logger.info("Checking {} for misplaced class", object.getURI());
				this.totalClassesCount++;
				if (seenClasses.containsKey(object.toString())){
					if (!(seenClasses.get(object.toString()))){
						this.misplacedClassesCount++;
						this.createProblemModel(quad.getSubject(), object, DQMPROB.MisplacedProperty);
					}
				} else {
					if(VocabularyLoader.checkTerm(object)){ //if the object does not exist, then do not count it as misplaced
						if (VocabularyLoader.isProperty(object)){
							this.misplacedClassesCount++;
							this.createProblemModel(quad.getSubject(), object, DQMPROB.MisplacedProperty);
							seenClasses.put(object.toString(), false);
						}
						seenClasses.put(object.toString(), true);
					}
				}
			}
		}
	}
	
//	private void createProblemModel(Node resource, Node classOrProperty, Resource type){
//		Model m = ModelFactory.createDefaultModel();
//		
//		Resource subject = m.createResource(resource.toString());
//		m.add(new StatementImpl(subject, QPRO.exceptionDescription, type));
//		
//		if (type.equals(DQM.MisplacedClass))
//			m.add(new StatementImpl(subject, DQM.hasMisplacedClass, m.asRDFNode(classOrProperty)));		
//		else
//			m.add(new StatementImpl(subject, DQM.hasMisplacedProperty, m.asRDFNode(classOrProperty)));		
//		
//
//		this.problemList.add(new SerialisableModel(m));
//	}
	
	private void createProblemModel(Node resource, Node classOrProperty, Resource type){
		ProblemReport pr = new ProblemReport(resource, classOrProperty, type);
		Boolean isAdded = this.problemSampler.add(pr);
		if (!isAdded) pr = null;
	}

	/**
	 * This method computes metric value for the object of this class.
	 * 
	 * @return (total number of undefined classes or properties) / (total number
	 *         of classes or properties)
	 */
	
	public double metricValue() {

		double metricValue = 1.0;
		
		double misplaced = this.misplacedClassesCount + this.misplacedPropertiesCount;
		if (misplaced > 0.0) 
			metricValue = 1.0 - (misplaced / (this.totalPropertiesCount + this.totalClassesCount));
		
		statsLogger.info("Number of Misplaced Classes: {}; Number of Misplaced Properties: {}; Total Properties Count: {}; Metric Value: {}", this.misplacedClassesCount , this.misplacedPropertiesCount, this.totalPropertiesCount, metricValue);

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
		private Node classOrProperty;
		private Node resource;
		
		ProblemReport(Node resource, Node classOrProperty, Resource type){
			this.resource = resource;
			this.classOrProperty = classOrProperty;
			this.type = type;
		}
		
		Model createProblemModel(){
			Model m = ModelFactory.createDefaultModel();
			
			Resource subject = m.createResource(resource.toString());
			m.add(new StatementImpl(subject, QPRO.exceptionDescription, type));
			
			if (type.equals(DQMPROB.MisplacedClass))
				m.add(new StatementImpl(subject, DQMPROB.hasMisplacedClass, m.asRDFNode(classOrProperty)));		
			else
				m.add(new StatementImpl(subject, DQMPROB.hasMisplacedProperty, m.asRDFNode(classOrProperty)));		
			

			return m;
		}
		

	}	
	
}