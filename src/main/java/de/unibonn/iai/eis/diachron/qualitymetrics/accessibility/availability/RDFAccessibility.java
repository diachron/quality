/**
 * 
 */
package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import com.hp.hpl.jena.graph.Triple;

import de.unibonn.iai.eis.diachron.qualitymetrics.QualityMetric;

/**
 * @author natalja
 *
 */
public class RDFAccessibility implements QualityMetric {

	double metricValue;
	public void compute(Triple triple) {
		
		metricValue=0;
		//Array List containing the content types of RDF files 
		ArrayList<String> rdfContentTypes= new ArrayList<String>(Arrays.asList("application/rdf+xml", 
				                                                         "text/plain",
				                                                         "application/x-turtle",
				                                                         "text/rdf+n3"));
		
		//Check if the property is void:dataDump		
		String sparqldataDump = "http://rdfs.org/ns/void#dataDump";
		
		if(triple.getPredicate().toString().equals(sparqldataDump))
		{
						
			try {
				
				//Create connection and Connect				
				URI uri = new URI(triple.getObject().toString());
				HttpURLConnection connection = (HttpURLConnection)  uri.toURL().openConnection();
				connection.setRequestMethod("HEAD");
				connection.connect();
										
				//Getting the content type of the file in the specified URL
				String contentType = connection.getContentType();
				
				//Checking if the file type is a RDF and setting the metric value
				if (rdfContentTypes.contains(contentType))
					metricValue=1;
				else
					metricValue=0;
				connection.disconnect();
			   } catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			   }catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			   } catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
		}
		
		
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public double metricValue() {
		
		return metricValue;
	}

}
