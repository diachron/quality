package de.unibonn.iai.eis.diachron.qualitymetrics.reputation;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;

/**
 * "By assigning explicit ratings to the dataset (manual) and analyzing
 * external links or page rank (semi-automated)" - Quality Assessment for Linked Open Data: A
 * Survey, Amrapali Zaveri, et al.
 * 
 * @author Muhammad Ali Qasmi
 * @date 12th June 2014
 */
public class OBOFoundry extends AbstractQualityMetric{

        /**
         * Metic URI
         */
        private final Resource METRIC_URI = null;
        /**
         * logger static object
         */
        static Logger logger = Logger.getLogger(OBOFoundry.class);
        /**
         * list of problematic quads
         */
        protected List<Quad> problemList = new ArrayList<Quad>();
        
        @Override
        public void compute(Quad quad) {
                // TODO Auto-generated method stub
        }

        @Override
        public double metricValue() {
                // TODO Auto-generated method stub
                return 0;
        }

        /*
         * (non-Javadoc)
         * @see de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric#getMetricURI()
         */
        @Override
        public Resource getMetricURI() {
                return this.METRIC_URI;
        }

        /*
         * (non-Javadoc)
         * @see de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric#getQualityProblems()
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

}
