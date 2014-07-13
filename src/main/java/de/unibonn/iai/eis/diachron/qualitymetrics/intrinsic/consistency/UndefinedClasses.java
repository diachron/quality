package de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.consistency;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xerces.util.URI;
import org.apache.xerces.util.URI.MalformedURIException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.VocabularyReader;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;
import de.unibonn.iai.eis.diachron.vocabularies.QR;

/**
 * Detects undefined classes and properties from data set by checking for 
 * its definition in their respective referred vocabulary.
 * 
 * Metric Value Range : [0 - 1]
 * Best Case : 0
 * Worst Case : 1
 * 
 * @author Muhammad Ali Qasmi
 * @date 11th March 2014
 */
public class UndefinedClasses extends AbstractQualityMetric {
	
	final  String RDF_PREFIX="http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	/**
	 * Metric URI
	 */
	private final Resource METRIC_URI = DQM.UndefinedClassesMetric;
	/**
	 * static logger object
	 */
	static Logger logger = Logger.getLogger(UndefinedClasses.class);
	/**
	 * total number of undefined classes
	 */
	protected long undefinedClassesCount = 0;
	/**
	 * total number classes
	 */
	protected long totalClassesCount = 0;
	
	
	/**
	 * list of problematic quads
	 */
	protected List<Quad> problemList = new ArrayList<Quad>();

	/**
	 * This method identifies whether a component (subject, predicate or object)
	 * of the given quad references an undefined class .
	 * 
	 * @param quad
	 *            - to be identified
	 */
	@Override
	public void compute(Quad quad) {
		
		

		logger.trace("compute() --Started--");

		try {

			Node predicate = quad.getPredicate(); // retrieve predicate
			Node object = quad.getObject(); // retrieve object
			
						
				String tmpURI = predicate.getURI();
			
              
                if (tmpURI != null && 
                                (tmpURI.equals(RDF.type.toString()) || 
                                tmpURI.equals(RDFS.domain.toString()) ||
                                tmpURI.equals(RDFS.range.toString()) ||
                                tmpURI.equals(RDFS.subPropertyOf.toString())||
                                tmpURI.equals(OWL.allValuesFrom.toString())||
                                tmpURI.equals(OWL.someValuesFrom.toString())||
                                tmpURI.equals(OWL.equivalentClass.toString())||                               		
                                tmpURI.equals(OWL.complementOf.toString())||	
                                tmpURI.equals(OWL.oneOf.toString())||
                                tmpURI.equals(OWL.disjointWith.toString())
                                		) ){
                
                
                        if (object.isURI()) { // check if object is URI (not blank or
                                // literal)
                            this.totalClassesCount++;
                            // load model
                            Model objectModel = VocabularyReader.read(object.getURI());
                            if (objectModel == null) { // check if system is able to
                                                        // retrieve model
                                    this.undefinedClassesCount++;
                                    this.problemList.add(quad);
                            } else {
                                 // search for URI resource from Model
                                    if (!objectModel.getResource(object.getURI())
                                                    .isURIResource()) {
                                        this.undefinedClassesCount++;
                                        this.problemList.add(quad);
                                    }      
                            }
                        }
                } 
		}
	
			

		 catch (Exception exception) {
			logger.debug(exception);
			logger.error(exception.getMessage());
		}

		logger.trace("compute() --Ended--");
	}

	/**
	 * This method returns metric value for the object of this class
	 * 
	 * @return (total number of undefined classes ) / (
	 *         total number of classes )
	 */
	@Override
	public double metricValue() {

		logger.trace("metricValue() --Started--");
		logger.debug("Number of Undefined Classes :: "
				+ this.undefinedClassesCount);
		logger.debug("Number of Classes :: " + this.totalClassesCount);
	

		long tmpTotalUndefinedClasses = this.undefinedClassesCount;
		long tmpTotalClasses = this.totalClassesCount;
		// return ZERO if total number of RDF literals are ZERO [WARN]
		if (tmpTotalClasses <= 0) {
			logger.warn("Total number of classes  in given document is found to be zero.");
			return 0.0;
		}

		double metricValue = (double) tmpTotalUndefinedClasses
				/ tmpTotalClasses;
		logger.debug("Metric Value :: " + metricValue);
		logger.trace("metricValue() --Ended--");
		return metricValue;
	}

	/**
	 * Returns Metric URI
	 * 
	 * @return metric URI
	 */
	@Override
	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	/**
	 * Returns list of problematic Quads
	 * 
	 * @return list of problematic quad
	 */
	@Override
	public ProblemList<?> getQualityProblems() {
		ProblemList<Quad> tmpProblemList = null;
		try {
			tmpProblemList = new ProblemList<Quad>(this.problemList);
		} catch (ProblemListInitialisationException problemListInitialisationException) {
			logger.debug(problemListInitialisationException);
			logger.error(problemListInitialisationException.getMessage());
		}
		return tmpProblemList;
	}
	
}

