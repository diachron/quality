package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.List;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.qualitymetrics.QualityMetric;
import de.unibonn.iai.eis.diachron.qualitymetrics.report.accessibility.URIProfile;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.CommonDataStructures;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.HTTPConnector;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.HTTPConnectorReport;

public class MisreportedContentType implements QualityMetric {

	
	
	private double misReportedType;
	private double correctReportedType;
	private HTTPConnectorReport report;
	
	//Constructor to Initialize our count variables
	public MisreportedContentType() {
		 misReportedType = 0;
		 correctReportedType = 0;
		}
	public void compute(Quad quad) {
		
		
		Node subject = quad.getSubject();

		if (HTTPConnector.isPossibleURL(subject) && (!CommonDataStructures.uriExists(subject.getURI()))){
			this.MisreportedConetentTypeChecker(this.buildURIProfile(subject, null));
		} else if (CommonDataStructures.uriExists(subject.getURI())){
			// The uri had been checked previously
			URIProfile profile = CommonDataStructures.getURIProfile(subject.getURI());
			if (profile.getHttpStatusCode() == 0) this.MisreportedConetentTypeChecker(this.buildURIProfile(subject, profile));
			else this.MisreportedConetentTypeChecker(profile);
			
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
	private URIProfile buildURIProfile(Node node, URIProfile p){
		//TODO: meaningful logging
		URIProfile profile = (p == null) ? new URIProfile() : p;
		try {
			report = HTTPConnector.connectToURI(node, false); // We want to make sure that there is no content redirection, thus 3xx codes are reported
			// TODO: do we require to check if the redirection actually works or gives us a 404? in that case it would be a broken dereferencable URI
			profile.setHttpStatusCode(report.getResponseCode());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		CommonDataStructures.addToUriMap(node.getURI(), profile);
		return profile;
	}

	private void MisreportedConetentTypeChecker(URIProfile profile){
		//Getting the content type for the given URI using RDFLanguages
				
		Lang lang  = RDFLanguages.filenameToLang(profile.getUri());
		if(lang != null)
		{
						
			if(lang.getContentType().toString().equals(report.getContentType().toString()))
			correctReportedType++;
			else
			misReportedType++;
					
		}	
	}
}


