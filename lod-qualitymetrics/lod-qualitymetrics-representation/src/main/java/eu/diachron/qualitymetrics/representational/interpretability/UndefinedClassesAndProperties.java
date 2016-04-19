/**
 * 
 */
package eu.diachron.qualitymetrics.representational.interpretability;

import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.semantics.DQMPROB;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ReservoirSampler;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import de.unibonn.iai.eis.luzzu.semantics.utilities.Commons;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.utilities.VocabularyLoader;

/**
 * @author Jeremy Debattista
 * 
 * This metric measures the number of undefined classes and
 * properties used by a data publisher in the assessed dataset.
 * By undefined classes and properties we mean that such resources
 * are used without any formal definition (e.g. using foaf:image 
 * instead of foaf:img).
 * 
 */
public class UndefinedClassesAndProperties implements QualityMetric {

	private int undefinedClasses = 0;
	private int undefinedProperties = 0;
	private int totalClasses = 0;
	private int totalProperties = 0;
	
	private static Logger logger = LoggerFactory.getLogger(UndefinedClassesAndProperties.class);
//	private SharedResources shared = SharedResources.getInstance();
//	private static DB mapDb = MapDbFactory.getMapDBAsyncTempFile();

//	private Set<String> seenSet = MapDbFactory.createHashSet(mapDb, UUID.randomUUID().toString());
//	private Set<SerialisableQuad> _problemList = MapDbFactory.createHashSet(mapDb, UUID.randomUUID().toString());
    private ConcurrentMap<String, Boolean> seenSet = new ConcurrentLinkedHashMap.Builder<String, Boolean>().maximumWeightedCapacity(100000).build();

	ReservoirSampler<ProblemReport> problemSampler = new ReservoirSampler<ProblemReport>(250000, false);
	private Model problemModel = ModelFactory.createDefaultModel();

	
	@Override
	public void compute(Quad quad) {
		logger.debug("Assessing quad: " + quad.asTriple().toString());

		Node predicate = quad.getPredicate();
		
		if (predicate.hasURI(RDF.type.getURI())){
			// Checking for classes
			Node object = quad.getObject();
			if ((!(object.isBlank())) &&  (!(this.seenSet.get(object.getURI())))){
				logger.info("checking class: " + object.getURI());
	
				if (!(object.isBlank())){
					this.totalClasses++;
					
					Boolean defined = false;
					if (VocabularyLoader.getInstance().checkTerm(predicate)){
						defined =  VocabularyLoader.getInstance().isClass(object);
					}
					
					if (!defined){
						this.undefinedClasses++;
						Quad q = new Quad(null, object, QPRO.exceptionDescription.asNode(), DQMPROB.UndefinedClass.asNode());
						ProblemReport pr = new ProblemReport(q);
						problemSampler.add(pr);
					}
				}
				this.seenSet.put(object.getURI(),true);
			}
			
		} 
		if (!(this.seenSet.get(predicate.getURI()))){
			// Checking for properties
			this.totalProperties++;
			logger.info("checking predicate: " + predicate.getURI());
			
			if (!(this.isContainerPredicate(predicate))){
				Boolean defined = false;
				if (VocabularyLoader.getInstance().checkTerm(predicate)){
					defined = VocabularyLoader.getInstance().isProperty(predicate);
				}

				if (!defined){
					this.undefinedProperties++;
					Quad q = new Quad(null, predicate, QPRO.exceptionDescription.asNode(), DQMPROB.UndefinedProperty.asNode());
					ProblemReport pr = new ProblemReport(q);
					problemSampler.add(pr);
				}
			}
			this.seenSet.put(predicate.getURI(),true);
		}	
	}
	
	private boolean isContainerPredicate(Node predicate){
		if (predicate.getURI().matches(RDF.getURI()+"_[0-9]+")){
			return true;
		}
		return false;
	}

	@Override
	public double metricValue() {
		statsLogger.info("Undefined Classes and Properties. Dataset: {} - Undefined Classes {}, Undefined Properties {}", 
				EnvironmentProperties.getInstance().getDatasetURI(), this.undefinedClasses, this.undefinedProperties);


		return (this.undefinedClasses + this.undefinedProperties == 0) ? 1.0 
				: 1.0 - ((double)(this.undefinedClasses + this.undefinedProperties)/(double)(this.totalClasses + this.totalProperties));
	}

	@Override
	public Resource getMetricURI() {
		return DQM.UndefinedClassesAndPropertiesMetric;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		ProblemList<Model> tmpProblemList = new ProblemList<Model>();
		if(this.problemSampler != null && this.problemSampler.size() > 0) {
			for(ProblemReport pr : this.problemSampler.getItems()){
				this.problemModel.add(pr.getProblemQuad());
			}
			tmpProblemList.getProblemList().add(this.problemModel);

		} else {
			tmpProblemList = new ProblemList<Model>();
		}
		return tmpProblemList;
	}
	
//	public ProblemList<?> getQualityProblems() {
//		ProblemList<SerialisableQuad> pl = null;
//		try {
//			if(this._problemList != null && this._problemList.size() > 0) {
//				pl = new ProblemList<SerialisableQuad>(this._problemList);	
//			} else {
//				pl = new ProblemList<SerialisableQuad>();
//			}
//		} catch (ProblemListInitialisationException e) {
//			logger.error(e.getMessage());
//		}
//		return pl;
//	}

	@Override
	public boolean isEstimate() {
		return false;
	}

	@Override
	public Resource getAgentURI() {
		return 	DQM.LuzzuProvenanceAgent;
	}
	
	private class ProblemReport{
		
		private Quad q;

		ProblemReport(Quad q){
			this.q = q;
		}
		
		
		Statement getProblemQuad(){
			Statement s = new StatementImpl(Commons.asRDFNode(q.getSubject()).asResource(),
			ModelFactory.createDefaultModel().createProperty(q.getPredicate().getURI()),
			Commons.asRDFNode(q.getObject())
			);
	    	
	    	
	    	return s;
		}
	}
	
}
