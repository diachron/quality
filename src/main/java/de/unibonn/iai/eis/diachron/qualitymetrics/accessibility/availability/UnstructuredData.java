package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.qualitymetrics.QualityMetric;
import de.unibonn.iai.eis.diachron.qualitymetrics.report.accessibility.URIProfile;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.CommonDataStructures;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.HTTPConnector;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.HTTPConnectorReport;

/**
 * @author Nikhil Patra
 * 
 * # unstructured  links= #dead links + # links without RDF support/#total URI
 * 
 * Pattern:  < _ _ ?o>
 * 
 * Check using jena isURI and then obtain source and prove whether RDF is available
 *
 * Metric Value :(#dead links + # links without RDF support)/#total URI
 * 
 */
public class UnstructuredData implements QualityMetric{

	static Logger logger = Logger.getLogger(UnstructuredData.class);

	private double unStructuredURI;
	private double deadURI;
	private double totalURI;
	private double metricValue;

	public void compute(Quad quad) {
		Date date = new Date();
		Timestamp t = new Timestamp(date.getTime());
		
		logger.trace("[Unstructured Data Metric - "+ t.toString() +"] Computing metric on : " + quad.asTriple());
		Node subject = quad.getSubject();

		if (HTTPConnector.isPossibleURL(subject) && (!CommonDataStructures.uriExists(subject.getURI()))){
			this.unStructuredDataChecker(this.buildURIProfile(subject, null));
		} else if (CommonDataStructures.uriExists(subject.getURI())){
			URIProfile profile = CommonDataStructures.getURIProfile(subject.getURI());
			if (profile.getHttpStatusCode() == 0) this.unStructuredDataChecker(this.buildURIProfile(subject, profile));
			else this.unStructuredDataChecker(profile);
		}

//		TODO: check if predicate needs to be checked for Unstructured Data - 
//		it does not make sense, since the publisher do not have any control on the schema
//		Node predicate = quad.getPredicate();
//
//		if (HTTPConnector.isPossibleURL(predicate) && (!CommonDataStructures.uriExists(predicate.getURI()))){
//			this.unStructuredDataChecker(this.buildURIProfile(predicate, null));
//		} else if (CommonDataStructures.uriExists(predicate.getURI())){
//			URIProfile profile = CommonDataStructures.getURIProfile(predicate.getURI());
//			if (profile.getHttpStatusCode() == 0) this.unStructuredDataChecker(this.buildURIProfile(predicate, profile));
//			else this.unStructuredDataChecker(profile);
//		}

		
		Node object = quad.getObject();

		if (HTTPConnector.isPossibleURL(object) && (!CommonDataStructures.uriExists(object.getURI()))){
			this.unStructuredDataChecker(this.buildURIProfile(object, null));
		} else if (CommonDataStructures.uriExists(object.getURI())){
			URIProfile profile = CommonDataStructures.getURIProfile(object.getURI());
			if (profile.getHttpStatusCode() == 0) this.unStructuredDataChecker(this.buildURIProfile(object, profile));
			else this.unStructuredDataChecker(profile);
		}

	
	}

	private void unStructuredDataChecker(URIProfile profile) {
		if (profile.isBroken()) this.deadURI++;
		if ((profile.getStructuredContentType().size() == 0) && (profile.isBroken() == false)) this.unStructuredURI++;
		if (profile.getHttpStatusCode() >= 400 || profile.getHttpStatusCode() < 600) profile.setBroken(true);
		this.totalURI++;
	}

	private URIProfile buildURIProfile(Node node, URIProfile p){
		//TODO: meaningful logging
		URIProfile profile = (p == null) ? new URIProfile() : p;
		if ((profile.getStructuredContentType().size() == 0) && (profile.isBroken() == false)){
			for(String content : CommonDataStructures.ldContentTypes){
				try {
					HTTPConnectorReport report = HTTPConnector.connectToURI(node, content, true, true); 
					if (((report.getResponseCode() >= 400) || (report.getResponseCode() < 600)) && report.isContentParsable())
						profile.addToStructuredContentType(content); 
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (ProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		CommonDataStructures.addToUriMap(node.getURI(), profile);
		return profile;
	}



	public String getName() {
		return "UnstructuredData";
	}


	public double metricValue() {
		this.metricValue= (this.deadURI + this.unStructuredURI) / this.totalURI;
		return this.metricValue;
	}


	public List<Triple> toDAQTriples() {
		// TODO Auto-generated method stub
		return null;
	}

}