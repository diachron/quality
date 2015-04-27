package eu.diachron.qualitymetrics.intrinsic.consistency;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mapdb.HTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.utilities.VocabularyLoader;

/**
 * 
 * @author Christoph Lange
 * @date 13th May 2014
 */
public class EntitiesAsMembersOfDisjointClasses implements QualityMetric {
	/**
	 * Metric URI
	 */
	private final Resource METRIC_URI = DQM.EntitiesAsMembersOfDisjointClassesMetric;
	/**
	 * logger static object
	 */
	private static Logger logger = LoggerFactory.getLogger(EntitiesAsMembersOfDisjointClasses.class);
	
	/**
	 * number of entities that are instances of disjoint classes
	 */
	protected long entitiesAsMembersOfDisjointClasses = 0;
	
	
	/**
	 * the data structure that for each resource collects the classes it's an
	 * instance of
	 */
	protected HTreeMap<String, Set<String>> typesOfResource = MapDbFactory.createFilesystemDB().createHashMap("entities_members_disjoinedclasses").make();
	
	
	/**
	 * list of problematic nodes
	 */
	protected List<Model> problemList = new ArrayList<Model>();
	
	private boolean metricCalculated = false;

	/**
	 */
	
	public void compute(Quad quad) {
		logger.debug("Assessing {}", quad.asTriple().toString());

		try {
			String subject = quad.getSubject().toString();
			Node predicate = quad.getPredicate();
			String object = quad.getObject().toString();

			if (RDF.type.asNode().equals(predicate)) {
				// If we have a triple ?s rdf:type ?o, we add ?o to the list of
				// types of ?s

				Set<String> tmpTypes = typesOfResource.get(subject);

				if (tmpTypes == null) {
					tmpTypes = new HashSet<String>();
					typesOfResource.put(subject, tmpTypes);
				}

				tmpTypes.add(object);
			}
		} catch (Exception exception) {
			logger.error(exception.getMessage());
		}
	}

	/**
	 * counts number of entities that are members of disjoint classes
	 * 
	 * @return the number of entities that are members of disjoint classes
	 */
	protected long countEntitiesAsMembersOfDisjointClasses() {
		long count = 0;
		
		for (Map.Entry<String, Set<String>> entry : typesOfResource.entrySet()) {
			// one entity in the dataset …
			String entity = entry.getKey();
			// … and the classes it's an instance of
			Set<String> classes = entry.getValue();

			if (classes.size() >= 2) {
				// we only need to check disjointness when there are at least 2 classes
				
				Iterator<String> iter = new ArrayList<String>(classes).iterator();
				while (iter.hasNext()){
					String _class = iter.next();
					Resource classAsResource = ModelFactory.createDefaultModel().createResource(_class);
					Model model = VocabularyLoader.getModelForVocabulary(classAsResource.getNameSpace());
					
					// wrap the class under consideration into something that we can use to query the model
					
					// query the model for ?class owl:disjointWith ?otherClass
					for (StmtIterator i = model.listStatements(classAsResource, OWL.disjointWith, (RDFNode) null); i.hasNext(); ) {
						String otherClass = i.next().getObject().toString();
						// if ?otherClass is among the set of classes of our entity, the entity is a member of disjoint classes
						if (classes.contains(otherClass)) {
							count++;
							createProblemModel(ModelFactory.createDefaultModel().createResource(entity).asNode(), classAsResource.asNode(), ModelFactory.createDefaultModel().createResource(otherClass).asNode());							
						}
					}
					classes.remove(_class);
				}
			}
		}
		metricCalculated = true;
		return count;
	}
	
	private void createProblemModel(Node resource, Node _class, Node _otherClass){
		Model m = ModelFactory.createDefaultModel();
		
		Resource subject = m.createResource(resource.toString());
		m.add(new StatementImpl(subject, QPRO.exceptionDescription, DQM.MultiTypedResourceWithDisjointedClasses));
		
		
		m.add(new StatementImpl(subject, DQM.violatingDisjoinedClass, m.asRDFNode(_class)));		
		m.add(new StatementImpl(subject, DQM.violatingDisjoinedClass, m.asRDFNode(_otherClass)));


		this.problemList.add(m);
	}

	/**
	 * Returns metric value for the object of this class
	 * 
	 * @return (number of heterogeneous properties ) / (total number of
	 *         properties)
	 */
	
	public double metricValue() {

		if (!metricCalculated){
			this.entitiesAsMembersOfDisjointClasses = countEntitiesAsMembersOfDisjointClasses();
		}
		
		if (typesOfResource.entrySet().size() <= 0) {
			logger.warn("Total number of entities in given dataset is found to be zero.");
			return 0.0;
		}

		logger.debug("Values: Members of Disjoined Classes {}, Types of resource {}", this.entitiesAsMembersOfDisjointClasses, this.typesOfResource.entrySet().size());

		double metricValue = 1 - ((double) entitiesAsMembersOfDisjointClasses / this.typesOfResource.entrySet().size());

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
	 * Returns list of problematic quads
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
