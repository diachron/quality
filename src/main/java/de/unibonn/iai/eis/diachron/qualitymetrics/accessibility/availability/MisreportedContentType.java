package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.qualitymetrics.QualityMetric;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.CommonDataStructures;

public class MisreportedContentType implements QualityMetric {

	
	
	private double misReportedType;
	private double correctReportedType;
	private HttpURLConnection urlConn;
	private String datasetURI;
	
	//Constructor to Initialize our count variables
	public MisreportedContentType() {
		 misReportedType = 0;
		 correctReportedType = 0;
		}
	public void compute(Quad quad) {
		
		String contentType;
		Node subject = quad.getSubject();
		datasetURI= subject.toString();
		
		if (!CommonDataStructures.uriExists(subject)){
			boolean isBroken = this.isBrokenURI(subject);
			CommonDataStructures.addToUriMap(subject, isBroken);
			if (!isBroken){
				
				//Getting the content type for the connection
				contentType= urlConn.getContentType();
				
				//Getting the content type for the given URI using RDFLanguages
				Lang lang  = RDFLanguages.filenameToLang(datasetURI);
				//if(!lang.equals(null))
				{
					System.out.println(!lang.equals(null) + " " + !CommonDataStructures.uriExists(subject) );
					if(lang.getContentType().toString().equals(contentType))
					correctReportedType++;
					else
					misReportedType++;
							
				}
			}
		}
		
		
	}
	private boolean isBrokenURI(Node node){
		//TODO: log more meaningful exceptions
		Boolean isBroken = CommonDataStructures.uriExists(node) ? CommonDataStructures.isUriBroken(node) : null;
		if (isBroken != null)
			return isBroken;
		else {
			URL extUrl;
			try {
				extUrl = new URL(node.getURI());
			} catch (MalformedURLException e) {
				return true;
			}

			
			try {
				urlConn = (HttpURLConnection) extUrl.openConnection();
			} catch (IOException e) {
				return true;
			}

			try {
				urlConn.setRequestMethod("GET");
			} catch (ProtocolException e) {
				return true;
			}

			int responseCode = 0;

			try {
				urlConn.connect();
				responseCode = urlConn.getResponseCode();
			} catch (IOException e) {
				return true;
			}

			if (responseCode >= 200 && responseCode < 400) {
				return false;
			} else {
				return true;
			}
		}
	}
	public String getName() {
		return "MisreportedContentType";
	}

	public double metricValue() {
		// Returns total no. of correct reported types/(correct + misreported types)
		double metricValue = correctReportedType/(misReportedType+correctReportedType);
		return metricValue;
	}

	public List<Triple> toDAQTriples() {
		// TODO Auto-generated method stub
		return null;
	}

}
