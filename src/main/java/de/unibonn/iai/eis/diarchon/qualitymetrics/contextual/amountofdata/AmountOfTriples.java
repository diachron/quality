package de.unibonn.iai.eis.diarchon.qualitymetrics.contextual.amountofdata;

import java.util.List;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.qualitymetrics.QualityMetric;


/**
 * @author Nikhil Patra
 * 
 * Amount of triples class counts the number of triples present in the dataset.
 * It will check on the size of the dataset which is the lower bound for the number of triples present in the dataset.
 * 
 * Metric Value: According to The range in which the size lies in.
 *
 */
public class AmountOfTriples implements QualityMetric{

	protected double metricValue;
	protected int numTriples;
	
	public String getName() {
		return "AmountOfTriples";
	}
	
	// TODO: Find ranges @jerdeb
	public double metricValue() {
		
		long high       = 1000000000;
		long mediumHigh =   10000000;
		long mediumLow  =     500000;
		long low        =      10000;

		
		System.out.println(numTriples);
		
		//Assumed the following metric value for different sizes for the given criteria

		if (numTriples >= high) metricValue = 1;
		else if (numTriples < high       && numTriples >= mediumHigh) metricValue =  0.75;
		else if (numTriples < mediumHigh && numTriples >= mediumLow) metricValue =  0.5;
		else if (numTriples < mediumLow  && numTriples >= low) metricValue = 0.25;
		else metricValue = 0;
		return metricValue;
	}

	public List<Triple> toDAQTriples() {
		// TODO Auto-generated method stub
		return null;
	}

	public void compute(Quad quad) {
		if (quad.isTriple())
			numTriples++;
		
	}

}
