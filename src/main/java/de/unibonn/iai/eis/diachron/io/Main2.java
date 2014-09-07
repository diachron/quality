
package de.unibonn.iai.eis.diachron.io;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;

import de.unibonn.iai.eis.diachron.datatypes.Object2Quad;
import de.unibonn.iai.eis.diachron.qualitymetrics.trust.verifiability.DigitalSignatures;

/**
 * This class is the main class to call the metrics and process it
 * @author Carlos
 *
 */
public class Main2 {
	
	/**
	 * This value determine the size of the call every time to the dataset
	 */
	private static int INCREMENT = 10000;
	
	public static void main(String[] args) {
		
		//SPARQL endpoint definition
		String service = "http://es.dbpedia.org/sparql";
		//String service = "http://lod.openlinksw.com/sparql/";
		
		DigitalSignatures sigMetric = new DigitalSignatures();
		
		int iterationNumber = 0;
		int variable = 0; //I use this variable to count the number of triples retrieved
		while (true) {
			System.out.println("*************        ITERATION NUMBER: " + iterationNumber + " TRIPLES: " +  iterationNumber*INCREMENT + "*******************");
			String query = "SELECT DISTINCT*"
					//+ "WHERE{ <http://dbpedia.org/res/ource/David_Beckham> ?p ?o }"
					//+ "WHERE{ <http://es.dbpedia.org/resource/Villaverde_Peñahorada__utonomica__2> ?p ?o }"					
					+ "{ ?s ?p ?o }"
					//+ "{{ ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } }}"
					//+ "ORDER BY ?p"
					+ " LIMIT "+ INCREMENT 
					+ " OFFSET "+ INCREMENT*iterationNumber;
			iterationNumber++;
			QueryExecution qe = QueryExecutionFactory.sparqlService(service,query);
			try {
				int counter = 0;
				ResultSet rs = qe.execSelect();
				while (rs.hasNext()) {
					counter++; //Increase the value of the counter that is used to finish the cicle
					variable++; //Increase the counter of triples
					
					Object2Quad obj = new Object2Quad(rs.next());
					//System.out.println(obj.getStatement());
					//TODO: create the metrics and compute with the value of every quad.
					sigMetric.compute(obj.getStatement());
				}
				if(counter == 0)//The next result not return any result then it finish
					break;
			} catch (QueryExceptionHTTP e) {
				System.out.println(service + " is Not working or is DOWN");
				break; //Close the cicle
			} finally {
				qe.close();
			} // end try/catch/finally
		}
		System.out.println("The number of triples read is: " + variable + ", Metric value: " + sigMetric.metricValue());
	} // end method
} // end class