package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import org.apache.jena.riot.WebContent;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.datatypes.Response;
import de.unibonn.iai.eis.diachron.datatypes.Status;
import de.unibonn.iai.eis.diachron.datatypes.StatusCode;
import de.unibonn.iai.eis.diachron.datatypes.URIProfile;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.CommonDataStructures;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.HTTPConnector;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.HTTPConnectorReport;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;

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
public class Dereferencibility extends AbstractQualityMetric {

	private final Resource METRIC_URI = DQM.DereferencibilityMetric;

	private static Logger logger = Logger.getLogger(Dereferencibility.class);

	private double metricValue = 0.0;
	private double totalURI = 0;
	private double dereferencedURI = 0;


	public void compute(Quad quad) {
		//logger.trace("Computing Dereferencibility metric on : "+ quad.asTriple());
		//TODO: not to check if property is rdf:type
		//TODO: idea of new metric, is the publisher using well defined vocab, i.e check if the vocab has correct dereferencable URI
		//TODO: check if predicate needs to be checked for dereferencability - it does not make sense, since the publisher do not have any control on the schema

		try {	
			if (!(quad.getPredicate().getURI().equals(RDF.type.getURI()))){ // we are currently ignoring triples ?s a ?o
				
				// Computing Subject
				if (HTTPConnector.isPossibleURL(quad.getSubject())){
					if (!CommonDataStructures.uriExists(quad.getSubject().getURI())){
						logger.info("Computing Dereferencibility on " + quad.getSubject().getURI());
						URIProfile uriProfile = this.startDereferencingProcess(quad.getSubject().getURI());
						CommonDataStructures.addToUriMap(quad.getSubject().getURI(), uriProfile);
						logger.info(" - " + uriProfile.getUriStatus().getStatusCode());
					} // if we computed the Dereferencibility we do not need to recompute or else add to total URIs
				}
				
				// Computing Object
				if (HTTPConnector.isPossibleURL(quad.getObject())){
					if (!CommonDataStructures.uriExists(quad.getObject().getURI())){
						logger.info("Computing Dereferencibility on " + quad.getObject().getURI());
						URIProfile uriProfile = this.startDereferencingProcess(quad.getObject().getURI());
						CommonDataStructures.addToUriMap(quad.getObject().getURI(), uriProfile);
						logger.info(" - " + uriProfile.getUriStatus().getStatusCode());
					} // if we computed the Dereferencibility we do not need to recompute or else add to total URIs
				}
			}
		} catch (MalformedURLException e) {
			// TODO more practical logging
			logger.warn("Malformed Exception " + quad.toString());
		} catch (ProtocolException e) {
			// TODO more practical logging
			logger.warn("Protocol Exception " + quad.toString());
		} catch (IOException e) {
			// TODO more practical logging
			logger.warn("IOException Exception " + quad.toString());
		} 
	}

	public double metricValue() {
		this.metricValue = this.dereferencedURI / this.totalURI;
		return this.metricValue;
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

		// making request - 1st we try with RDFXML content type
		HTTPConnectorReport rep = HTTPConnector.connectToURI(uri, WebContent.contentTypeRDFXML, false, false); 
		
		
		// if the first try fails, we try again to check if the content can be negotiated with any other LD Content type e.g. text/turtle
		// TODO: Check if this is actually required. this is required for those URI like http://acrux.weposolutions.de/xodx/?c=person&id=toni&a=rdf which returns a 404 when requesting rdf+xml but works with text/turtle.
		if (rep.getResponseCode() == 404){
			HTTPConnectorReport temp_rep = HTTPConnector.connectToURI(uri, null, false, false); 
			if (CommonDataStructures.ldContentTypes.contains(temp_rep.getContentType())){
				rep = temp_rep; // if temp_rep gives a content type which is part of ldContentTypes 
			}
		}

		// building response
		r = this.buildResponse(rep,isHash);

		Tuple t = new Tuple();
		t.r = r;
		t.s = s;

		if ((r.getStatusCode() == StatusCode.SC200) || 
				(r.getStatusCode() == StatusCode.SC4XX) || 
				(r.getStatusCode() == StatusCode.SC5XX) || 
				(r.getStatusCode() == StatusCode.BAD)) return this.getRule(t); // we do not need to continue dereferecing
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
		case BAD:
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
		if (i == -1) return StatusCode.BAD;
 		else return StatusCode.EMPTY;
	}

	/**
	 * Starts the dereferencing process for a given URI
	 * 
	 * @param URI to dereference
	 * @return A new URIProfile
	 * @throws MalformedURLException
	 * @throws ProtocolException
	 * @throws IOException
	 */
	private URIProfile startDereferencingProcess(String uri) throws MalformedURLException, ProtocolException, IOException{
		Status s = new Status();
		s.setUri(uri);
		s.setStatusCode(StatusCode.EMPTY);
		s.setTuri(""); // "" means empty response uri

		Status ret = this.dereferenceNow(s);
		
		URIProfile profile = new URIProfile();
		profile.setUri(uri);
		profile.setUriStatus(ret);
		
		if ((ret.getStatusCode() == StatusCode.SC4XX) || (ret.getStatusCode() == StatusCode.SC5XX) || (ret.getStatusCode() == StatusCode.BAD)) profile.setBroken(true);

		if ((ret.getStatusCode() == StatusCode.SC303) || (ret.getStatusCode() == StatusCode.UNHASH)) this.dereferencedURI++;
		this.totalURI++;
		
		return profile;
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


