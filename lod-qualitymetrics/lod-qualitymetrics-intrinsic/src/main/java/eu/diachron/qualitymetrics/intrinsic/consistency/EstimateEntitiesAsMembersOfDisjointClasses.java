/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.consistency;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.mapdb.HTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ReservoirSampler;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.utilities.VocabularyLoader;

/**
 * @author Jeremy Debattista
 * 
 * An Estimate version for the metric Entities as members of
 * disjoint classes using reservoir sampling
 */
public class EstimateEntitiesAsMembersOfDisjointClasses {

	private final Resource METRIC_URI = DQM.EntitiesAsMembersOfDisjointClassesMetric;
	private static Logger logger = LoggerFactory.getLogger(EntitiesAsMembersOfDisjointClasses.class);
	protected long entitiesAsMembersOfDisjointClasses = 0;
	
	protected HTreeMap<String, MDC> typesOfResource = MapDbFactory.createFilesystemDB().createHashMap("entities_members_disjoinedclasses").make();
	protected List<Model> problemList = new ArrayList<Model>();
	private boolean metricCalculated = false;
	
	//Reservoir Settings
	private static int MAX_SIZE = 100000;
	private ReservoirSampler<MDC> reservoir = new ReservoirSampler<MDC>(MAX_SIZE, true);

	
	public void compute(Quad quad) {
		logger.debug("Assessing {}", quad.asTriple().toString());

		try {
			String subject = quad.getSubject().toString();
			Node predicate = quad.getPredicate();
			String object = quad.getObject().toString();

			if (RDF.type.asNode().equals(predicate)) {
				
				if (this.typesOfResource.containsKey(subject)){
					MDC mdc = this.typesOfResource.get(subject);
					MDC foundMDC = this.reservoir.findItem(mdc);
					if (foundMDC == null){
						logger.trace("Subject not in reservoir: {}... Trying to add it", mdc.subject);

						mdc.objects.add(object);
						this.reservoir.add(mdc);
					} else {
						foundMDC.objects.add(object);
					}
				} else{
					MDC mdc = new MDC(subject,object);
					this.typesOfResource.put(subject, mdc);
					logger.trace("Subject not in reservoir: {}... Trying to add it", mdc.subject);
					this.reservoir.add(mdc);
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
		for (MDC mdc : this.reservoir.getItems()){
			if (mdc.objects.size() >= 2){
				Iterator<String> iter = new ArrayList<String>(mdc.objects).iterator();
				while (iter.hasNext()){
					String _class = iter.next();
					Resource classAsResource = ModelFactory.createDefaultModel().createResource(_class);
					Model model = VocabularyLoader.getModelForVocabulary(classAsResource.getNameSpace());
					Set<String> disjoinedClasses = this.getDisjointClasses(model, _class);
					disjoinedClasses.retainAll(mdc.objects);
					if (disjoinedClasses.size() > 0){
						count++;
						this.createProblemModel(ModelFactory.createDefaultModel().createResource(mdc.subject).asNode(), classAsResource.asNode(), disjoinedClasses);
					}
				}
			}
		}
		
		metricCalculated = true;
		return count;
	}
	
	private void createProblemModel(Node resource, Node _class, Set<String> _otherClasses){
		Model m = ModelFactory.createDefaultModel();
		
		Resource subject = m.createResource(resource.toString());
		m.add(new StatementImpl(subject, QPRO.exceptionDescription, DQM.MultiTypedResourceWithDisjointedClasses));
		
		
		m.add(new StatementImpl(subject, DQM.violatingDisjoinedClass, m.asRDFNode(_class)));	
		for(String s : _otherClasses){
			m.add(new StatementImpl(subject, DQM.violatingDisjoinedClass, m.createResource(s)));
		}


		this.problemList.add(m);
	}

	public double metricValue() {

		if (!metricCalculated){
			this.entitiesAsMembersOfDisjointClasses = countEntitiesAsMembersOfDisjointClasses();
		}
		
		if (typesOfResource.entrySet().size() <= 0) {
			logger.warn("Total number of entities in given dataset is found to be zero.");
			return 0.0;
		}

		logger.debug("Values: Members of Disjoined Classes {}, Types of resource {}", this.entitiesAsMembersOfDisjointClasses, this.typesOfResource.entrySet().size());

		double metricValue = (double) entitiesAsMembersOfDisjointClasses / this.typesOfResource.entrySet().size();

		return metricValue;
	}

	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

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

	public boolean isEstimate() {
		return true;
	}

	public Resource getAgentURI() {
		return DQM.LuzzuProvenanceAgent;
	}
	
	private Set<String> getDisjointClasses(Model m, String _class){
		String query = "SELECT ?object WHERE { <" + _class + "> <" + OWL.disjointWith.getURI() + "> ?object }";

		Query qry = QueryFactory.create(query);
	    QueryExecution qe = QueryExecutionFactory.create(qry, m);
	    ResultSet rs = qe.execSelect();
	    Set<String> objects = new HashSet<String>();

	    while (rs.hasNext()){
	    	 objects.add(rs.next().get("object").toString());
	    }
	    return objects;
	}
	
	class MDC implements Serializable{
		private static final long serialVersionUID = 2235605621422072042L;
		protected String subject;
		protected Set<String> objects;
		
		public MDC(String subject, String object){
			this.subject = subject;
			this.objects = new HashSet<String>();
			this.objects.add(object);
		}
	}
}
