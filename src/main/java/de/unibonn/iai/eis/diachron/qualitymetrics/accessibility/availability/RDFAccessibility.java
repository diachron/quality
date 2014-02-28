/**
 * 
 */
package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.UnknownHostException;
import java.util.List;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.qualitymetrics.QualityMetric;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.HTTPConnector;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.HTTPConnectorReport;
import de.unibonn.iai.eis.diachron.vocabularies.DAQ;
import de.unibonn.iai.eis.diachron.vocabularies.VOID;

/**
 * @author Jeremy Debattista
 * 
 *     Check if data dumps (void:dataDump) exists and are reachable and parsable.
 *      
 */
public class RDFAccessibility implements QualityMetric {

	private final Resource CATEGORY_URI = DAQ.Accessibility;
	private final Resource DIMENSION_URI = DAQ.Availability;
	private final Resource METRIC_URI = DAQ.RDFAvailabilityMetric;
	
	private double metricValue = 0.0d;
	private double countRDF = 0.0d;
	private double positiveRDF = 0.0d;

	public void compute(Quad quad) {
		// TODO Meaningful error logging
		if (quad.getPredicate().equals(VOID.dataDump)) {

			countRDF++;
			
			try {
				HTTPConnectorReport report = HTTPConnector.connectToURI(quad.getObject(), "", false, true);
				if (report.getResponseCode() == 200) positiveRDF++; 
			} catch (MalformedURLException e) {
			} catch (ProtocolException e) {
			} catch (UnknownHostException e) {
			} catch (IOException e) {
			}
		}

	}

	public double metricValue() {
		metricValue = positiveRDF / countRDF;

		return metricValue;
	}

	public List<Triple> toDAQTriples() {
		return null;
	}

	public Resource getMetricURI() {
		return this.METRIC_URI;
	}
	
	public Resource getDimensionURI() {
		return this.DIMENSION_URI;
	}

	public Resource getCategoryURI() {
		return this.CATEGORY_URI;
	}
}
