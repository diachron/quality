/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.syntacticvalidity;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ReservoirSampler;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.semantics.utilities.Commons;
import eu.diachron.qualitymetrics.utilities.AbstractQualityMetric;

/**
 * 
 * Checks for the number of untyped literals.
 * Untyped literals, albeit the possibility of
 * being correct, are undesirable as agents
 * should be able to convert a Literal value
 * to the actual data type
 * 
 * @author Jeremy Debattista
 * 
 */
public class UntypedLiterals extends AbstractQualityMetric {

	private int numberTypedLiterals = 0;
	private int numberUntypedLiterals = 0;
	
	ReservoirSampler<ProblemReport> problemSampler = new ReservoirSampler<ProblemReport>(1000, false);

	private Model problemModel = ModelFactory.createDefaultModel();
	private  Resource bagURI = Commons.generateURI();
	private Bag problemBag = problemModel.createBag(bagURI.getURI());
	{
		//TODO: fix
		problemModel.createStatement(bagURI, RDF.type, problemModel.createResource(DQM.NAMESPACE+"UntypedLiteralException"));
	}
	
	@Override
	public void compute(Quad quad) {
		Node obj = quad.getObject();
		
		if (obj.isLiteral()){
			if (obj.getLiteralDatatype() != null) numberTypedLiterals++;
			else{
				ProblemReport pr = new ProblemReport(quad);
				Boolean isAdded = this.problemSampler.add(pr);
				if (!isAdded) pr = null;
				numberUntypedLiterals++;
			}
		}
		
	}

	@Override
	public double metricValue() {
		statsLogger.info("Dataset: {};# Typed Literals: {}, # Untyped Literals: {}",this.getDatasetURI(), numberTypedLiterals, numberUntypedLiterals);
		if (((double)numberTypedLiterals + (double) numberUntypedLiterals) == 0.0) return 1.0;
		
		return 1.0 - (double)numberUntypedLiterals / ((double)numberTypedLiterals + (double) numberUntypedLiterals);
	}

	@Override
	public Resource getMetricURI() {
		return DQM.UntypedLiteralsMetric;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		ProblemList<Model> tmpProblemList = new ProblemList<Model>();

		if(this.problemSampler != null && this.problemSampler.size() > 0) {
			for(ProblemReport pr : this.problemSampler.getItems()){
				List<Statement> stmt = pr.createProblemModel();
				this.problemBag.add(stmt.get(0).getSubject());
				this.problemModel.add(stmt);
			}
			tmpProblemList.getProblemList().add(this.problemModel);

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
		private Quad q;
		
		ProblemReport(Quad q){
			this.q = q;
		}
		
		List<Statement> createProblemModel(){
			List<Statement> lst = new ArrayList<Statement>();
	    	Resource anon = ModelFactory.createDefaultModel().createResource(AnonId.create());
	    	
	    	lst.add(new StatementImpl(anon, RDF.subject, Commons.asRDFNode(q.getSubject())));
	    	lst.add(new StatementImpl(anon, RDF.predicate, Commons.asRDFNode(q.getPredicate())));
	    	lst.add(new StatementImpl(anon, RDF.object, Commons.asRDFNode(q.getObject())));
	    	
	    	return lst;
		}
	}

}
