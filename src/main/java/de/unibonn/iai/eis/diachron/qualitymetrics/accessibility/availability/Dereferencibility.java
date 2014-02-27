package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.List;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.qualitymetrics.DimensionNamesOntology;
import de.unibonn.iai.eis.diachron.qualitymetrics.QualityMetric;
import de.unibonn.iai.eis.diachron.qualitymetrics.report.accessibility.URIProfile;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.CommonDataStructures;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.HTTPConnector;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.HTTPConnectorReport;

/**
 * @author
 * 
 *         Approach:This metric calculates the number of valid redirects (303)
 *         according to LOD Principles
 * 
 *         Metric value: ratio of Number Derefernced URIs to Total Number of
 *         URIs
 * 
 */
public class Dereferencibility implements QualityMetric {

	static Logger logger = Logger.getLogger(Dereferencibility.class);

	private double metricValue = 0.0;
	private double totalURI = 0;
	private double dereferencedURI = 0;

	// Compute Function
	public void compute(Quad quad) {
		logger.trace("Computing Dereferencibility metric on : "
				+ quad.asTriple());
		Node subject = quad.getSubject();

		if (HTTPConnector.isPossibleURL(subject)
				&& (!CommonDataStructures.uriExists(subject.getURI()))) {
			this.dereferencabilityChecker(this.buildURIProfile(subject, null));
		} else if (CommonDataStructures.uriExists(subject.getURI())) {
			// The uri had been checked previously
			URIProfile profile = CommonDataStructures.getURIProfile(subject
					.getURI());
			if (profile.getHttpStatusCode() == 0)
				this.dereferencabilityChecker(this.buildURIProfile(subject,
						profile));
			else
				this.dereferencabilityChecker(profile);
		}

		// TODO: check if predicate needs to be checked for dereferencability -
		// it does not make sense, since the publisher do not have any control
		// on the schema
		// Node predicate = quad.getPredicate();
		// if (HTTPConnector.isPossibleURL(predicate) &&
		// (!CommonDataStructures.uriExists(predicate.getURI()))){
		// this.dereferencabilityChecker(this.buildURIProfile(predicate, null));
		// } else if (CommonDataStructures.uriExists(predicate.getURI())){
		// URIProfile profile =
		// CommonDataStructures.getURIProfile(predicate.getURI());
		// if (profile.getHttpStatusCode() == 0)
		// this.dereferencabilityChecker(this.buildURIProfile(predicate,
		// profile));
		// else this.dereferencabilityChecker(profile);
		// }

		Node object = quad.getObject();
		if (HTTPConnector.isPossibleURL(object)) {
			if (HTTPConnector.isPossibleURL(object)
					&& (!CommonDataStructures.uriExists(object.getURI()))) {
				this.dereferencabilityChecker(this
						.buildURIProfile(object, null));
			} else if (CommonDataStructures.uriExists(object.getURI())) {
				URIProfile profile = CommonDataStructures.getURIProfile(object
						.getURI());
				if (profile.getHttpStatusCode() == 0)
					this.dereferencabilityChecker(this.buildURIProfile(object,
							profile));
				else
					this.dereferencabilityChecker(profile);
			}
		}
	}

	public String getName() {
		return "Dereferencibility";
	}

	public double metricValue() {
		this.metricValue = this.dereferencedURI / this.totalURI;
		return this.metricValue;
	}

	public List<Triple> toDAQTriples() {
		// TODO Auto-generated method stub
		return null;
	}

	private URIProfile buildURIProfile(Node node, URIProfile p) {
		// TODO: meaningful logging
		URIProfile profile = (p == null) ? new URIProfile() : p;
		try {
			HTTPConnectorReport report = HTTPConnector
					.connectToURI(node, false); // We want to make sure that
												// there is no content
												// redirection, thus 3xx codes
												// are reported
			// TODO: do we require to check if the redirection actually works or
			// gives us a 404? in that case it would be a broken dereferencable
			// URI
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

	private void dereferencabilityChecker(URIProfile profile) {
		if (profile.getHttpStatusCode() == 303) {
			this.dereferencedURI++;
			profile.setValidDereferencableURI(true);
		}
		if (profile.getHttpStatusCode() >= 400
				|| profile.getHttpStatusCode() < 600)
			profile.setBroken(true);
		this.totalURI++;
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
