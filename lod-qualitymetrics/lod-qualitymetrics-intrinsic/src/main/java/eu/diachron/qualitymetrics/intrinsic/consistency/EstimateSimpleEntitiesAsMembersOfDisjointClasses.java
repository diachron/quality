/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.consistency;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.semantics.DQMPROB;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ReservoirSampler;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import de.unibonn.iai.eis.luzzu.semantics.utilities.Commons;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.intrinsic.consistency.helper.MDC;
import eu.diachron.qualitymetrics.utilities.AbstractQualityMetric;
import eu.diachron.qualitymetrics.utilities.VocabularyLoader;

/**
 * @author Jeremy Debattista
 * 
 * An Estimate version for the metric Entities as members of
 * disjoint classes using reservoir sampling
 */
public class EstimateSimpleEntitiesAsMembersOfDisjointClasses extends AbstractQualityMetric{

	private final Resource METRIC_URI = DQM.EntitiesAsMembersOfDisjointClassesMetric;
	private static Logger logger = LoggerFactory.getLogger(EstimateSimpleEntitiesAsMembersOfDisjointClasses.class);
	protected long entitiesAsMembersOfDisjointClasses = 0;
	
	
	// Sampling of problems - testing for LOD Evaluation
	ReservoirSampler<ProblemReport> problemSampler = new ReservoirSampler<ProblemReport>(1000, false);
	
	private boolean metricCalculated = false;
	
	//Reservoir Settings
	private int MAX_SIZE = 100000;
	private ReservoirSampler<MDC> reservoir = new ReservoirSampler<MDC>(MAX_SIZE, true);

	public void setMaxSize(int size){
		MAX_SIZE = size;
		reservoir = new ReservoirSampler<MDC>(MAX_SIZE, true);
	}
	
	
	public void compute(Quad quad) {
//		logger.debug("Assessing {}", quad.asTriple().toString());

		try {
			
			RDFNode subject = Commons.asRDFNode(quad.getSubject());
			Node predicate = quad.getPredicate();
			RDFNode object = Commons.asRDFNode(quad.getObject());

			if (RDF.type.asNode().equals(predicate)) {
				MDC mdc = new MDC(subject.asResource());
				MDC foundMDC = this.reservoir.findItem(mdc);
				if (foundMDC == null){
					logger.trace("Subject not in reservoir: {}... Trying to add it", mdc.subject);

					mdc.objects.add(object);
					this.reservoir.add(mdc);
				} else {
					foundMDC.objects.add(object);
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
				Iterator<RDFNode> iter = new ArrayList<RDFNode>(mdc.objects).iterator();
				Set<RDFNode> checked = new HashSet<RDFNode>();
				while (iter.hasNext()){
					RDFNode _class = iter.next();
					checked.add(_class);
					Model model = VocabularyLoader.getInstance().getModelForVocabulary(_class.asResource().getNameSpace());
					Set<RDFNode> disjoinedClasses = model.listObjectsOfProperty(_class.asResource(), OWL.disjointWith).toSet();
					disjoinedClasses.retainAll(mdc.objects);
					disjoinedClasses.removeAll(checked);
					if (disjoinedClasses.size() > 0){
						count++;
						this.createProblemModel(mdc.subject.asNode(), _class.asNode(), disjoinedClasses);
					} 
				}
			}
		}
		
		metricCalculated = true;
		return count;
	}
	
	
//	private void createProblemModel(Node resource, Node _class, Set<RDFNode> _otherClasses){
//		Model m = ModelFactory.createDefaultModel();
//		
//		Resource subject = m.createResource(resource.toString());
//		m.add(new StatementImpl(subject, QPRO.exceptionDescription, DQM.MultiTypedResourceWithDisjointedClasses));
//		
//		
//		m.add(new StatementImpl(subject, DQM.violatingDisjoinedClass, m.asRDFNode(_class)));	
//		for(RDFNode s : _otherClasses){
//			m.add(new StatementImpl(subject, DQM.violatingDisjoinedClass, s));
//		}
//
//
//		this.problemList.add(new SerialisableModel(m));
//	}
//	
	private void createProblemModel(Node resource, Node _class, Set<RDFNode> _otherClasses){
		ProblemReport pr = new ProblemReport(resource, _class, _otherClasses);
		Boolean isAdded = this.problemSampler.add(pr);
		if (!isAdded) pr = null;
	}
	

	public double metricValue() {

		if (!metricCalculated){
			this.entitiesAsMembersOfDisjointClasses = countEntitiesAsMembersOfDisjointClasses();
		}
		
		if (this.reservoir.getItems().size() <= 0) {
			logger.warn("Total number of entities in given dataset is found to be zero.");
			return 0.0;
		}

		double metricValue = 1 - ((double) entitiesAsMembersOfDisjointClasses / this.reservoir.size());

		statsLogger.info("Dataset: {}; Members of Disjoined Classes: {}, Types of resource: {}, Metric Value: {}",EnvironmentProperties.getInstance().getDatasetURI(), this.entitiesAsMembersOfDisjointClasses, this.reservoir.getItems().size(), metricValue);

		return metricValue;
	}

	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

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

	public boolean isEstimate() {
		return true;
	}

	public Resource getAgentURI() {
		return DQM.LuzzuProvenanceAgent;
	}
	
	private class ProblemReport{
		
		private Set<RDFNode> _otherClasses;
		private Node resource;
		private Node _class;
		
		ProblemReport(Node resource, Node _class, Set<RDFNode> _otherClasses){
			this.resource = resource;
			this._class = _class;
			this._otherClasses = _otherClasses;
		}
		
		Model createProblemModel(){
			Model m = ModelFactory.createDefaultModel();
			
			Resource subject = m.createResource(resource.toString());
			m.add(new StatementImpl(subject, QPRO.exceptionDescription, DQMPROB.MultiTypedResourceWithDisjointedClasses));
			
			
			m.add(new StatementImpl(subject, DQMPROB.violatingDisjoinedClass, m.asRDFNode(_class)));	
			for(RDFNode s : _otherClasses){
				m.add(new StatementImpl(subject, DQMPROB.violatingDisjoinedClass, s));
			}


			return m;
		}
	}	
}
