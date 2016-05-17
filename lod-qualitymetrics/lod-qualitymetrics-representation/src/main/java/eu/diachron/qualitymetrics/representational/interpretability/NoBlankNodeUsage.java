/**
 * 
 */
package eu.diachron.qualitymetrics.representational.interpretability;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.mapdb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ReservoirSampler;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import de.unibonn.iai.eis.luzzu.semantics.utilities.Commons;
import eu.diachron.qualitymetrics.utilities.AbstractQualityMetric;

/**
 * @author Jeremy Debattista
 * 
 * This metric calculates the number of blank nodes found in the subject or the object
 * of a triple. The metric value returns a value [0-1] where a higher number of blank nodes
 * will result in a value closer to 0.
 */
public class NoBlankNodeUsage extends AbstractQualityMetric {

	private static Logger logger = LoggerFactory.getLogger(NoBlankNodeUsage.class);
	
	//we will store all data level constraints that are URIs
	private static DB mapDb = MapDbFactory.getMapDBAsyncTempFile();
	private Set<String> uniqueDLC = MapDbFactory.createHashSet(mapDb, UUID.randomUUID().toString());
	private Set<String> uniqueBN = MapDbFactory.createHashSet(mapDb, UUID.randomUUID().toString());
//	private Set<SerialisableQuad> _problemList = MapDbFactory.createHashSet(mapDb, UUID.randomUUID().toString());
	
	ReservoirSampler<ProblemReport> problemSampler = new ReservoirSampler<ProblemReport>(250000, false);
	private Model problemModel = ModelFactory.createDefaultModel();
//	private  Resource bagURI = Commons.generateURI();
//	private Bag problemBag = problemModel.createBag(bagURI.getURI());
	
	@Override
	public void compute(Quad quad) {
		Node predicate = quad.getPredicate();
		Node subject = quad.getSubject();
		Node object = quad.getObject();

		logger.debug("Assessing quad: " + quad.asTriple().toString());
		
		// we will skip all "typed" triples
		if (!(predicate.hasURI(RDF.type.getURI()))){
			if (subject.isBlank()) {
				uniqueBN.add(subject.getBlankNodeLabel());
				ProblemReport pr = new ProblemReport(quad);
				problemSampler.add(pr);
			}
			else uniqueDLC.add(subject.getURI());
			
			if (!(object.isLiteral())){
				if (object.isBlank()){
					uniqueBN.add(object.getBlankNodeLabel());
					ProblemReport pr = new ProblemReport(quad);
					problemSampler.add(pr);
				}
				else uniqueDLC.add(object.getURI());
			}
		}
	}

	@Override
	public double metricValue() {
		statsLogger.info("No Blank Node Usage. Dataset: {} - Unique DLC {}, Unique BN {}", 
				EnvironmentProperties.getInstance().getDatasetURI(), uniqueDLC.size(), uniqueBN.size() );

		return ((double) uniqueDLC.size()) / ((double) uniqueDLC.size() + (double) uniqueBN.size());
	}

	@Override
	public Resource getMetricURI() {
		return DQM.NoBlankNodeMetric;
	}

	//TODO: fix problem report
	@Override 
	public ProblemList<?> getQualityProblems() {
		ProblemList<Model> tmpProblemList = new ProblemList<Model>();
		if(this.problemSampler != null && this.problemSampler.size() > 0) {
//			for(ProblemReport pr : this.problemSampler.getItems()){
//				List<Statement> stmt = pr.createProblemModel();
//				this.problemBag.add(stmt.get(0).getSubject());
//				this.problemModel.add(stmt);
//			}
//			tmpProblemList.getProblemList().add(this.problemModel);

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
		
		
		List<Statement> createProblemModel(){
			List<Statement> lst = new ArrayList<Statement>();
	    	Resource anon = ModelFactory.createDefaultModel().createResource(Commons.generateURI());
	    	
	    	lst.add(new StatementImpl(anon, RDF.subject, Commons.asRDFNode(q.getSubject())));
	    	lst.add(new StatementImpl(anon, RDF.predicate, Commons.asRDFNode(q.getPredicate())));
	    	lst.add(new StatementImpl(anon, RDF.object, Commons.asRDFNode(q.getObject())));
	    	
	    	return lst;
		}
	}
}
