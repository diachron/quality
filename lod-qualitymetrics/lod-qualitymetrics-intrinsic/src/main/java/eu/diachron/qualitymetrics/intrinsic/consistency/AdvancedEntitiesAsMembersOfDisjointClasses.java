package eu.diachron.qualitymetrics.intrinsic.consistency;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.semantics.utilities.SPARQLHelper;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.utilities.SerialisableModel;
import eu.diachron.qualitymetrics.utilities.VocabularyLoader;

/**
 * 
 * This metric checks both explicit resource type and their implicit (inferred)
 * subclasses for disjointness
 * 
 * @author Jeremy Debattista
 * 
 * TODO: fix when having a lot of classes
 */
public class AdvancedEntitiesAsMembersOfDisjointClasses implements QualityMetric {
	/**
	 * Metric URI
	 */
	private final Resource METRIC_URI = DQM.EntitiesAsMembersOfDisjointClassesMetric;
	/**
	 * logger static object
	 */
	private static Logger logger = LoggerFactory.getLogger(AdvancedEntitiesAsMembersOfDisjointClasses.class);
	
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
	private static DB mapDb = MapDbFactory.getMapDBAsyncTempFile();
	protected Set<SerialisableModel> problemList = MapDbFactory.createHashSet(mapDb, UUID.randomUUID().toString());
	
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
				Model vocabs = ModelFactory.createDefaultModel();
				
				StringBuilder askQuery = new StringBuilder();
				askQuery.append("SELECT DISTINCT * {");
				int counter = 0;
				List<String> lst = new ArrayList<String>();
				Set<String> nsSet = new HashSet<String>();
				for (String s : classes){
					String ns = ModelFactory.createDefaultModel().createResource(s).getNameSpace();
					if (nsSet.add(ns)) vocabs.add(VocabularyLoader.getModelForVocabulary(ns));
					askQuery.append("<"+s+"> " + SPARQLHelper.toSPARQL(RDFS.subClassOf) + "* ?s"+counter+" . ");
					lst.add("?s"+counter);
					counter++;
				}
				ICombinatoricsVector<String> originalVector = Factory.createVector(lst);
				Generator<String> gen = Factory.createPermutationGenerator(originalVector);
				for (ICombinatoricsVector<String> permutaion : gen) {
					askQuery.append(permutaion.getValue(0) + " <"+OWL.disjointWith+"> " +  permutaion.getValue(1) + " . ");
				}

				askQuery.append("}");
				
				logger.debug("Ask Query : {}",askQuery.toString());
				
				QueryExecution exec = QueryExecutionFactory.create(askQuery.toString(), vocabs);
				ResultSet rs = exec.execSelect();
				if (rs.hasNext()){
					count++;
					createProblemModel(ModelFactory.createDefaultModel().createResource(entity).asNode(), rs);
				}
				
			}
		}
		metricCalculated = true;
		return count;
	}
	
	
	private void createProblemModel(Node resource, ResultSet rs){
		Model m = ModelFactory.createDefaultModel();
		
		Resource subject = m.createResource(resource.toString());
		m.add(new StatementImpl(subject, QPRO.exceptionDescription, DQM.MultiTypedResourceWithDisjointedClasses));
		
		while (rs.hasNext()){
			QuerySolution qs = rs.next();
			Iterator<String> vars = qs.varNames();
			while(vars.hasNext()){
				String s = vars.next();
				RDFNode node = qs.get(s);
				m.add(new StatementImpl(subject, DQM.violatingDisjoinedClass,node));
			}
		}

		this.problemList.add(new SerialisableModel(m));
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

	@Override
	public boolean isEstimate() {
		return false;
	}

	@Override
	public Resource getAgentURI() {
		return DQM.LuzzuProvenanceAgent;
	}
}
