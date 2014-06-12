package de.unibonn.iai.eis.diachron.qualitymetrics.reputation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
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
public class ReputationOfDataset extends AbstractQualityMetric{

        /**
         * Metic URI
         */
        private final Resource METRIC_URI = null;
        /**
         * logger static object
         */
        static Logger logger = Logger.getLogger(ReputationOfDataset.class);
        /**
         * list of problematic quads
         */
        protected List<Quad> problemList = new ArrayList<Quad>();
        /**
         * Directory containing reputable sources
         */
        protected String reputableSourceDirectory = "src/main/resources/reputable";         
        /**
         * Models of reputable sources
         */
        protected List<Model> reputableSourcesList = new ArrayList<Model>();
        /**
         * loads reputable sources models
         */
        protected void loadReputableSources() {
                try {
                File[] files = new File(reputableSourceDirectory).listFiles();
                for (File file : files) {
                        if (file.isFile()) {
                                System.out.println("loading : " + file.getName());
                                Model model = ModelFactory.createDefaultModel();
                                FileInputStream fileInputStream = new FileInputStream(file);
                                model.read(fileInputStream, null);
                                fileInputStream.close();
                                reputableSourcesList.add(model);
                        }
                    }
                System.out.println(reputableSourcesList.size());
                } catch (FileNotFoundException e) {
                        logger.debug(e.getStackTrace());
                        logger.error(e.getMessage());
                } catch (IOException e) {
                        logger.debug(e.getStackTrace());
                        logger.error(e.getMessage());
                }
        }
        
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
