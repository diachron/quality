/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.syntacticvalidity;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
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
 * This metric checks the compatability of the literal datatype
 * against the lexical form of the said literal.
 * This metric only catches literals with a datatype
 * whilst untyped literals are not checked in this metric
 * as their lexical form cannot be validated.
 * 
 * Therefore, in order to check for untyped literals,
 * the metric UntypedLiterals in the same dimension
 * checks for such quality problems.
 * 
 * @author Jeremy Debattista
 * 
 */
public class CompatibleDatatype extends AbstractQualityMetric {

//	private static Logger logger = LoggerFactory.getLogger(CompatibleDatatype.class);
	
	
	// Sampling of problems - testing for LOD Evaluation
	ReservoirSampler<ProblemReport> problemSampler = new ReservoirSampler<ProblemReport>(1000, false);

	private Model problemModel = ModelFactory.createDefaultModel();
	
	private Resource bagURI = Commons.generateURI();
	private Bag problemBag = problemModel.createBag(bagURI.getURI());
	{
		//TODO: fix
		problemModel.createStatement(bagURI, RDF.type, problemModel.createResource(DQM.NAMESPACE+"CompatibleDatatypeException"));
	}
	
	private int numberCorrectLiterals = 0;
	private int numberIncorrectLiterals = 0;

	@Override
	public void compute(Quad quad) {
		Node obj = quad.getObject();
		
		if (obj.isLiteral()){
			if (obj.getLiteralDatatype() != null){
				// unknown datatypes cannot be checked for their correctness,
				// but in the UsageOfIncorrectDomainOrRangeDatatypes metric
				// we check if these literals are used correctly against their
				// defined property. We also check for untyped literals in another metric
				if (this.compatibleDatatype(obj)) 
					numberCorrectLiterals++; 
				else {
					this.addToProblem(quad);
					numberIncorrectLiterals++;
				}
			} 
		}
	}
	
//    private void addToProblem(Quad q){
//    	Resource anon = problemModel.createResource(AnonId.create());
//    	
//    	problemModel.createStatement(anon, RDF.subject, Commons.asRDFNode(q.getSubject()));
//    	problemModel.createStatement(anon, RDF.predicate, Commons.asRDFNode(q.getPredicate()));
//    	problemModel.createStatement(anon, RDF.object, Commons.asRDFNode(q.getObject()));
//    	
//    	problemBag.add(anon);
//    }
	
	private void addToProblem(Quad q){
		ProblemReport pr = new ProblemReport(q);
		Boolean isAdded = this.problemSampler.add(pr);
		if (!isAdded) pr = null;
	}
    

	@Override
	public double metricValue() {
		double metricValue = (double) numberCorrectLiterals / ((double)numberIncorrectLiterals + (double)numberCorrectLiterals);
		statsLogger.info("CompatibleDatatype. Dataset: {} - Total # Correct Literals : {}; # Incorrect Literals : {}; # Metric Value: {}", 
				this.getDatasetURI(), numberCorrectLiterals, numberIncorrectLiterals, metricValue);
		
		if (((Double)metricValue).isNaN())
			metricValue = 1.0d;

		return metricValue;
	}

	@Override
	public Resource getMetricURI() {
		return DQM.CompatibleDatatype;
	}

//	@Override
//	public ProblemList<?> getQualityProblems() {
//		ProblemList<Model> tmpProblemList = null;
//		try {
//			if(this.problemModel != null && this.problemModel.size() > 0) {
//				List<Model> problemList = new ArrayList<Model>();
//				problemList.add(problemModel);
//				tmpProblemList = new ProblemList<Model>(problemList);
//			} else {
//				tmpProblemList = new ProblemList<Model>();
//			}		} catch (ProblemListInitialisationException problemListInitialisationException) {
//			logger.error(problemListInitialisationException.getMessage());
//		}
//		return tmpProblemList;
//	}
	
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
	
	private boolean compatibleDatatype(Node lit_obj){
		RDFNode n = Commons.asRDFNode(lit_obj);
		Literal lt = (Literal) n;
		RDFDatatype dt = lt.getDatatype();
		String stringValue = lt.getLexicalForm();
		
		return dt.isValid(stringValue);
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
