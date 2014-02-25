package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.qualitymetrics.QualityMetric;
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
		
		//Check for Subject
		Node subject = quad.getSubject();

		if (HTTPConnector.isPossibleURL(subject) && (!CommonDataStructures.uriExists(subject.getURI())))
			{
			this.MisreportedConetentTypeChecker(subject);
		
			}
		
		//Check for predicate
		Node predicate = quad.getSubject();

		if (HTTPConnector.isPossibleURL(predicate) && (!CommonDataStructures.uriExists(predicate.getURI())))
			{
			this.MisreportedConetentTypeChecker(predicate);
				
			}
		
		//Check for object
		Node object = quad.getSubject();

		if (HTTPConnector.isPossibleURL(object) && (!CommonDataStructures.uriExists(object.getURI())))
			{
			this.MisreportedConetentTypeChecker(object);
				
			}
				
	}
		
	
	public String getName() {
		return "MisreportedContentType";
	}

	public double metricValue() {
		// Returns total no. of correct reported types/(misreported types + correct)
		double metricValue = correctReportedType/(misReportedType+correctReportedType);
		return metricValue;
	}

	public List<Triple> toDAQTriples() {
		// TODO Auto-generated method stub
		return null;
	}
	private void MisreportedConetentTypeChecker(Node node){
		
			try {
				
				//Keeping Follow Redirects to true
				
				report = HTTPConnector.connectToURI(node, true);
				//System.out.println(report.isContentParsable());
				
				if(report.isContentParsable() && CommonDataStructures.ldContentTypes.contains(report.getContentType()))
				{
					correctReportedType++;
				}
				else
				{
					misReportedType++;
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
}


