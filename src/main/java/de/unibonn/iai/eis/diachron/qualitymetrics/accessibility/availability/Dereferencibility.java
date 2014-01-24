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

public class Dereferencibility implements QualityMetric{
	
	double metricValue;

	//Constructor initializes the number of deadURI and totalURI
	public Dereferencibility() {
		super();
		dereferencableURI=0;
		totalURI=0;
		notDeadURI=0;
		
	}

	double dereferencableURI;
	double totalURI;
	double notDeadURI;
	
	public void compute(Triple triple) {
		
		try {
			//Check for the URI done by the URI class and throws exception in case of URI syntax mismatch
		
			URI uriLink = new URI(triple.getObject().toString());
			
			
			//Create connection and connect 
			HttpURLConnection connection = (HttpURLConnection)uriLink.toURL().openConnection();
			connection.setRequestMethod("HEAD");
			connection.connect();
			
			//Check for dead links
			//Receive the response code for the URI 
			//Response code for the Links reference : http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html 
			int responseCode = connection.getResponseCode();
			if (responseCode>=400 && responseCode<600)
			{
				dereferencableURI++;
				totalURI++;
			}
			else
			{
				notDeadURI++;
				totalURI++;
			}
			connection.disconnect();
			
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//Considering Unknown host also as dead links
		}catch(UnknownHostException e){
			dereferencableURI++;
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
		
		
		
	}

	
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public double metricValue() {
		//Return the Metric value
		metricValue = notDeadURI / totalURI;
		return metricValue;
	}


	public List<Triple> toDAQTriples() {
		// TODO Auto-generated method stub
		return null;
	}

}
