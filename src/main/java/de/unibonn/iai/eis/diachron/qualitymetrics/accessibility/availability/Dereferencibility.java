package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.apache.jena.riot.WebContent;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.datatypes.Response;
import de.unibonn.iai.eis.diachron.datatypes.Status;
import de.unibonn.iai.eis.diachron.datatypes.StatusCode;
import de.unibonn.iai.eis.diachron.qualitymetrics.QualityMetric;
import de.unibonn.iai.eis.diachron.qualitymetrics.report.accessibility.URIProfile;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.CommonDataStructures;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.HTTPConnector;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.HTTPConnectorReport;
import de.unibonn.iai.eis.diachron.vocabularies.DAQ;

/**
 * @author Jeremy Debatista
 * 
 * This metric calculates the number of valid redirects (303) or hashed links
 * according to LOD Principles
 * 
 * Based on: <a href="http://www.hyperthing.org/">Hyperthing - A linked data Validator</a>
 * 
 * @see <a href="http://dl.dropboxusercontent.com/u/4138729/paper/dereference_iswc2011.pdf">
 * Dereferencing Semantic Web URIs: What is 200 OK on the Semantic Web? - Yang et al.</a>
 * 
 */
public class Dereferencibility implements QualityMetric {

	private final Resource METRIC_URI = DAQ.DereferencibilityMetric;

	private static Logger logger = Logger.getLogger(Dereferencibility.class);

	private double metricValue = 0.0;
	private double totalURI = 0;
	private double dereferencedURI = 0;

