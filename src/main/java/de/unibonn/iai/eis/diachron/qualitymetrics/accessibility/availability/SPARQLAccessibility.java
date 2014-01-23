package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;

import de.unibonn.iai.eis.diachron.qualitymetrics.QualityMetric;



public class SPARQLAccessibility implements QualityMetric {
	
	double metricValue;

	public void compute(Triple triple) {
		
		metricValue=0;
		//Check for each triple if the property is void:sparqlEnpoint for the given dataset it is as below.
		String sparqlEndpoint ="http://rdfs.org/ns/void#sparqlEndpoint";
		
		
		if (triple.getPredicate().toString().equals(sparqlEndpoint))
		{
			//The query string
			String sparqlQuerystring= "select ?s where {?s ?p ?o}limit 1";
			Query query = QueryFactory.create(sparqlQuerystring);
			
			//Executing SPARQL Query and pointing to the triple store SPARQL Endpoint 
	        QueryExecution qexec = QueryExecutionFactory.sparqlService(triple.getObject().toString(), query);
	        
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
		// TODO Auto-generated method stub
		return null;
	}

	public double metricValue() {
		// TODO Auto-generated method stub
		return metricValue;
	}

	
	

}
