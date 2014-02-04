package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.qualitymetrics.QualityMetric;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.CommonDataStructures;

public class UnstructuredData implements QualityMetric{

	private double deadURI;
	private double totalURI;
	private double metricValue;
	private double linkWithoutRDFSupport;
	private HttpURLConnection urlConn;

	//RDF File content types reference : http://www.w3.org/2008/01/rdf-media-types
	//ntriples RDF file uses text/plain type address this issue when there is a normal text file
	//Array List containing the content types of RDF files 
	private ArrayList<String> rdfContentTypes= new ArrayList<String>(Arrays.asList("application/rdf+xml", 
			"text/plain",
			"application/x-turtle",
			"application/n-triples",
			"text/turtle",
			"application/n3",
			"text/n3",
			"text/n-quads",
			"application/rdf+json",
			"text/x-nquads",
			"text/rdf+n3"));

	//Constructor Initializing the parameters
	public UnstructuredData() {

		this.deadURI = 0;
		this.totalURI = 0;
		this.metricValue = 0;
		this.linkWithoutRDFSupport = 0;
	}


	public void compute(Quad quad) {

		
			Node object = quad.getObject();
			
			if (this.isPossibleRDFSupportingURI(object) && (!CommonDataStructures.uriExists(object))){
				boolean isBroken = this.isBrokenURI(object);
				
				//checkWeb.
				CommonDataStructures.addToUriMap(object, isBroken);
				if (!isBroken){
					
					//Getting the content type of the file in the specified URL
					String contentType = urlConn.getContentType();
									
					//Check for RDF supporting objects
					if (!rdfContentTypes.contains(contentType))
						{
						this.linkWithoutRDFSupport++;
						}
					
				}
			}
Node subject = quad.getSubject();
			
			if (this.isPossibleRDFSupportingURI(subject) && (!CommonDataStructures.uriExists(subject))){
				boolean isBroken = this.isBrokenURI(subject);
				
				//checkWeb.
				CommonDataStructures.addToUriMap(subject, isBroken);
				if (!isBroken){
					
					//Getting the content type of the file in the specified URL
					String contentType = urlConn.getContentType();
									

					//Check for RDF supporting objects
					if (!rdfContentTypes.contains(contentType))
						{
						this.linkWithoutRDFSupport++;
						}
					
				}
			}
Node predicate = quad.getPredicate();
			
			if (this.isPossibleRDFSupportingURI(predicate) && (!CommonDataStructures.uriExists(predicate))){
				boolean isBroken = this.isBrokenURI(predicate);
				
				//checkWeb.
				CommonDataStructures.addToUriMap(predicate, isBroken);
				if (!isBroken){
					
					//Getting the content type of the file in the specified URL
					String contentType = urlConn.getContentType();
					
					//Check for RDF supporting objects
					if (!rdfContentTypes.contains(contentType))
						{
						this.linkWithoutRDFSupport++;
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
				//If Any other response code then it is considered dead
				this.deadURI++;
				return true;
			}
		}
	}
	
	private boolean isPossibleRDFSupportingURI(Node node)
	{
		if ((node.isURI()) && ((node.getURI().startsWith("http")) || (node.getURI().startsWith("https")))){
			this.totalURI++;
			return true;
		} else return false;
	}


	public String getName() {
		return "UnstructuredData";
	}


	public double metricValue() {
		metricValue= (deadURI+ linkWithoutRDFSupport)/totalURI;
		return metricValue;
	}


	public List<Triple> toDAQTriples() {
		// TODO Auto-generated method stub
		return null;
	}

}