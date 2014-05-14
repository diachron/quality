/**
 * 
 */
package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.UnknownHostException;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.qualitymetrics.QualityMetric;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.HTTPConnector;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.HTTPConnectorReport;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;
import de.unibonn.iai.eis.diachron.vocabularies.VOID;

/**
 * @author Jeremy Debattista
 * 
 *     Check if data dumps (void:dataDump) exists and are reachable and parsable.
 *      
 */
public class RDFAccessibility extends AbstractQualityMetric {

	private final Resource METRIC_URI = DQM.RDFAvailabilityMetric;
	
	private double metricValue = 0.0d;
	private double countRDF = 0.0d;
	private double positiveRDF = 0.0d;

	public void compute(Quad quad) {
		// TODO Meaningful error logging
		if (quad.getPredicate().getURI().equals(VOID.dataDump.getURI())) {

			countRDF++;
			
			HTTPConnectorReport report = HTTPConnector.connectToURI(quad.getObject().getURI(), "", false, true);
			if (report.getResponseCode() == 200) positiveRDF++; 
		}

	}

	public double metricValue() {
		metricValue = positiveRDF / countRDF;

		return metricValue;
	}


	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	public ProblemList<?> getQualityProblems() {
		// TODO Auto-generated method stub
		return null;
	}
}
