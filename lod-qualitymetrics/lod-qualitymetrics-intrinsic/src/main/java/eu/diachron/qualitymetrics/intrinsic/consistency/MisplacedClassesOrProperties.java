package eu.diachron.qualitymetrics.intrinsic.consistency;

import java.util.ArrayList;
import java.util.Set;

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
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.utilities.SerialisableModel;
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
	
	private HTreeMap<String, Boolean> seenProperties = MapDbFactory.createFilesystemDB().createHashMap("misplaced-classes-seenProperties").makeOrGet();
	private HTreeMap<String, Boolean> seenClasses = MapDbFactory.createFilesystemDB().createHashMap("misplaced-classes-seenClasses").makeOrGet();

	private double misplacedClassesCount = 0.0;
	private double totalClassesCount = 0.0;
	private double misplacedPropertiesCount = 0.0;
	private double totalPropertiesCount = 0.0;
	protected Set<SerialisableModel> problemList = MapDbFactory.createFilesystemDB().createHashSet("problem-list").make();
	

	public void compute(Quad quad) {
//		logger.debug("Assessing {}", quad.asTriple().toString());

		Node predicate = quad.getPredicate(); // retrieve predicate
		Node object = quad.getObject(); // retrieve object
		
		//checking if classes are found in the property position
//		logger.info("Is the used predicate {} actually a class?", predicate.getURI());
		this.totalPropertiesCount++;
		if (seenProperties.containsKey(predicate.toString())){
			if (!(seenProperties.get(predicate.toString()))){
				this.misplacedPropertiesCount++;
				this.createProblemModel(quad.getSubject(), predicate, DQM.MisplacedClass);
			}
		} else {
			if ((VocabularyLoader.isClass(predicate)) && (VocabularyLoader.checkTerm(predicate))){
				this.misplacedPropertiesCount++;
				this.createProblemModel(quad.getSubject(), predicate, DQM.MisplacedClass);
				seenProperties.put(predicate.toString(), false);
			}
			seenProperties.put(predicate.toString(), true);
		}
		
		//checking if properties are found in the object position
		if ((object.isURI()) && (predicate.getURI().equals(RDF.type.getURI())) && (VocabularyLoader.checkTerm(object))){
//			logger.info("Checking {} for misplaced class", object.getURI());
			this.totalClassesCount++;
			if (seenClasses.containsKey(object.toString())){
				if (!(seenClasses.get(object.toString()))){
					this.misplacedClassesCount++;
					this.createProblemModel(quad.getSubject(), object, DQM.MisplacedProperty);
				}
			} else {
				if (VocabularyLoader.isProperty(object)){
					this.misplacedClassesCount++;
					this.createProblemModel(quad.getSubject(), object, DQM.MisplacedProperty);
					seenClasses.put(object.toString(), false);
				}
				seenClasses.put(object.toString(), true);
			}
		}
	}
	
	private void createProblemModel(Node resource, Node classOrProperty, Resource type){
		Model m = ModelFactory.createDefaultModel();
		
		Resource subject = m.createResource(resource.toString());
		m.add(new StatementImpl(subject, QPRO.exceptionDescription, type));
		
		if (type.equals(DQM.MisplacedClass))
			m.add(new StatementImpl(subject, DQM.hasMisplacedClass, m.asRDFNode(classOrProperty)));		
		else
			m.add(new StatementImpl(subject, DQM.hasMisplacedProperty, m.asRDFNode(classOrProperty)));		
		

		this.problemList.add(new SerialisableModel(m));
	}

	/**
	 * This method computes metric value for the object of this class.
	 * 
	 * @return (total number of undefined classes or properties) / (total number
	 *         of classes or properties)
	 */
	
	public double metricValue() {
		logger.info("Number of Misplaced Classes: {}", this.misplacedClassesCount);
		logger.info("Number of Misplaced Properties: {}", this.misplacedPropertiesCount);

		double metricValue = 1.0;
		
		double misplaced = this.misplacedPropertiesCount + this.misplacedPropertiesCount;
		if (misplaced > 0.0) 
			metricValue = 1.0 - (misplaced / (this.totalPropertiesCount + this.totalClassesCount));
		
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
		ProblemList<SerialisableModel> tmpProblemList = null;
		try {
			if(this.problemList != null && this.problemList.size() > 0) {
				tmpProblemList = new ProblemList<SerialisableModel>(new ArrayList<SerialisableModel>(this.problemList));
			} else {
				tmpProblemList = new ProblemList<SerialisableModel>();
			}		} catch (ProblemListInitialisationException problemListInitialisationException) {
			logger.error(problemListInitialisationException.getMessage());
		}
		return tmpProblemList;
	}

	/* (non-Javadoc)
	 * @see de.unibonn.iai.eis.luzzu.assessment.QualityMetric#isEstimate()
	 */
	@Override
	public boolean isEstimate() {
		return false;
	}

	/* (non-Javadoc)
	 * @see de.unibonn.iai.eis.luzzu.assessment.QualityMetric#getAgentURI()
	 */
	@Override
	public Resource getAgentURI() {
		return DQM.LuzzuProvenanceAgent;
	}
	
}