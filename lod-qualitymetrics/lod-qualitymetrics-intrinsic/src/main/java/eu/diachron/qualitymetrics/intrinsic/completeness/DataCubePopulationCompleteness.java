/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.completeness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.semantics.DQMPROB;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.AfterException;
import de.unibonn.iai.eis.luzzu.exceptions.BeforeException;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.semantics.utilities.Commons;
import eu.diachron.qualitymetrics.utilities.AbstractComplexQualityMetric;

/**
 * @author Jeremy Debattista
 * 
 * This metric defines a generic method to check
 * for population completeness within RDF DataCube
 * defined datasets. 
 * 
 * The idea is to check the completeness of observations
 * against a list of "original/fixed" codelist. For example
 * check that there exists at least one mayor for
 * every city in France. Mayors are listed
 * as observations (as they might change over time)
 * and cities are the "original/fixed" codelist 
 */
public class DataCubePopulationCompleteness extends AbstractComplexQualityMetric {

	private final Resource METRIC_URI = DQM.DataCubePopulationCompleteness;
	
	private static Logger logger = LoggerFactory.getLogger(DataCubePopulationCompleteness.class);
	
	private Map<Resource,PopulationCompletenessConfiguration> pcc_set = new HashMap<Resource,PopulationCompletenessConfiguration>();
	private Map<Resource, Set<Resource>> gs_codeLists = new HashMap<Resource, Set<Resource>>();
	
//	private static DB mapDb = MapDbFactory.getMapDBAsyncTempFile();
//	private Map<Resource, Map<Resource, Integer>> ds_seenCLConcepts = MapDbFactory.createHashMap(mapDb, "seen-codelist-concepts");
//	private Map<Resource, Set<Resource>> ds_observations = MapDbFactory.createHashMap(mapDb, "codelist-2-observation-mapping");

	private Map<Resource, Map<Resource, Integer>> ds_seenCLConcepts = new HashMap<Resource, Map<Resource, Integer>>();
	private Map<Resource, Set<Resource>> ds_observations = new HashMap<Resource, Set<Resource>>();
	
	private Dataset ds = TDBFactory.createDataset("/tmp/diachron-tdb/");
	private Model problemModel = ds.getDefaultModel();
	{
		ds.begin(ReadWrite.WRITE);
	}

	
	@Override
	public void compute(Quad quad) {
		Resource prop = Commons.asRDFNode(quad.getPredicate()).asResource();
		if (pcc_set.containsKey(prop)){
			Resource observation = Commons.asRDFNode(quad.getSubject()).asResource();
			Resource object = Commons.asRDFNode(quad.getObject()).asResource();
			
			Map<Resource, Integer> codelist = (ds_seenCLConcepts.containsKey(prop)) ? ds_seenCLConcepts.get(prop) : new HashMap<Resource, Integer>();
			int codelistCount = (codelist.containsKey(object)) ? codelist.get(object) : 1;
			codelist.put(object, codelistCount);
			ds_seenCLConcepts.put(prop, codelist);
			
			Set<Resource> observationSet = (ds_observations.containsKey(object)) ? ds_observations.get(object) : new HashSet<Resource>();
			observationSet.add(observation);
			ds_observations.put(object,observationSet);
		}
	}

	@Override
	public double metricValue() {
		double averagePopulationCompleteness = 0.0;
		
		for(Resource res : gs_codeLists.keySet()){
			int totalExpectedCoverage = gs_codeLists.get(res).size();
			PopulationCompletenessConfiguration pcc = this.pcc_set.get(res);
			Set<Resource> missingCoverageSet = pcc.assertComponentProperty(ds_seenCLConcepts.get(res), gs_codeLists.get(res));
			int actualCoverageSize = totalExpectedCoverage - missingCoverageSet.size();
			double totalCoverage = (double) actualCoverageSize / (double)totalExpectedCoverage;
			
			averagePopulationCompleteness += totalCoverage;
			
			logger.info("Total Expected Size: {}, Actual Size: {}, Total Coverage {}%", totalExpectedCoverage, actualCoverageSize, (totalCoverage * 100));
			this.addToProblemReport(res, missingCoverageSet);
		}
		
		
		return (averagePopulationCompleteness / (double) pcc_set.size());
	}
	
