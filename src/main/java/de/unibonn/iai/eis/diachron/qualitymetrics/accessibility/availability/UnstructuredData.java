package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import com.hp.hpl.jena.graph.Triple;

import de.unibonn.iai.eis.diachron.qualitymetrics.QualityMetric;

public class UnstructuredData implements QualityMetric{

	double deadURI;
	double totalURI;
	double notDeadURI;
	double metricValue;
	double linkWithoutRDFSupport;
	
	//Constructor Initializing the parameters
	public UnstructuredData() {
		
		this.deadURI = 0;
		this.totalURI = 0;
		this.notDeadURI = 0;
		this.metricValue = 0;
		this.linkWithoutRDFSupport = 0;
	}


	public void compute(Triple triple) {
		
		//RDF File content types reference : http://www.w3.org/2008/01/rdf-media-types
		//ntriples RDF file uses text/plain type address this issue when there is a normal text file
		//Array List containing the content types of RDF files 
		ArrayList<String> rdfContentTypes= new ArrayList<>(Arrays.asList("application/rdf+xml", 
						                                                  "text/plain",
						                                                  "application/x-turtle",
						                                                  "text/rdf+n3"));
		try{
			
		//the check for URI done by the URI class-- throws URIsyntax exception -exception has been handled 
			
		URI uriLink = new URI(triple.getObject().toString());
		
		
		//Check response
		HttpURLConnection connection = (HttpURLConnection)uriLink.toURL().openConnection();
		connection.setRequestMethod("GET");
		connection.connect();
		//Getting the content type of the file in the specified URL
		String contentType = connection.getContentType();
		
		//Check for RDF supporting objects
		if (rdfContentTypes.contains(contentType))
			linkWithoutRDFSupport++;
		
        //Check for dead links
		//Receive the response code for the URI 
		//Response code for the Links reference : http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html 
		int responseCode = connection.getResponseCode();
		if (responseCode>=400 && responseCode<600)
		{
			deadURI++;
			totalURI++;
		}
		else
		{
			notDeadURI++;
			totalURI++;
		}
		}
		catch (URISyntaxException e) {
			// Exception in Case the string given is not a URI
			e.printStackTrace();
			//Considering unknown host as dead links
		}catch(UnknownHostException e){
			deadURI++;
			totalURI++;
			e.printStackTrace();
			
		} catch(java.lang.ClassCastException e)
		{
			e.printStackTrace();
		}
		catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		//System.out.println("values"+deadURI+" "+linkWithoutRDFSupport+" "+notDeadURI+" "+totalURI);
	}

	
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public double metricValue() {
		
		//Number of Unstructured links
		metricValue= deadURI+ linkWithoutRDFSupport;
		return metricValue;
	}

}
