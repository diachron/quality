package de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.consistency;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;

/**
 * 
 * @author Christoph Lange
 * @date 13th May 2014
 */
public class EntitiesAsMembersOfDisjointClasses extends AbstractQualityMetric {
	/**
	 * Metric URI
	 */
	private final Resource METRIC_URI = DQM.EntitiesAsMembersOfDisjointClassesMetric;
	/**
	 * logger static object
	 */
	static Logger logger = Logger
			.getLogger(EntitiesAsMembersOfDisjointClasses.class);
	/**
	 * number of entities that are instances of disjoint classes
	 */
	protected long entitiesAsMembersOfDisjointClasses = 0;
	/**
	 * set of all entities
	 * TODO OK to use HashSet, or should we use something concurrent?
	 */
	protected Set<Node> allEntities = new HashSet<Node>();
	/**
	 * the data structure that for each resource collects the classes it's an
	 * instance of
	 */
	protected Map<Node, Set<Node>> typesOfResource = new Hashtable<Node, Set<Node>>();
	/**
	 * list of problematic nodes
	 */
	protected List<Node> problemList = new ArrayList<Node>();

	/**
	 */
	@Override
	public void compute(Quad quad) {
		logger.trace("compute() --Started--");
		try {
			Node subject = quad.getSubject();
			Node predicate = quad.getPredicate();
			Node object = quad.getObject();

			// TODO I suspect a lot of metrics count the entities they process.  If I'm right, should we centralise this functionality?
			allEntities.add(subject);
			
			if (RDF.type.asNode().equals(predicate)) {
				// If we have a triple ?s rdf:type ?o, we add ?o to the list of
				// types of ?s

				Set<Node> tmpTypes = typesOfResource.get(subject);

				if (tmpTypes == null) {
					// FIXME Is it OK to use HashSet here, or should we be using
					// something that permits concurrency?
					tmpTypes = new HashSet<Node>();
					typesOfResource.put(subject, tmpTypes);
				}

				tmpTypes.add(object);
			}
		} catch (Exception exception) {
			logger.debug(exception);
			logger.error(exception.getMessage());
		}
		logger.trace("compute() --Ended--");
	}

	/**
	 * This method identifies whether a given property is heterogeneous or not.
	 * 
	 * @param givenTable
	 *            - property (its rdf type and its count)
	 * @param threshold
	 *            - to declare a property as heterogeneous
	 * @return true - if heterogeneous
	 */
	protected boolean isHeterogeneousDataType(
			Hashtable<RDFDatatype, Long> givenTable, Long threshold) {

		Long tmpMax = new Long(0); // for count of Max dataType
		Long tmpTotal = new Long(0); // for count of total

		Enumeration<RDFDatatype> enumKey = givenTable.keys();

		while (enumKey.hasMoreElements()) {
			RDFDatatype key = enumKey.nextElement();
			Long value = givenTable.get(key);
			tmpMax = (value > tmpMax) ? value : tmpMax; // get Max Datatype
			tmpTotal += value; // count total
		}

		return (((tmpMax / tmpTotal) * 100) >= threshold) ? true : false;
	}

	/**
	 * counts number of entities that are members of disjoint classes
	 * 
	 * @return the number of entities that are members of disjoint classes
	 */
	protected long countEntitiesAsMembersOfDisjointClasses() {
		long count = 0;
		
		for (Map.Entry<Node, Set<Node>> entry : typesOfResource.entrySet()) {
			// one entity in the dataset …
			Node entity = entry.getKey();
			// … and the classes it's an instance of
			Set<Node> classes = entry.getValue();

			if (classes.size() >= 2) {
				// we only need to check disjointness when there are at least 2 classes
				
				classesLoop: for (Node _class : classes) {
					// retrieve an RDF description of the class (and possibly of more stuff, maybe even the whole vocabularies) in an LOD way
					// TODO it's terribly inefficient to do this all the time.  We need a library that caches downloaded vocabularies
					Model model = ModelFactory.createDefaultModel();
					model.read(_class.getURI());
					
					// wrap the class under consideration into something that we can use to query the model
					// TODO can this be done more elegantly?
					Resource classAsResource = model.createResource(_class.getURI());
					
					// query the model for ?class owl:disjointWith ?otherClass
					for (StmtIterator i = model.listStatements(classAsResource, OWL.disjointWith, (RDFNode) null); i.hasNext(); ) {
						Node otherClass = i.next().getObject().asNode();
						// if ?otherClass is among the set of classes of our entity, the entity is a member of disjoint classes
						if (classes.contains(otherClass)
								/* TODO actually we'd only have to check whether the set of classes we've not yet iterated over contains(otherClass) */
								) {
							count++;
							problemList.add(entity);
							break classesLoop;
						}
					}
				}
			}
		}
		
		return count;
	}

	/**
	 * Returns metric value for the object of this class
	 * 
	 * @return (number of heterogeneous properties ) / (total number of
	 *         properties)
	 */
	@Override
	public double metricValue() {

		logger.trace("metricValue() --Started--");

		this.entitiesAsMembersOfDisjointClasses = countEntitiesAsMembersOfDisjointClasses();

		// return ZERO if total number of entities is ZERO [WARN]
		if (allEntities.size() <= 0) {
			logger.warn("Total number of entities in given dataset is found to be zero.");
			return 0.0;
		}

		double metricValue = (double) entitiesAsMembersOfDisjointClasses
				/ allEntities.size();

		logger.trace("metricValue() --Ended--");

		return metricValue;
	}

	/**
	 * Returns Metric URI
	 * 
	 * @return metric URI
	 */
	@Override
	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	/**
	 * Returns list of problematic quads
	 * 
	 * @return list of problematic quads
	 */
	@Override
	public ProblemList<?> getQualityProblems() {
		ProblemList<Node> tmpProblemList = null;
		try {
			tmpProblemList = new ProblemList<Node>(this.problemList);
		} catch (ProblemListInitialisationException problemListInitialisationException) {
			logger.debug(problemListInitialisationException);
			logger.error(problemListInitialisationException.getMessage());
		}
		return tmpProblemList;
	}

}