	private void addToProblemReport(Resource codedProperty, Set<Resource> setCodedProperties){
		Resource uri = Commons.generateURI();
		problemModel.add(uri, RDF.type, DQMPROB.MissingPopulationCoverage);
		problemModel.add(uri, DQMPROB.forCodedProperty, codedProperty.asResource());
		
		Bag bag = problemModel.createBag(Commons.generateURI().getURI());
		for (Resource r : setCodedProperties){
			try{
				bag.add(r);
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		this.problemModel.add(uri, DQMPROB.missingPopulationCoverageList, bag);
	}

	@Override
	public Resource getMetricURI() {
		return this.METRIC_URI;
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

	@Override
	public void before(Object... args) throws BeforeException {
		// Load Config File
//		if (args == null || args.length <= 0 || args[0] == null){
//			logger.error("Argument in the Reuse Existing Vocabulary metric should not be null.");
//			throw new BeforeException("Argument should not be a string with a filename.");
//		}
//		String fileName = (String) args[0];
		
		logger.info("Loading Configuration File.");

		Model configModel = RDFDataMgr.loadModel(DataCubePopulationCompleteness.class.getResource("/completeness/dp_pc.ttl").getFile());
		
		StmtIterator iter = configModel.listStatements((Resource) null, RDF.type, configModel.createResource("http://example.org/PopulationCompletenessConfiguration"));
		
		while(iter.hasNext()){
			// Create Assessment Configuration
			Statement stmt = iter.next();
			NodeIterator configObjects = configModel.listObjectsOfProperty(stmt.getSubject(), configModel.createProperty("http://example.org/conditionConfiguration"));
			while(configObjects.hasNext()){
				RDFNode config = configObjects.next();
				PopulationCompletenessConfiguration pcc = new PopulationCompletenessConfiguration();

				pcc.setAssessedComponentProperty(configModel.listObjectsOfProperty(config.asResource(), configModel.createProperty("http://example.org/assessedComponentProperty")).next().asResource());
				pcc.setConditionOperator(configModel.listObjectsOfProperty(config.asResource(), configModel.createProperty("http://example.org/conditionOperator")).next().asLiteral().getString());
				pcc.setConditionValue(configModel.listObjectsOfProperty(config.asResource(), configModel.createProperty("http://example.org/conditionValue")).next().asLiteral());

				
				pcc_set.put(pcc.assessedComponentProperty,pcc);
			}
			
			// Load DSD and Gold Standard to get information about the configured properties only
			String dsdFile = configModel.listObjectsOfProperty(stmt.getSubject(), configModel.createProperty("http://example.org/dsd")).next().asResource().getURI();
			Model dsdModel = RDFDataMgr.loadModel(dsdFile);
			
			String gsFile = configModel.listObjectsOfProperty(stmt.getSubject(), configModel.createProperty("http://example.org/goldStandard")).next().asResource().getURI();
			Model gsModel = RDFDataMgr.loadModel(gsFile);
			
			logger.info("Finding all coded properties.");

			String strQuery = "SELECT DISTINCT ?prop ?codelist ?range { ?prop a <http://purl.org/linked-data/cube#CodedProperty> . "
					+ "?prop <http://purl.org/linked-data/cube#codeList> ?codelist . "
					+ "?prop <http://www.w3.org/2000/01/rdf-schema#range> ?range . }";
			
			Query query = QueryFactory.create(strQuery);
			QueryExecution qexec = QueryExecutionFactory.create(query, dsdModel);
			ResultSet rs = qexec.execSelect();
			while (rs.hasNext()){
				QuerySolution sol = rs.next();
				Resource codedProperty = sol.getResource("prop");
				if (!pcc_set.containsKey(codedProperty)) continue;

				Resource codeList = sol.getResource("codelist");
				Resource range = sol.getResource("range");

				logger.info("Loading code list for {}.", codedProperty.getURI());
				
				Set<Resource> codeListset = new HashSet<Resource>();
				String strCLQuery = "SELECT DISTINCT ?s { ?s a <"+range.getURI()+"> ."
						+ "?s <http://www.w3.org/2004/02/skos/core#inScheme> <"+codeList.getURI()+"> . }";
				Query clQuery = QueryFactory.create(strCLQuery);
				QueryExecution clQexec = QueryExecutionFactory.create(clQuery,gsModel);
				ResultSet clRS = clQexec.execSelect();
				while (clRS.hasNext()){
					codeListset.add(clRS.next().getResource("s"));
				}

				gs_codeLists.put(codedProperty, codeListset);
			}
		}
	}

	@Override
	public void after(Object... args) throws AfterException {
		// TODO Auto-generated method stub
	}
	
	
	private class PopulationCompletenessConfiguration{
		private Resource assessedComponentProperty;
		private String conditionOperator;
		private Literal conditionValue;
		
		
		public void setAssessedComponentProperty(
				Resource assessedComponentProperty) {
			this.assessedComponentProperty = assessedComponentProperty;
		}

		public void setConditionOperator(String conditionOperator) {
			this.conditionOperator = conditionOperator;
		}

		public void setConditionValue(Literal conditionValue) {
			this.conditionValue = conditionValue;
		}
		
		public Set<Resource> assertComponentProperty(Map<Resource, Integer> datasetAndCount, Set<Resource> gs_codeList){			
			//Returns a list of resources that do not match the configuration condition
			Set<Resource> returnResource = new HashSet<Resource>();
			
			for(Resource res : gs_codeList){
				if (datasetAndCount.containsKey(res)){
					int cv = conditionValue.getInt();
					
					if (conditionOperator.equals("<")) if (!(datasetAndCount.get(res) < cv)) returnResource.add(res);
					if (conditionOperator.equals(">")) if (!(datasetAndCount.get(res) > cv)) returnResource.add(res);
					if (conditionOperator.equals("=")) if (!(datasetAndCount.get(res) == cv)) returnResource.add(res);
					if (conditionOperator.equals(">=")) if (!(datasetAndCount.get(res) >= cv)) returnResource.add(res);
					if (conditionOperator.equals("<=")) if (!(datasetAndCount.get(res) <= cv)) returnResource.add(res);
					
				} else {
					returnResource.add(res);
				}
			}
			return returnResource;
		}
		
	}

}
