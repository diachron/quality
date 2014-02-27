/**
 * 
 */
package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.qualitymetrics.DimensionNamesOntology;
import de.unibonn.iai.eis.diachron.qualitymetrics.QualityMetric;

/**
 * @author natalja
 * 
 *         Check if a SPARQL endpoint is available and returns a result. This
 *         can be done by checking for the void:sparqlEndpoint value, and query
 *         the server; checking the result if it is in RDF
 * 
 *         check jena functions to connect to sparql endpoint
 * 
 *         Pattern: < _ void:sparqlEndpoint ?o> Obtain response from object
 * 
 *         Metric Value : Ratio of the positive RDF triple objects to the Number
 *         of RDF triple objects
 */
public class RDFAccessibility implements QualityMetric {

	private double metricValue = 0.0d;
	private double countRDF = 0.0d;
	private double positiveRDF = 0.0d;

	// Array List containing the content types of RDF files
	private final ArrayList<String> rdfContentTypes = new ArrayList<String>(
			Arrays.asList("application/rdf+xml", "text/plain",
					"application/x-turtle", "text/rdf+n3"));

	public void compute(Quad quad) {

		// Check if the property is void:dataDump
		String sparqldataDump = "http://rdfs.org/ns/void#dataDump";

		if (quad.getPredicate().toString().equals(sparqldataDump)) {

			// Count the number of URI's with void:dataDump predicate
			countRDF++;

			try {

				// Create connection and Connect
				URI uri = new URI(quad.getObject().toString());
				HttpURLConnection connection = (HttpURLConnection) uri.toURL()
						.openConnection();
				connection.setRequestMethod("HEAD");
				connection.connect();

				// Getting the content type of the file in the specified URL
				String contentType = connection.getContentType();

				// Checking if the file type is a RDF and increment the number
				// of positive counts
				if (rdfContentTypes.contains(contentType))
					positiveRDF++;

				connection.disconnect();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	public String getName() {

		return "RDFAccessibility";
	}

	public double metricValue() {

		// Return the ratio of the positive RDF triple objects to the Number of
		// RDF triple objects
		metricValue = positiveRDF / countRDF;

		return metricValue;
	}

	public List<Triple> toDAQTriples() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDimension() {
		return DimensionNamesOntology.ACCESIBILITY.AVAILABILITY;
	}

	public String getGroup() {
		return DimensionNamesOntology.ACCESIBILITY.GROUP_NAME;
	}

	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

}
