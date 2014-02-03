package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.qualitymetrics.QualityMetric;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.CommonDataStructures;


/**
 * @author 
 *
 * description of metric
 * 
 */
public class Dereferencibility implements QualityMetric{

	private double metricValue = 0.0;
	private double totalURI = 0;
	private double dereferencedURI = 0;


	//Compute Function 	
	public void compute(Quad quad) {
		Node subject = quad.getSubject();

		if (this.isAPossibleDereferencableURI(subject) && (!CommonDataStructures.uriExists(subject))){
			boolean isBroken = this.isBrokenURI(subject);
			CommonDataStructures.addToUriMap(subject, isBroken);
			if (!isBroken){
				this.dereferencedURI++;
			}
		}

		Node predicate = quad.getPredicate();
		if (this.isAPossibleDereferencableURI(predicate) && (!CommonDataStructures.uriExists(predicate))){
			boolean isBroken = this.isBrokenURI(predicate);
			CommonDataStructures.addToUriMap(predicate, isBroken);
			if (!isBroken){
				this.dereferencedURI++;
			}
		}

		Node object = quad.getObject();
		if (this.isAPossibleDereferencableURI(object) && (!CommonDataStructures.uriExists(object))){
			boolean isBroken = this.isBrokenURI(object);
			CommonDataStructures.addToUriMap(object, isBroken);
			if (!isBroken){
				this.dereferencedURI++;
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

	private boolean isBrokenURI(Node node){
		//TODO: log more meaningful exceptions
		Boolean isBroken = CommonDataStructures.uriExists(node) ? CommonDataStructures.isUriBroken(node) : null;
		if (isBroken != null)
			return isBroken;
		else {
			URL extUrl;
			try {
				extUrl = new URL(node.getURI());
			} catch (MalformedURLException e) {
				return true;
			}

			HttpURLConnection urlConn;
			try {
				urlConn = (HttpURLConnection) extUrl.openConnection();
			} catch (IOException e) {
				return true;
			}

			try {
				urlConn.setRequestMethod("GET");
			} catch (ProtocolException e) {
				return true;
			}

			int responseCode = 0;

			try {
				urlConn.connect();
				responseCode = urlConn.getResponseCode();
			} catch (IOException e) {
				return true;
			}

			if (responseCode >= 200 && responseCode < 400) {
				return false;
			} else {
				return true;
			}
		}
	}

	private boolean isAPossibleDereferencableURI(Node node){
		if ((node.isURI()) && ((node.getURI().startsWith("http")) || (node.getURI().startsWith("https")))){
			this.totalURI++;
			return true;
		} else return false;
	}
}
