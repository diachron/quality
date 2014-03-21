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

	private static Logger logger = Logger.getLogger(HTTPConnector.class);
	
	private HTTPConnector(){}

	
	/**
	 * Connects to a URI via HTTP using standard text/html content negotiation.
	 * If followRedirects is set to true, then the HTTP connector will follow all 
	 * possible redirects
	 * 
	 * @param node
	 * @param followRedirects
	 * @return An HTTP report about the URI
	 * @throws MalformedURLException
	 * @throws ProtocolException
	 * @throws IOException
	 */
	@Deprecated 
	public static HTTPConnectorReport connectToURI(Node node, boolean followRedirects) throws MalformedURLException, ProtocolException, IOException{
		return connectToURI(node, "text/html", followRedirects);
	}
		
	/**
	 * Connects to a URI via HTTP using a defined content negotiation.
	 * If followRedirects is set to true, then the HTTP connector will follow all 
	 * possible redirects
	 * 
	 * @param node
	 * @param contentNegotiation
	 * @param followRedirects
	 * @return An HTTP report about the URI
	 * @throws MalformedURLException
	 * @throws ProtocolException
	 * @throws IOException
	 */
	@Deprecated
	public static HTTPConnectorReport connectToURI(Node node, String contentNegotiation, boolean followRedirects) throws MalformedURLException, ProtocolException, IOException {
		return connectToURI(node, contentNegotiation, followRedirects, false);
	}
	
	/**
	 * Connects to a URI via HTTP using a defined content negotiation.
	 * If followRedirects is set to true, then the HTTP connector will follow all 
	 * possible redirects
	 * If requiresMeaningfulData is set to true, then it tries to parse the
	 * content data using a Jena Model
	 * 
	 * @param node
	 * @param contentNegotiation
	 * @param followRedirects
	 * @param requiresMeaningfulData
	 * @return An HTTP report about the URI
	 * @throws MalformedURLException
	 * @throws ProtocolException
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	@Deprecated
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
		report.setRedirectLocation(urlConn.getHeaderField("Location"));
		
		if (((report.getResponseCode() < 400) || (report.getResponseCode() >= 600)) && (requiresMeaningfulData))
			report.setContentParsable(isContentParsable(urlConn));
		
		return report;
	}
	
	public static HTTPConnectorReport connectToURI(String uri, String contentNegotiation, boolean followRedirects, boolean requiresMeaningfulData) {
		HttpURLConnection.setFollowRedirects(followRedirects); 
		HTTPConnectorReport report = new HTTPConnectorReport();
		report.setUri(uri);

		try{
			URL extUrl =  new URL(uri);//new URL(node.getURI());
			HttpURLConnection urlConn  = (HttpURLConnection) extUrl.openConnection();
			urlConn.setRequestMethod("GET");
			if (contentNegotiation != null) urlConn.setRequestProperty("Accept", contentNegotiation);
			report.setResponseCode(urlConn.getResponseCode());
			report.setContentType(urlConn.getContentType());
			report.setRedirectLocation(urlConn.getHeaderField("Location"));

			if (((report.getResponseCode() < 400) || (report.getResponseCode() >= 600)) && (requiresMeaningfulData))
				report.setContentParsable(isContentParsable(urlConn));
		} catch (MalformedURLException mue) {
			report.setResponseCode(-1);
			report.setContentType("");
			logger.warn("Malformed Exception " + mue.getLocalizedMessage());
		} catch (UnknownHostException uhe) {
			report.setResponseCode(-1);
			report.setContentType("");
			logger.warn("Unknown Host Exception " + uhe.getLocalizedMessage());
		} catch (ProtocolException pe) {
			report.setResponseCode(-1);
			report.setContentType("");
			logger.warn("Protocol Exception " + pe.getLocalizedMessage());
		} catch (IOException ioe) {
			report.setResponseCode(-1);
			report.setContentType("");
			logger.warn("IO Exception " + ioe.getLocalizedMessage());
		}
		return report;
	}
	
	
	// TODO: check if there are HTML descriptions of a resource i.e. check for requesting (X)HTML - is this part of "No Structured Data metric"?
	
	/**
	 * Checks if a Jena Node is a possible URL. 
	 * The conditions for this to be true is that a node is a URI and that it has 
	 * the http or https protocol.
	 * 
	 * @param node
	 * @return True if a Node is a possible URL
	 */
	public static boolean isPossibleURL(Node node){
		//TODO: add more protocols
		return ((node.isURI()) && ((node.getURI().startsWith("http")) || (node.getURI().startsWith("https"))));
	}
	
	/**
	 * Checks if content is parsable by a Jena Model.
	 * 
	 * @param connection
	 * @return True if content is parsable
	 */
	private static boolean isContentParsable(HttpURLConnection connection){
		Model m = null;
		
		try {
			connection.connect();
			m = ModelFactory.createDefaultModel(); // create a new Model that will store the resultant RDF graph from Vapour
			m.read(connection.getInputStream(),null); //TODO: read according to negotiated content type
		} catch (RiotException re) {
			logger.error(connection.getURL() + " content cannot be parsed");
			return false;
		} catch (IOException e) {
			logger.error("Error connecting or parsing input stream "+ e.getLocalizedMessage());
			return false;
		}
			
		return true;
	}
}
