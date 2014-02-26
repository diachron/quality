package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
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
 * In "Misreported Content Type" metric we check if RDF/XML content is returned with a reported type other than application/rdf+xml
 * 
 * Approach: Check the content type returned by the URI and check if we can parse it. If it is parsible and not of application/rdf+xml
 *           then it is a misreported content type.
 *           
 * Metric Value : Total no. of correct reported types/(misreported types + correct)
 */
public class MisreportedContentType implements QualityMetric {

	
	//TODO check why parsing slows down at http://academic.research.microsoft.com/Author/53619090
	//TODO handle unknown host exception (people.comiles.eu,fb.comiles.eu)
	private double misReportedType;
	private double correctReportedType;
	
	static Logger logger = Logger.getLogger(MisreportedContentTypeTest.class);
	boolean followRedirects = true;
	String contentNegotiation = "application/rdf+xml";
	
	
	
	//Constructor to Initialize our count variables
	public MisreportedContentType() {
		 misReportedType = 0;
		 correctReportedType = 0;
		 BasicConfigurator.configure();
		}
	
	public void compute(Quad quad) {
		
		//TODO Check if the check should be done for subject,predicate,object
		
		//Check for Subject
		Node subject = quad.getSubject();
		//System.out.println(subject.toString()+ " ");
		
		if (HTTPConnector.isPossibleURL(subject) && (!CommonDataStructures.uriExists(subject.getURI())))
			{
			this.MisreportedConetentTypeChecker(subject);
			}
		
		//Check for object
		Node object = quad.getObject();
		//System.out.println(object.toString()+ " ");
				
		if (HTTPConnector.isPossibleURL(object) && (!CommonDataStructures.uriExists(object.getURI())))
			{
			this.MisreportedConetentTypeChecker(object);
			}
		
		//Check for Predicate
		Node predicate = quad.getPredicate();		
		if (HTTPConnector.isPossibleURL(predicate) && (!CommonDataStructures.uriExists(predicate.getURI())))
			{
			this.MisreportedConetentTypeChecker(predicate);
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
	
	  URIProfile profile = new URIProfile();
	  HTTPConnectorReport report;
	  try {
		  //Connect to URI and set the profile values
		  //TODO Check on "requires meaningful data" parameter  presently set to true
			report= HTTPConnector.connectToURI(node,contentNegotiation,followRedirects,true);
			profile.setHttpStatusCode(report.getResponseCode());
		
		//Get if content is parsible 
			boolean isParsible =report.isContentParsable();
			
		//If parsible then check for the content type
		if(isParsible)
		{
		//get the content type and compare if it is application/rdf+xml	
			boolean isLODtype = report.getContentType().equals(contentNegotiation);
			
			if(isLODtype)
			{
				correctReportedType++;
			}
			else
			{
				misReportedType++;
			}
		}
	  }catch(UnknownHostException e){
		  e.printStackTrace();
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
		
		CommonDataStructures.addToUriMap(node.toString(),profile);
		
		}
}



