package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import java.util.List;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.qualitymetrics.QualityMetric;


/**
 * @author Nikhil Patra
 * 
 *Check if a SPARQL endpoint is available and returns a result. This can be done by checking for the  void:sparqlEndpoint value, and query the server; checking the result if it is in RDF
 *
 *check jena functions to connect to sparql endpoint
 *
 *Pattern:  < _  void:sparqlEndpoint ?o>
 *Obtain response from object 
 *
 *Metric Value :Iterating over the SPARQL Query results and check if any result is received 
	            metricValue is 1 if results received otherwise set to 0 
 */
public class SPARQLAccessibility implements QualityMetric {
	
	double metricValue;

	public void compute(Quad quad) {
		
	
		//Check for each triple if the property is void:sparqlEnpoint for the given dataset it is as below.
		String sparqlEndpoint ="http://rdfs.org/ns/void#sparqlEndpoint";
		//TODO find how to match for the property void:sparqlEndpoint
		
		if (quad.getPredicate().toString().equals(sparqlEndpoint))
		{
			//The query string
			String sparqlQuerystring= "select ?s where {?s ?p ?o}limit 1";
			Query query = QueryFactory.create(sparqlQuerystring);
			
			//Executing SPARQL Query and pointing to the triple store SPARQL Endpoint 
	        QueryExecution qexec = QueryExecutionFactory.sparqlService(quad.getObject().toString(), query);
	        
	       //Retrieving the SPARQL Query results
	        ResultSet results = qexec.execSelect();
	        
	       //Iterating over the SPARQL Query results and check if any result is received 
	       //set metricValue to 1 if results received otherwise set to 0 
	        if(results.hasNext()) 
	        	metricValue=1;
	        	else
	        	metricValue=0;
	                                                            
	        
	        //Release the resources used to query
	        qexec.close();
		}
		
		
		
	}

	public String getName() {
		
		return "SPARQLAccessibility";
	}

	public double metricValue() {
		
		return metricValue;
	}

	public List<Triple> toDAQTriples() {
		// TODO Auto-generated method stub
		return null;
	}

	
	

}
