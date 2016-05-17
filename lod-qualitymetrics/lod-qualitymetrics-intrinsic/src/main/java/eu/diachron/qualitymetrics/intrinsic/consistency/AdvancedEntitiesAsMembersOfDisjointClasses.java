package eu.diachron.qualitymetrics.intrinsic.consistency;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
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
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.utilities.AbstractQualityMetric;
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
public class AdvancedEntitiesAsMembersOfDisjointClasses extends AbstractQualityMetric {
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
	protected HTreeMap<String, Set<String>> typesOfResource = MapDbFactory.createHashMap(mapDb, UUID.randomUUID().toString());
	
	
	/**
	 * list of problematic nodes
	 */
	private static DB mapDb = MapDbFactory.getMapDBAsyncTempFile();
//	protected Set<SerialisableModel> problemList = MapDbFactory.createHashSet(mapDb, UUID.randomUUID().toString());
	
	
	/**
	 * Sampling of problems - testing for LOD Evaluation
	 */
	ReservoirSampler<ProblemReport> problemSampler = new ReservoirSampler<ProblemReport>(1000, false);
	
	
	
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
					tmpTypes.add(object);
					typesOfResource.put(subject, tmpTypes);
				} else {
					tmpTypes.add(object);
					typesOfResource.put(subject, tmpTypes);
				}
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
		
//		for (Map.Entry<String, Set<String>> entry : typesOfResource.entrySet()) {
		for (String entity : typesOfResource.keySet()){
			// one entity in the dataset …
//			String entity = entry.getKey();
			// … and the classes it's an instance of
			Set<String> classes = typesOfResource.get(entity);
			
			if (classes.size() >= 2) {
				// we only need to check disjointness when there are at least 2 classes
				boolean isDisjoint = false;
				for (String s : classes){
					if (VocabularyLoader.getInstance().checkTerm(ModelFactory.createDefaultModel().createResource(s).asNode())){
						Set<String> _set = this.rdfNodeSetToString(VocabularyLoader.getInstance().getDisjointWith(ModelFactory.createDefaultModel().createResource(s).asNode()));
						
						SetView<String> setView = Sets.intersection(classes, _set);
						if (setView.size() > 0){
							isDisjoint = true;
							createProblemModel(ModelFactory.createDefaultModel().createResource(entity).asNode(), setView);
						}
					}
				}
				if (isDisjoint) count++;
//				SELECT DISTINCT * {
//					<http://social.mercedes-benz.com/mars/schema/InternalModelName> <http://www.w3.org/2000/01/rdf-schema#subClassOf>* ?s0 . 
//					<http://www.w3.org/2008/05/skos-xl#Label> <http://www.w3.org/2000/01/rdf-schema#subClassOf>* ?s1 . 
//					?s0 <http://www.w3.org/2002/07/owl#disjointWith> ?s1 . 
//					?s1 <http://www.w3.org/2002/07/owl#disjointWith> ?s0 . }
			}
		}
		metricCalculated = true;
		return count;
	}
	
	
//	private void createProblemModel(Node resource, ResultSet rs){
//		Model m = ModelFactory.createDefaultModel();
//		
//		Resource subject = m.createResource(resource.toString());
//		m.add(new StatementImpl(subject, QPRO.exceptionDescription, DQM.MultiTypedResourceWithDisjointedClasses));
//		
//		while (rs.hasNext()){
//			QuerySolution qs = rs.next();
//			Iterator<String> vars = qs.varNames();
//			while(vars.hasNext()){
//				String s = vars.next();
//				RDFNode node = qs.get(s);
//				m.add(new StatementImpl(subject, DQM.violatingDisjoinedClass,node));
//			}
//		}
//
//		this.problemList.add(new SerialisableModel(m));
//	}
	
	
	private void createProblemModel(Node resource, SetView<String> sv){
		ProblemReport pr = new ProblemReport(resource, sv);
		Boolean isAdded = this.problemSampler.add(pr);
		if (!isAdded) pr = null;
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

		double metricValue = 1 - ((double) entitiesAsMembersOfDisjointClasses / this.typesOfResource.entrySet().size());

		statsLogger.info("Dataset: {}; Values: Members of Disjoined Classes: {}, Types of resource: {}, Metric Value: {}", EnvironmentProperties.getInstance().getDatasetURI(), this.entitiesAsMembersOfDisjointClasses, this.typesOfResource.entrySet().size(), metricValue);

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
//	
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
	
	// problems private class for sampling
	private class ProblemReport{
		
		private Node resource;
		private SetView<String> setView;
		
		
		ProblemReport(Node resource, SetView<String> setView){
			this.resource = resource;
			this.setView = setView;
		}
		
		Model createProblemModel(){
			Model m = ModelFactory.createDefaultModel();
			
			Resource subject = m.createResource(resource.toString());
			m.add(new StatementImpl(subject, QPRO.exceptionDescription, DQMPROB.MultiTypedResourceWithDisjointedClasses));
			
			for(String s : setView){
				m.add(new StatementImpl(subject, DQMPROB.violatingDisjoinedClass, m.createResource(s)));
			}

			return m;
		}
	}
	
	private Set<String> rdfNodeSetToString(Set<RDFNode> set){
		Set<String> hSet = new HashSet<String>();
		for(RDFNode n : set) hSet.add(n.asResource().getURI());
		return hSet;
	}
}