	// Compute Function
	public void compute(Quad quad) {
		logger.trace("Computing Dereferencibility metric on : "+ quad.asTriple());
		//TODO: not to check if property is rdf:type
		//TODO: idea of new metric, is the publisher using well defined vocab, i.e check if the vocab has correct dereferencable URI
		//TODO: check if predicate needs to be checked for dereferencability - it does not make sense, since the publisher do not have any control on the schema

		try {
			if (!(quad.getPredicate().getURI().equals(RDF.type.getURI()))){
				this.startDereferencingProcess(quad.getSubject().getURI());//TODO: check if subject is a possible URI
				if (HTTPConnector.isPossibleURL(quad.getObject())) this.startDereferencingProcess(quad.getObject().getURI());
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	public double metricValue() {
		this.metricValue = this.dereferencedURI / this.totalURI;
		return this.metricValue;
	}

	public List<Triple> toDAQTriples() {
		// TODO Auto-generated method stub
		return null;
	}

	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	/**
	 * This is the process which analyses chains of HTTP Requests and responses.
	 * Given a Status object (RequestURI, StatusCode, TemporaryURI), this method will rewrite
	 * a new Status (URIr, StatusCode, URIt) derived by the defined rules in Yang et al.
	 * 
	 * @param Status datatype consisting of the RequestURI, StatusCode (optional), TemporaryURI (optional)
	 * @return new Status derived from the defined rules.
	 * 
	 * @throws MalformedURLException
	 * @throws ProtocolException
	 * @throws IOException
	 */
	private Status dereferenceNow(Status s) throws MalformedURLException, ProtocolException, IOException{
		Response r = new Response();
		String uri = (s.getTuri() == "") ? s.getUri() : s.getTuri(); // "" means empty response uri

		boolean isHash = false;
		if (uri.contains("#")) {
			uri = uri.substring(0,uri.indexOf("#"));
			isHash = true;
		}

		// making request
		HTTPConnectorReport rep = HTTPConnector.connectToURI(uri, WebContent.contentTypeRDFXML, false, false);

		// building response
		r = this.buildResponse(rep,isHash);

		Tuple t = new Tuple();
		t.r = r;
		t.s = s;

		if ((r.getStatusCode() == StatusCode.SC200) || 
				(r.getStatusCode() == StatusCode.SC4XX) || 
				(r.getStatusCode() == StatusCode.SC5XX)) return this.getRule(t); // we do not need to continue dereferecing
		else return this.dereferenceNow(this.getRule(t));
	}

	/**
	 * Given a Tuple of Status and Response, we generate a new Status
	 * as per the rules defined in the paper 
	 * Dereferencing Semantic Web URIs: What is 200 OK on the Semantic Web?
	 * 
	 * @see <a href="http://dl.dropboxusercontent.com/u/4138729/paper/dereference_iswc2011.pdf">
	 * Dereferencing Semantic Web URIs: What is 200 OK on the Semantic Web?</a>
	 * 
	 * @param A Tuple Status X Response
	 * @return A new status based on Rules
	 */
	private Status getRule(Tuple statXresp){
		Status s = statXresp.s;
		Response r = statXresp.r;
		Status newStatus = new Status();

		switch (r.getStatusCode()){
		case SC200 : // rule 2 and 7
			if (s.getStatusCode() == StatusCode.EMPTY) {
				//rule 2
				newStatus.setUri(s.getUri());
				newStatus.setStatusCode(StatusCode.SC200);
				newStatus.setTuri("");
			}
			if ((s.getStatusCode() == StatusCode.SC303) || (s.getStatusCode() == StatusCode.UNHASH)){
				//rule 7
				newStatus.setUri(s.getUri());
				newStatus.setStatusCode(s.getStatusCode());
				newStatus.setTuri(s.getTuri());
			}
			break;
		case SC303 : 
			if (s.getStatusCode() == StatusCode.EMPTY) {
				//rule 4
				newStatus.setUri(s.getUri());
				newStatus.setStatusCode(StatusCode.SC303);
				newStatus.setTuri(r.getUri());
			}
			if ((s.getStatusCode() == StatusCode.SC303) || (s.getStatusCode() == StatusCode.UNHASH)){
				//rule 8
				newStatus.setUri(s.getUri());
				newStatus.setStatusCode(s.getStatusCode());
				newStatus.setTuri(r.getUri());
			}
			break;
		case SC301 :
		case SC302 :
		case SC307 :
			if (s.getStatusCode() == StatusCode.EMPTY) {
				//rule 3
				newStatus.setUri(s.getUri());
				newStatus.setStatusCode(StatusCode.EMPTY);
				newStatus.setTuri(r.getUri());
			}
			if ((s.getStatusCode() == StatusCode.SC303) || (s.getStatusCode() == StatusCode.UNHASH)){
				//rule 8
				newStatus.setUri(s.getUri());
				newStatus.setStatusCode(s.getStatusCode());
				newStatus.setTuri(r.getUri());
			}
			break;
		case SC4XX :
		case SC5XX :
			//rule 5
			newStatus.setUri(s.getUri());
			newStatus.setStatusCode(StatusCode.BAD);
			newStatus.setTuri(r.getUri());
			break;
		case UNHASH :
			if ((s.getStatusCode() == StatusCode.SC303) || (s.getStatusCode() == StatusCode.UNHASH)){
				//rule 8
				newStatus.setUri(s.getUri());
				newStatus.setStatusCode(s.getStatusCode());
				newStatus.setTuri(r.getUri());
			} else {
				//rule 6
				newStatus.setUri(s.getUri());
				newStatus.setStatusCode(StatusCode.UNHASH);
				newStatus.setTuri(r.getUri());
			}
			break;
		default : break;
		}

		return newStatus;
	}

	/**
	 * Builds the Response (StatusCode, URI) which is used together with the Status to derived
	 * a new Status from the rules defined by Yang et al.
	 * 
	 * @param HTTPConnectorReport with all the necessary details for the URI connection
	 * @param A boolean (isHash) which is true when the URI passed is a hased URI
	 * @return Response object (StatusCode, URI)
	 */
	private Response buildResponse(HTTPConnectorReport rep, boolean isHash){
		Response r = new Response();
		StatusCode statusCode = (isHash) ? StatusCode.UNHASH : intToStatusCode(rep.getResponseCode());
		r.setStatusCode(statusCode);

		switch (statusCode){
		case SC301 : 
		case SC302 : 
		case SC303 :
		case SC307 : r.setUri(rep.getRedirectLocation()); break;
		case UNHASH: r.setUri(rep.getUri()); break; //this is the request URI unhashed
		case SC200 : 
		case SC4XX :
		case SC5XX : r.setUri(""); break;
		default : r.setUri(""); break;
		}
		return r;
	}

	/**
	 * Converts an HTTP Respond Code a StatusCode enum 
	 * 
	 * @param HTTP Response Code (integer)
	 * @return StatusCode object
	 */
	private StatusCode intToStatusCode(int i){
		if (i == 200) return StatusCode.SC200;
		if (i == 301) return StatusCode.SC301;
		if (i == 302) return StatusCode.SC302;
		if (i == 303) return StatusCode.SC303;
		if (i == 307) return StatusCode.SC307;
		if ((i >= 400) &&  (i <= 499)) return StatusCode.SC4XX;
		if ((i >= 500) &&  (i <= 599)) return StatusCode.SC5XX;	
		else return StatusCode.EMPTY;
	}

	/**
	 * Starts the dereferencing process for a given URI
	 * 
	 * @param URI to dereference
	 * @throws MalformedURLException
	 * @throws ProtocolException
	 * @throws IOException
	 */
	private void startDereferencingProcess(String uri) throws MalformedURLException, ProtocolException, IOException{
		Status s = new Status();
		s.setUri(uri);
		s.setStatusCode(StatusCode.EMPTY);
		s.setTuri(""); // "" means empty response uri

		Status ret = this.dereferenceNow(s);

		if ((ret.getStatusCode() == StatusCode.SC303) || (ret.getStatusCode() == StatusCode.UNHASH)) this.dereferencedURI++;
		this.totalURI++;
	}


	/**
	 * This protected class is required in order to
	 * create Dereferening Rules.
	 *
	 */
	protected class Tuple{
		Status s;
		Response r;
	}

}


