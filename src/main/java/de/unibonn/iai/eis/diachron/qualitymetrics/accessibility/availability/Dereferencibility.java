package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.List;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.qualitymetrics.QualityMetric;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.CommonDataStructures;

public class Dereferencibility implements QualityMetric{


	private double metricValue;
	private double errorURI;
	private double totalURI;
	private double dereferencedURI;
	

	private CommonDataStructures checkedURISet = new CommonDataStructures();

	//Constructor initializes the number of deadURI and totalURI
	public Dereferencibility() {

		errorURI=0;
		totalURI=0;
		dereferencedURI=0;

	}

	//Compute Function 	
	public void compute(Quad quad) {
		
		
		//Boolean variables to keep track if the triple object and subject have a protocol scheme "http" or "https"
		boolean objectHasProtocolScheme;
		boolean subjectHasProtocolScheme;


		//Check if the Object is URI and If it has been already checked
		if(quad.getObject().isURI())
		{



			try {
				URI objURILink = new URI(quad.getObject().toString());
				//True if the object URI has a protocol else false
				objectHasProtocolScheme =objURILink.getScheme().equals("http") || objURILink.getScheme().equals("https");

				if(!checkedURISet.uriExists(objURILink) && objectHasProtocolScheme)
				{

					totalURI++;
					checkedURISet.addCheckedURISet(objURILink);
					//Create connection and connect 
					HttpURLConnection connection = (HttpURLConnection)objURILink.toURL().openConnection();
					connection.setRequestMethod("HEAD");
					connection.connect();

					//Check for dead links
					//Receive the response code for the URI 
					//Response code for the Links reference : http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html 
					int responseCode = connection.getResponseCode();
					if (responseCode>=400 && responseCode<600)
					{
						errorURI++;

					}
					else if(responseCode>=200 && responseCode<=300)
					{
						dereferencedURI++;

					}
					connection.disconnect();
				}
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}catch(UnknownHostException e){
				//Considering Unknown host also as dead links
				errorURI++;
			} catch(java.lang.ClassCastException e){

				e.printStackTrace();
			} 
			catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


		//Check if the subject is URI
		if(quad.getSubject().isURI())
		{


			try {   
				URI subURILink = new URI(quad.getSubject().toString());
			
				
				//True if the subject URI has a protocol else false
				subjectHasProtocolScheme =subURILink.getScheme().equals("http") || subURILink.getScheme().equals("https");
				//Check If it has been already checked
				if(!checkedURISet.uriExists(subURILink) && subjectHasProtocolScheme)
				{
					totalURI++;
					
					//Create connection and connect 
					HttpURLConnection connection = (HttpURLConnection)subURILink.toURL().openConnection();
					connection.setRequestMethod("HEAD");
					connection.connect();

					//Check for dead links
					//Receive the response code for the URI 
					//Response code for the Links reference : http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html 
					int responseCode = connection.getResponseCode();
					if (responseCode>=400 && responseCode<600)
					{
						errorURI++;

					}
					else if(responseCode>=200 && responseCode<=300)
					{
						dereferencedURI++;

					}
					connection.disconnect();
				}
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}catch(UnknownHostException e){
				//Considering Unknown host also as dead links
				errorURI++;

			} catch(java.lang.ClassCastException e){

				e.printStackTrace();
			} 
			catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	public String getName() {

		return "Dereferencibility";
	}


	public double metricValue() {

		//Return the Metric value
		metricValue = dereferencedURI / totalURI;
		return metricValue;
	}


	public List<Triple> toDAQTriples() {
		// TODO Auto-generated method stub
		return null;
	}

}
