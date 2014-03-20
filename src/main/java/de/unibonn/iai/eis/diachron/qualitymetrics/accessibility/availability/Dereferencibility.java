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
 * Based on: http://www.hyperthing.org/ 
 * and http://dl.dropboxusercontent.com/u/4138729/paper/dereference_iswc2011.pdf
 * 
 */
public class Dereferencibility implements QualityMetric {

	/**
	 * HTTP URI Dereferencing Model
	 * 
	 * 	1) Take the URI resource and make a RDF/XML request [build the Status]
	 *	2) Get Response
	 *	3) Map to rules and get a new status
	 *	4) Take the temp resource and recursively determine if the deferencing succeeded
	 */

	private final Resource METRIC_URI = DAQ.DereferencibilityMetric;

	private static Logger logger = Logger.getLogger(Dereferencibility.class);

	private double metricValue = 0.0;
	private double totalURI = 0;
	private double dereferencedURI = 0;


	public Status hyperthing(Status s) throws MalformedURLException, ProtocolException, IOException{
		Response r = new Response();
		String uri = (s.turi == "") ? s.uri : s.turi; // "" means empty response uri

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

		if ((r.sc == StatusCode.SC200) || (r.sc == StatusCode.SC4XX) || (r.sc == StatusCode.SC5XX)) return this.derefRule(t); // we do not need to continue dereferecing
		else return this.hyperthing(this.derefRule(t));
	}

	private Status derefRule(Tuple statXresp){
		Status s = statXresp.s;
		Response r = statXresp.r;
		Status newStatus = new Status();

		switch (r.sc){
		case SC200 : // rule 2 and 7
			if (s.sc == StatusCode.EMPTY) {
				//rule 2
				newStatus.uri = s.uri;
				newStatus.sc = StatusCode.SC200;
				newStatus.turi = "";
			}
			if ((s.sc == StatusCode.SC303) || (s.sc == StatusCode.UNHASH)){
				//rule 7
				newStatus.uri = s.uri;
				newStatus.sc = s.sc;
				newStatus.turi = s.turi;
			}
			break;
		case SC303 : 
			if (s.sc == StatusCode.EMPTY) {
				//rule 4
				newStatus.uri = s.uri;
				newStatus.sc = StatusCode.SC303;
				newStatus.turi = r.uri;
			}
			if ((s.sc == StatusCode.SC303) || (s.sc == StatusCode.UNHASH)){
				//rule 8
				newStatus.uri = s.uri;
				newStatus.sc = s.sc;
				newStatus.turi = r.uri;
			}
			break;
		case SC301 :
		case SC302 :
		case SC307 :
			if (s.sc == StatusCode.EMPTY) {
				//rule 3
				newStatus.uri = s.uri;
				newStatus.sc = StatusCode.EMPTY;
				newStatus.turi = r.uri;
			}
			if ((s.sc == StatusCode.SC303) || (s.sc == StatusCode.UNHASH)){
				//rule 8
				newStatus.uri = s.uri;
				newStatus.sc = s.sc;
				newStatus.turi = r.uri;
			}
			break;
		case SC4XX :
		case SC5XX :
			//rule 5
			newStatus.uri = s.uri;
			newStatus.sc = StatusCode.BAD;
			newStatus.turi = "";
			break;
		case UNHASH :
			if ((s.sc == StatusCode.SC303) || (s.sc == StatusCode.UNHASH)){
				//rule 8
				newStatus.uri = s.uri;
				newStatus.sc = s.sc;
				newStatus.turi = r.uri;
			} else {
			//rule 6
				newStatus.uri = s.uri;
				newStatus.sc = StatusCode.UNHASH;
				newStatus.turi = r.uri;
			}
			break;
		default : break;
		}

		return newStatus;
	}

	private Response buildResponse(HTTPConnectorReport rep, boolean isHash){
		Response r = new Response();
		StatusCode statusCode = (isHash) ? StatusCode.UNHASH : intToStatusCode(rep.getResponseCode());
		r.sc = statusCode;

		switch (statusCode){
		case SC301 : 
		case SC302 : 
		case SC303 :
		case SC307 : r.uri = rep.getRedirectLocation(); break;
		case UNHASH: r.uri = rep.getUri(); break; //this is the request URI unhashed
		case SC200 : 
		case SC4XX :
		case SC5XX : r.uri = ""; break;
		default : r.uri = ""; break;
		}
		return r;
	}

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


	public void testHT(String uri) throws MalformedURLException, ProtocolException, IOException{
		Status s = new Status();
		s.uri = uri;
		s.sc = StatusCode.EMPTY;
		s.turi = ""; // "" means empty response uri

		Status ret = this.hyperthing(s);
		
		//System.out.println("("+ret.uri+","+ret.sc+","+ret.turi+")");
		if ((ret.sc == StatusCode.SC303) || (ret.sc == StatusCode.UNHASH)) this.dereferencedURI++;
		
		this.totalURI++;
	}
	

	// Compute Function
	public void compute(Quad quad) {
		logger.trace("Computing Dereferencibility metric on : "+ quad.asTriple());
		//TODO: not to check if property is rdf:type
		//TODO: idea of new metric, is the publisher using well defined vocab, i.e check if the vocab has correct dereferencable URI
		//TODO: check if predicate needs to be checked for dereferencability - it does not make sense, since the publisher do not have any control on the schema
	
		if (!(quad.getPredicate().getURI().equals(RDF.type.getURI()))){
			try {
				this.testHT(quad.getSubject().getURI());
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //TODO: check if subject is a possible URI
			if (HTTPConnector.isPossibleURL(quad.getObject()))
				try {
					this.testHT(quad.getObject().getURI());
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

	
	}

	public double metricValue() {
		System.out.println(this.dereferencedURI);
		System.out.println(this.totalURI);
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



	protected class Status{
		String uri;
		StatusCode sc;
		String turi;
	}

	protected class Response{
		StatusCode sc;
		String uri;
	}

	protected enum StatusCode{
		SC200,SC301,SC302,SC303,SC307,SC4XX,SC5XX,UNHASH,BAD,ANY,EMPTY;
	}

	protected class Tuple{
		Status s;
		Response r;
	}

}


