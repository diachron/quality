package eu.diachron.qualitymetrics.accessibility.availability;

import java.util.ArrayList;
import java.util.List;




import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.semantics.knownvocabs.SD;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.VOID;

/**
 * @author Jeremy Debattista
 * 
 * Check if a SPARQL endpoint (matching void:sparqlEndpoint) is available and returns a result. 
 * 
 */
public class SPARQLAccessibility implements QualityMetric {

	private final Resource METRIC_URI = DQM.EndPointAvailabilityMetric;
	
	static Logger logger = LoggerFactory.getLogger(SPARQLAccessibility.class);
	
	/**
	 * list of problematic quads
	 */
	protected List<Quad> problemList = new ArrayList<Quad>();
	
	double metricValue = 0.0;
	
	double sparqlEndPoints = 0.0;
	double totalDefinedSparqlEndPoints = 0.0;
	
	
	final static List<String> endpointProperty = new ArrayList<String>();
	static{
		endpointProperty.add(VOID.sparqlEndpoint.getURI());
//		endpointProperty.add(SIOCSERVICES.service_endpoint);
		endpointProperty.add(SD.endpoint.getURI());
	}
	
	
	
	public void compute(Quad quad) {
		logger.debug("Assessing {}", quad.asTriple().toString());

		if ((endpointProperty.contains(quad.getPredicate().getURI())) && (quad.getSubject().getURI().equals(EnvironmentProperties.getInstance().getDatasetURI()))) {
			totalDefinedSparqlEndPoints++;
			String sparqlQuerystring = "SELECT ?s where {?s ?p ?o} LIMIT 1";
			Query query = QueryFactory.create(sparqlQuerystring);

			QueryExecution qexec = QueryExecutionFactory.sparqlService(quad.getObject().toString(), query);

			try{
				ResultSet results = qexec.execSelect();
	
				if (results.hasNext())
					sparqlEndPoints++;
				else
				{
					Quad q = new Quad(null, quad.getSubject() , QPRO.exceptionDescription.asNode(), DQM.InvalidSPARQLEndPoint.asNode());
					problemList.add(q);
				}
				qexec.close();
			} catch (QueryException e){
				logger.error("Endpoint " + quad.getObject().toString() + " responded with : " + e.getMessage());
				Quad q = new Quad(null, quad.getSubject() , QPRO.exceptionDescription.asNode(), DQM.InvalidSPARQLEndPoint.asNode());
				problemList.add(q);
			}
			
		}
	}

	public double metricValue() {
		if (totalDefinedSparqlEndPoints == 0.0) return 0.0;
		return sparqlEndPoints/totalDefinedSparqlEndPoints;
	}

	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		ProblemList<Quad> tmpProblemList = null;
		
		if (this.metricValue() == 0){
			String resource = EnvironmentProperties.getInstance().getBaseURI();
			Resource subject = ModelFactory.createDefaultModel().createResource(resource);
			Quad q = new Quad(null, subject.asNode() , QPRO.exceptionDescription.asNode(), DQM.NoEndPointAccessibility.asNode());
			this.problemList.add(q);
		}
		
		try {
			if(this.problemList != null && this.problemList.size() > 0) {
				tmpProblemList = new ProblemList<Quad>(this.problemList);
			} else {
				tmpProblemList = new ProblemList<Quad>();
			}
		} catch (ProblemListInitialisationException problemListInitialisationException) {
			//TODO
		}
		return tmpProblemList;
	}
	
	@Override
	public boolean isEstimate() {
		return false;
	}

	@Override
	public Resource getAgentURI() {
		return 	DQM.LuzzuProvenanceAgent;
	}
}
