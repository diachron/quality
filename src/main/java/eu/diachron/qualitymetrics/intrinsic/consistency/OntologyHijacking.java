package eu.diachron.qualitymetrics.intrinsic.consistency;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xerces.util.URI;
import org.apache.xerces.util.URI.MalformedURIException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import eu.diachron.semantics.vocabulary.DQM;
import eu.diachron.qualitymetrics.utilities.VocabularyReader;

/**
 * The Ontology Hijacking detects the redefinition by analyzing defined classes or 
 * properties in data set and looks of same definition in its respective vocabulary. 
 * 
 * Metric Value Range : [0 - 1]
 * Best Case : 0
 * Worst Case : 1
 * 
 * Note: This class uses utilities.VocabularyReader to download models (vocabularies) from
 * web. Though VocabularyReader has it own cache but it has some inherit performance/scalability issues.   
 *  
 * @author Muhammad Ali Qasmi
 * @date 10th June 2014
 * 
 */
public class OntologyHijacking implements QualityMetric{
        
        /**
         * Metric URI
         */
        private final Resource METRIC_URI = DQM.OntologyHijackingMetric;
        /**
         * logger static object
         */
        static Logger logger = Logger.getLogger(OntologyHijacking.class);
        /**
         * total number of locally defined classes or properties count 
         */
        protected double totalLocallyDefinedClassesOrPropertiesCount = 0;
        /**
         * total number of hijacked classes or properties found 
         */
        protected double hijackedClassesOrPropertiesCount = 0;
        /**
         * list of problematic quads
         */
        protected List<Quad> problemList = new ArrayList<Quad>();
        
        /**
         * Check if the given quad has predicate of URI with given fragment
         * 
         * @param predicate
         * @param fragment
         * @return true if predicate is URI with given fragment
         */
        protected boolean isDefinedClassOrProperty(Quad quad, String fragment){
                try {
                       if (quad.getPredicate().isURI()) { // predicate is a URI
                           URI tmpURI = new URI(quad.getPredicate().getURI());
                           return (tmpURI.getFragment() != null && tmpURI.getFragment().toLowerCase().equals(fragment)) ?  true : false;
                       }
                       
                } catch (MalformedURIException e) {
                        logger.debug(e.getStackTrace());
                        logger.error(e.getMessage());
                }
                return false;
        }
        
        /**
         * Detects if given node is defined in vocabulary or not
         * 
         * @param node
         * @return true - if given node is found in the vocabulary with property of RDF.type
         */
        protected boolean isHijacked(Node node){
                Model model = VocabularyReader.read(node.getURI());
                if (model != null){
                        if (model.getResource(node.getURI()).isURIResource()){
                                if ( model.getResource(node.getURI()).hasProperty(RDF.type)) {
                                     return true;   
                                }
                        }
                }
                return false;
        }
        
        /**
         * Filters quad triples that are locally defined in the data set.
         * Detects if filtered triples are already defined in vocabulary
         */
        
        public void compute(Quad quad) {
                try {
                    if (isDefinedClassOrProperty(quad, "type")){ // quad represent a locally defined statement
                            this.totalLocallyDefinedClassesOrPropertiesCount++; // increments defined class or property count
                            Node subject = quad.getSubject(); // retrieve subject
                            if (isHijacked(subject)){ 
                                    this.hijackedClassesOrPropertiesCount++; // increments redefined class or property count
                                    this.problemList.add(quad);
                            }
                    }
                    else if (isDefinedClassOrProperty(quad, "domain")){ // quad represent a locally defined statement
                            this.totalLocallyDefinedClassesOrPropertiesCount++; // increments defined class or property count
                            Node object = quad.getObject(); // retrieve object
                            if (isHijacked(object)){ 
                                    this.hijackedClassesOrPropertiesCount++; // increments redefined class or property count
                                    this.problemList.add(quad);
                            }
                    }
                }
                catch (Exception e) {
                     logger.debug(e.getStackTrace());
                     logger.error(e.getMessage());
                }
        }
        
        /**
         * Returns metric value for between 0 to 1. Where 0 as the best case and 1 as worst case 
         * @return double - range [0 - 1] 
         */
        
        public double metricValue() {
                if (this.totalLocallyDefinedClassesOrPropertiesCount <= 0) {
                        logger.warn("Total classes or properties count is ZERO");
                        return 0;
                }
                return (this.hijackedClassesOrPropertiesCount / this.totalLocallyDefinedClassesOrPropertiesCount);
        }

        /*
         * (non-Javadoc)
         * @see de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric#getMetricURI()
         */
        
        public Resource getMetricURI() {
                return this.METRIC_URI;
        }

        /*
         * (non-Javadoc)
         * @see de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric#getQualityProblems()
         */
        
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

}
