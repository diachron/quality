package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.List;
import com.hp.hpl.jena.graph.Triple;

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
public void compute(Triple triple) {
		
	//Check if the Object is URI and If it has been already checked
	if(triple.getObject().isURI())
	{
		
		
		
	try {
		   URI objURILink = new URI(triple.getObject().toString());
		   
		   if(!checkedURISet.uriExists(objURILink))
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
			
			e.printStackTrace();
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
		if(triple.getSubject().isURI())
		{
			
		
		try {   
			   URI subURILink = new URI(triple.getSubject().toString());
			   //Check If it has been already checked
			   if(!checkedURISet.uriExists(subURILink))
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
				
				e.printStackTrace();
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
