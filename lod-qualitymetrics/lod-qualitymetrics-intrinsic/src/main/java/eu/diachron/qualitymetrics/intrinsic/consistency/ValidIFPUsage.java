/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.consistency;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Bag;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import de.unibonn.iai.eis.luzzu.semantics.utilities.Commons;
import eu.diachron.qualitymetrics.intrinsic.consistency.helper.IFPTriple;
import eu.diachron.qualitymetrics.utilities.VocabularyLoader;

/**
 * @author Jeremy Debattista
 * 
 * This metric checks if the Inverse Functional Properties (IFP)
 * is used correctly, i.e. if we have S P O and P is set to be
 * an owl:InverseFunctionalProperty, then S is the one and only
 * resource connected to O. If there is a triple S1 P O, then
 * the IFP is not used correctly and thus since S1 will be "reasoned"
 * to be the same as S.
 * 
 * More information can be found in Hogan et. al Weaving the Pedantic Web.
 * 
 */
public class ValidIFPUsage implements QualityMetric{
	
	private final Resource METRIC_URI = DQM.ValidIFPUsageMetric;

	private static Logger logger = LoggerFactory.getLogger(ValidIFPUsage.class);
	
	private int totalIFPs = 0;
	private int totalViolatedIFPs = 0;
	private Map<IFPTriple,IFPTriple> seenIFPs = MapDbFactory.createAsyncFilesystemDB().createHashMap("seen-ifp-statements").make();

	//private Model problemModel = ModelFactory.createDefaultModel();
	
	private Dataset ds = TDBFactory.createDataset("/tmp/diachron-tdb/");
	private Model problemModel = ds.getDefaultModel();
	{
		ds.begin(ReadWrite.WRITE);
	}
	
	int counter = 0;
	@Override
	public void compute(Quad quad) {
		counter++;
		logger.debug("Computing : {} ", quad.asTriple().toString());
		
		if (VocabularyLoader.isInverseFunctionalProperty(quad.getPredicate())){
			logger.debug("{} is an IFP", quad.asTriple().toString());
			totalIFPs++;
			
			IFPTriple t = new IFPTriple(quad.asTriple());
			if (seenIFPs.containsKey(t)){
				totalViolatedIFPs++;
				t = seenIFPs.get(t);
				this.addProblem(t, quad); // if we encounter this violation for the first time, then we'll increment the totalViolatedIFPs once more.
			} else {
				seenIFPs.put(t,t);
			}
		}
	}
	
	private void addProblem(IFPTriple t, Quad q){
		Bag bag = problemModel.createBag(Commons.generateURI().getURI());
		
		Resource problemURI = t.getProblemURI();
		
		if (!(this.problemModel.contains(problemURI, RDF.type, DQM.InverseFunctionalPropertyViolation))){
			this.problemModel.add(problemURI, RDF.type, DQM.InverseFunctionalPropertyViolation);
			this.problemModel.add(problemURI, DQM.violatedPredicate, Commons.asRDFNode(q.getPredicate()));
			this.problemModel.add(problemURI, DQM.violatedObject, Commons.asRDFNode(q.getObject()));
			
			bag = this.problemModel.createBag();
			bag.add(t.getSubject());
			this.problemModel.add(problemURI, DQM.violatingSubjects, bag);
			
			//if it is the first time we encountered this violation
			totalViolatedIFPs++;
		}
		Resource bagURI = this.problemModel.listObjectsOfProperty(problemURI, DQM.violatingSubjects).next().asResource();
		bag = this.problemModel.getBag(bagURI);
		this.problemModel.remove(problemURI, DQM.violatingSubjects, bag);
			
		bag.add(Commons.asRDFNode(q.getSubject()));
		this.problemModel.add(problemURI, DQM.violatingSubjects, bag);
	}
	
	@Override
	public double metricValue() {
		logger.info("ValidIFPUsage. Dataset: {} - Total # IFP Statements : {}; # Violated Predicate-Object Statements : {};    {}"
				, EnvironmentProperties.getInstance().getDatasetURI(), totalIFPs, totalViolatedIFPs,counter);

		if (totalIFPs == 0) return 1.0;
		return 1.0 - ((double)totalViolatedIFPs/(double)totalIFPs);
	}

	@Override
	public Resource getMetricURI() {
		return METRIC_URI;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		ds.close();
		ProblemList<Model> tmpProblemList = null;
		try {
			if(this.problemModel != null && this.problemModel.size() > 0) {
				List<Model> problemList = new ArrayList<Model>();
				problemList.add(problemModel);
				tmpProblemList = new ProblemList<Model>(problemList);
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
