package eu.diachron.qualitymetrics.accessibility.availability;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.URIProfile;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.qualitymetrics.utilities.CommonDataStructures;
import eu.diachron.qualitymetrics.utilities.HTTPConnector;
import eu.diachron.qualitymetrics.utilities.HTTPConnectorReport;
import eu.diachron.semantics.vocabulary.DQM;

/**
 * @author Nikhil Patra
 * 
 *         In "Misreported Content Type" metric we check if RDF/XML content is
 *         returned with a reported type other than application/rdf+xml
 * 
 *         Approach: Check the content type returned by the URI and check if we
 *         can parse it. If it is parsible and not of application/rdf+xml then
 *         it is a misreported content type.
 * 
 */
public class MisreportedContentType implements QualityMetric {

	private final Resource METRIC_URI = DQM.MisreportedContentTypesMetric;

	// TODO check why parsing slows down at
	// http://academic.research.microsoft.com/Author/53619090
	// TODO handle unknown host exception (people.comiles.eu,fb.comiles.eu)
	private double misReportedType;
	private double correctReportedType;

	static Logger logger = Logger.getLogger(MisreportedContentType.class);
	boolean followRedirects = true;
	String contentNegotiation = "application/rdf+xml";

	// Constructor to Initialize our count variables
	public MisreportedContentType() {
		misReportedType = 0;
		correctReportedType = 0;
		BasicConfigurator.configure();
	}

	public void compute(Quad quad) {

		// Check for Subject
		Node subject = quad.getSubject();
		// System.out.println(subject.toString()+ " ");

		if (HTTPConnector.isPossibleURL(subject)
				&& (!CommonDataStructures.uriExists(subject.getURI()))) {
			this.MisreportedConetentTypeChecker(subject);
		}

		// Check for object
		Node object = quad.getObject();
		// System.out.println(object.toString()+ " ");

		if (HTTPConnector.isPossibleURL(object)
				&& (!CommonDataStructures.uriExists(object.getURI()))) {
			this.MisreportedConetentTypeChecker(object);
		}

	}

	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	public double metricValue() {

		// Returns total no. of correct reported types/(misreported types +
		// correct)
		double metricValue = correctReportedType
				/ (misReportedType + correctReportedType);

		return metricValue;
	}

	private void MisreportedConetentTypeChecker(Node node) {

		URIProfile profile = new URIProfile();
		HTTPConnectorReport report;
		for (String content : CommonDataStructures.ldContentTypes) {
			try {
				// Connect to URI and set the profile values

				report = HTTPConnector.connectToURI(node, content,
						followRedirects, true);
				profile.setHttpStatusCode(report.getResponseCode());

				// Get if content is parsible
				boolean isParsible = report.isContentParsable();

				// If parsible then check for the content type
				if (isParsible) {
					// get the content type and compare if it is
					// application/rdf+xml
					boolean isLODtype = report.getContentType().equals(
							contentNegotiation);

					if (isLODtype) {
						correctReportedType++;
					} else {
						misReportedType++;
					}
				}
				// TODO Meaningful Logging
			} catch (UnknownHostException e) {

			} catch (MalformedURLException e) {
			} catch (ProtocolException e) {
			} catch (IOException e) {
			}

			CommonDataStructures.addToUriMap(node.toString(), profile);

		}
	}

	public ProblemList<?> getQualityProblems() {
		// TODO Auto-generated method stub
		return null;
	}

}
