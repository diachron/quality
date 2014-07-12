/**
 * 
 */
package de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.qualitymetrics.QualityMetric;
import de.unibonn.iai.eis.diachron.qualitymetrics.representational.understandability.EmptyAnnotationValue;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.HTTPConnector;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.HTTPConnectorReport;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;
import de.unibonn.iai.eis.diachron.vocabularies.QR;
import de.unibonn.iai.eis.diachron.vocabularies.VOID;

/**
 * @author Jeremy Debattista
 * 
 *     Check if data dumps (void:dataDump) exists and are reachable and parsable.
 *      
 */
public class RDFAccessibility extends AbstractQualityMetric {
	
	static Logger logger = Logger.getLogger(EmptyAnnotationValue.class);
	
	/**
	 * list of problematic quads
	 */
	protected List<Quad> problemList = new ArrayList<Quad>();

	private final Resource METRIC_URI = DQM.RDFAvailabilityMetric;
	
	private double metricValue = 0.0d;
	private double countRDF = 0.0d;
	private double positiveRDF = 0.0d;

	public void compute(Quad quad) {
		// TODO Meaningful error logging
		if (quad.getPredicate().getURI().equals(VOID.dataDump.getURI())) {

			countRDF++;
			
			HTTPConnectorReport report = HTTPConnector.connectToURI(quad.getObject().getURI(), "", false, true);
			if (report.getResponseCode() == 200) { positiveRDF++;}
			else {
				this.problemList.add(quad);
			}
			
		}

	}

	public double metricValue() {
		metricValue = positiveRDF / countRDF;

		return metricValue;
	}


	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric#
	 * getQualityProblems()
	 */
	@Override
	public ProblemList<?> getQualityProblems() {
		ProblemList<Quad> tmpProblemList = null;
		try {
			tmpProblemList = new ProblemList<Quad>(this.problemList);
		} catch (ProblemListInitialisationException problemListInitialisationException) {
			logger.debug(problemListInitialisationException.getStackTrace());
			logger.error(problemListInitialisationException.getMessage());
		}
		return tmpProblemList;
	}
	
	/**
     * Writes problematic instances to given stream
     * 
     * @param inputSource - name/URI of source
     * @param outputStream - stream where instances are to be written
     */
    public void outProblematicInstancesToStream(String inputSource, OutputStream outputStream) {
           
           Model model = ModelFactory.createDefaultModel();
           
           Resource qp = QR.RDFAccessibilityProblem;
           qp.addProperty(QR.isDescribedBy, this.METRIC_URI);
           
           for(int i=0; i < this.problemList.size(); i++){
                   model.add(qp,QR.problematicThing,this.problemList.get(i).toString());     
           }
           
           model.add(QR.QualityReport,QR.computedOn,inputSource);
           model.add(QR.QualityReport,QR.hasProblem,qp);
           model.write(outputStream);
    }
}
