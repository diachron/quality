package de.unibonn.iai.eis.diachron.qualitymetrics.utilities;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;

import org.apache.jena.riot.RiotException;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class HTTPConnector {

	static Logger logger = Logger.getLogger(HTTPConnector.class);
	
	private HTTPConnector(){}
	
	public static HTTPConnectorReport connectToURI(Node node, boolean followRedirects) throws MalformedURLException, ProtocolException, IOException{
		return connectToURI(node, "text/html", followRedirects);
	}
	
	public static HTTPConnectorReport connectToURI(Node node, String contentNegotiation, boolean followRedirects) throws MalformedURLException, ProtocolException, IOException {
		return connectToURI(node, contentNegotiation, followRedirects, false);
	}
	
	public static HTTPConnectorReport connectToURI(Node node, String contentNegotiation, boolean followRedirects, boolean requiresMeaningfulData) throws MalformedURLException, ProtocolException, IOException, UnknownHostException {
		HttpURLConnection.setFollowRedirects(followRedirects); 
		HTTPConnectorReport report = new HTTPConnectorReport();
		report.setNode(node);
		
		URL extUrl =  new URL(node.getURI());
		HttpURLConnection urlConn  = (HttpURLConnection) extUrl.openConnection();
		urlConn.setRequestMethod("GET");
		urlConn.setRequestProperty("Accept", contentNegotiation);
		report.setResponseCode(urlConn.getResponseCode());
		report.setContentType(urlConn.getContentType());
		
		if (((report.getResponseCode() >= 400) || (report.getResponseCode() < 600)) && (requiresMeaningfulData))
			report.setContentParsable(isContentParsable(urlConn));
		
		return report;
	}
	
	// TODO: check if there are HTML descriptions of a resource i.e. check for requesting (X)HTML - is this part of "No Structured Data metric"?
	
	public static boolean isPossibleURL(Node node){
		return ((node.isURI()) && ((node.getURI().startsWith("http")) || (node.getURI().startsWith("https"))));
	}
	
	private static boolean isContentParsable(HttpURLConnection connection){
		Model m = null;
		
		try {
			connection.connect();
			m = ModelFactory.createDefaultModel(); // create a new Model that will store the resultant RDF graph from Vapour
			m.read(connection.getInputStream(),null);
		} catch (RiotException re) {
			logger.error("Content cannot be Parsed");
			return false;
		} catch (IOException e) {
			logger.error("Error connecting or parsing input stream "+ e.getLocalizedMessage());
			return false;
		}
			
		return true;
	}
}
