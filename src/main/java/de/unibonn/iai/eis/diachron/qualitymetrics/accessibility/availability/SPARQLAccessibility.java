package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.qualitymetrics.QualityMetric;
import de.unibonn.iai.eis.diachron.qualitymetrics.representational.understandability.EmptyAnnotationValue;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;
import de.unibonn.iai.eis.diachron.vocabularies.VOID;

/**
 * @author Nikhil Patra
 * 
 * Check if a SPARQL endpoint (matching void:sparqlEndpoint) is available and returns a result. 
 * 
 */
public class SPARQLAccessibility extends AbstractQualityMetric {

	private final Resource METRIC_URI = DQM.EndPointAvailabilityMetric;
	
	static Logger logger = Logger.getLogger(EmptyAnnotationValue.class);
	
	/**
	 * list of problematic quads
	 */
	protected List<Quad> problemList = new ArrayList<Quad>();
	
	double metricValue = 0.0;

	public void compute(Quad quad) {

		if (quad.getPredicate().getURI().equals(VOID.sparqlEndpoint.getURI())) {

			String sparqlQuerystring = "select ?s where {?s ?p ?o}limit 1";
			Query query = QueryFactory.create(sparqlQuerystring);

			QueryExecution qexec = QueryExecutionFactory.sparqlService(quad.getObject().toString(), query);

			ResultSet results = qexec.execSelect();

			// Iterating over the SPARQL Query results and check if any result is received
			// set metricValue to 1 if results received otherwise set to 0
			if (results.hasNext())
				metricValue = 1;
			else
			{
				metricValue = 0;
				problemList.add(quad);
				
			}
			

			// Release the resources used to query
			qexec.close();
		}

	}

	public double metricValue() {
		return metricValue;
	}

	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric#
	 * getQualityProblems()
	 */
	@Override
	public ProblemList<?> getQualityProblems() {
		ProblemList<Quad> tmpProblemList = null;
		try {
			tmpProblemList = new ProblemList<Quad>(this.problemList);
		} catch (ProblemListInitialisationException problemListInitialisationException) {
			logger.debug(problemListInitialisationException.getStackTrace());
			logger.error(problemListInitialisationException.getMessage());
		}
		return tmpProblemList;
	}
	
	
}
