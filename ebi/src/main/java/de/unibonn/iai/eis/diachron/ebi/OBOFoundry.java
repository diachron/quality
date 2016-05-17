package de.unibonn.iai.eis.diachron.ebi;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.semantics.EBIQM;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;


/**
 * Detects non reputable resources by retrieving URI of resources from
 * data sets and prefix match with "http://purl.obolibrary.org/obo/" .
 * 
 * This metric is Specific to the EBI use-case
 *  
 * Metric Value Range : [0 - 1]
 * Worst Case : 0
 * Best Case : 1
 * 
 * @author Muhammad Ali Qasmi
 * @date 12th June 2014
 * @category EBI
 */
public class OBOFoundry extends AbstractQualityMetric{
        /**
         * URI for OBO Foundry 
         */
        protected String OBO_Foundry_URI =  "http://purl.obolibrary.org/obo";
        /**
         * Metric URI
         */
        private final Resource METRIC_URI = EBIQM.OBOFoundryMetric;
        /**
         * logger static object
         */
        static Logger logger = Logger.getLogger(OBOFoundry.class);
        /**
         * list of problematic quads
         */
        protected List<Quad> problemList = new ArrayList<Quad>();
        /**
         * total number of resources 
         */
        protected long totalResources = 0;
        /**
         * total number of not reputable resources
         */
        protected long totalNotReputableResources = 0;
        
        /**
         * Retrieves URI (with path) of each subject, predicate and object.
         * Checks is those URI (with path) prefix matches with OBO_Foundry_URI
         */
        
        public void compute(Quad quad) {
                try {
                        
                        boolean isProblematicQuad = false;
                        if (quad.getSubject().isURI()){
                                this.totalResources++;    
                                if (!(quad.getSubject().getURI().startsWith(OBO_Foundry_URI))){
                                        this.totalNotReputableResources++;
                                        isProblematicQuad = true;
                                } 
                            }
                        
                        if (quad.getObject().isURI()){
                                this.totalResources++;    
                                if (!(quad.getObject().getURI().startsWith(OBO_Foundry_URI))){
                                        this.totalNotReputableResources++;
                                        isProblematicQuad = true;
                                }
                            }
                        
                        if (isProblematicQuad){
                                this.problemList.add(quad);
                        }
                        
                    } catch (Exception e){
                            logger.debug(e.getStackTrace());
                            logger.error(e.getMessage());
                    }
        }
        
        /**
         * metric value = total number of NOT reputable resources divided by total number of resources
         * 
         * @return  (total number of NOT reputable resources / total number of resources)
         */
        
        public double metricValue() {
                logger.debug("Total not Reputable Resources : " + this.totalNotReputableResources);
                logger.debug("Total Resources : " + this.totalResources);
                if (this.totalResources <= 0) {
                        logger.warn("Number of total resource are ZERO");
                        return 0;
                }
                return 1.0 - ((double) this.totalNotReputableResources / (double) this.totalResources);
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

    	@Override
    	public boolean isEstimate() {
    		return false;
    	}

    	@Override
    	public Resource getAgentURI() {
    		return 	DQM.LuzzuProvenanceAgent;
    	}
}
