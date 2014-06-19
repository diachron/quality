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
public class UndefinedClassesOrProperties extends AbstractQualityMetric {
	/**
	 * Metric URI
	 */
	private final Resource METRIC_URI = DQM.UndefinedClassesOrPropertiesMetric;
	/**
	 * static logger object
	 */
	static Logger logger = Logger.getLogger(UndefinedClassesOrProperties.class);
	/**
	 * total number of undefined classes
	 */
	protected long undefinedClassesCount = 0;
	/**
	 * total number classes
	 */
	protected long totalClassesCount = 0;
	/**
	 * total number of undefined properties
	 */
	protected long undefinedPropertiesCount = 0;
	/**
	 * total number of properties
	 */
	protected long totalPropertiesCount = 0;
	/**
	 * list of problematic quads
	 */
	protected List<Quad> problemList = new ArrayList<Quad>();

	/**
	 * This method identifies whether a component (subject, predicate or object)
	 * of the given quad references an undefined class or property.
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
			
			if (predicate.isURI()) { // check if predicate is URI
				this.totalPropertiesCount++;
				// load model
				Model predicateModel = VocabularyReader
						.read(predicate.getURI());
				if (predicateModel == null) { // check if system is able to
												// retrieve model
				        System.out.println("predicate : " + predicate);
                        this.undefinedPropertiesCount++;
                        this.problemList.add(quad);
				} else {
					// search for URI resource from Model
					if (predicateModel.getResource(predicate.getURI())
							.isURIResource()) {
						// search for its domain and range properties
						if (!(predicateModel.getResource(predicate.getURI())
								.hasProperty(RDFS.domain) && predicateModel
								.getResource(predicate.getURI()).hasProperty(
										RDFS.range))) {
					        System.out.println("predicate : " + predicate);    
							this.undefinedPropertiesCount++;
							this.problemList.add(quad);
						}
					}
				}
			}


			if (predicate.isURI()){
			        URI tmpURI = new URI(predicate.getURI());
			        String fragment = tmpURI.getFragment();
			        if (fragment != null && fragment.toLowerCase().equals("type")){
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

		} catch (MalformedURIException exception){
	        logger.debug(exception);
            logger.error(exception.getMessage());
		} catch (Exception exception) {
			logger.debug(exception);
			logger.error(exception.getMessage());
		}

		logger.trace("compute() --Ended--");
	}

	/**
	 * This method returns metric value for the object of this class
	 * 
	 * @return (total number of undefined classes and undefined properties) / (
	 *         total number of classes and properties)
	 */
	@Override
	public double metricValue() {

		logger.trace("metricValue() --Started--");
		logger.debug("Number of Undefined Classes :: "
				+ this.undefinedClassesCount);
		logger.debug("Number of Classes :: " + this.totalClassesCount);
		logger.debug("Number of Undefined Properties :: "
				+ this.undefinedPropertiesCount);
		logger.debug("Number of Properties :: " + this.totalPropertiesCount);

		long tmpTotalUndefinedClassesAndUndefinedProperties = this.undefinedClassesCount
				+ this.undefinedPropertiesCount;
		long tmpTotalClassesAndProperties = this.totalClassesCount
				+ this.totalPropertiesCount;
		// return ZERO if total number of RDF literals are ZERO [WARN]
		if (tmpTotalClassesAndProperties <= 0) {
			logger.warn("Total number of classes and properties in given document is found to be zero.");
			return 0.0;
		}

		double metricValue = (double) tmpTotalUndefinedClassesAndUndefinedProperties
				/ tmpTotalClassesAndProperties;
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
	
	/**
     * Writes problematic instances to given stream
     * 
     * @param inputSource - name/URI of source
     * @param outputStream - stream where instances are to be written
     */
    public void outProblematicInstancesToStream(String inputSource, OutputStream outputStream) {
           
           Model model = ModelFactory.createDefaultModel();
           
           Resource qp = QR.UndefinedClassesOrProperties;
           qp.addProperty(QR.isDescribedBy, this.METRIC_URI);
           
           for(int i=0; i < this.problemList.size(); i++){
                   model.add(qp,QR.problematicThing,this.problemList.get(i).toString());     
           }
           
           model.add(QR.QualityReport,QR.computedOn,inputSource);
           model.add(QR.QualityReport,QR.hasProblem,qp);
           model.write(outputStream);
    }
}
