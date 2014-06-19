package de.unibonn.iai.eis.diachron.qualitymetrics.reputation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.xerces.util.URI;
import org.apache.xerces.util.URI.MalformedURIException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;

/**
 * Detects non reputable resources by retrieving URI of resources from
 * data sets and comparing them with URI found in reputable resources.
 * 
 * Note: Reputable resources are stored in ../src/main/resources/reputable
 * as file. Sub-directories of reputable directories are not evaluated.
 * 
 * Metric Value Range : [0 - 1]
 * Best Case : 0
 * Worst Case : 1
 * 
 * @author Muhammad Ali Qasmi
 * @date 12th June 2014
 */
public class ReputationOfDataset extends AbstractQualityMetric{

        /**
         * Metric URI
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
        protected Set<String> reputableSourcesURISet = new HashSet<String>();
        /**
         * total number of resources 
         */
        protected long totalResources = 0;
        /**
         * total number of not reputable resources
         */
        protected long totalNotReputableResources = 0;
        /**
         * loads reputable sources models. Stores URI and path of each 
         * subject, predicate, object in HashSet.
         */
        protected void loadReputableSources() {
                try {
                File[] files = new File(reputableSourceDirectory).listFiles();
                for (File file : files) {
                        if (file.isFile()) {
                                logger.debug("loading reputable resource : " + file.getName());
                                Model model = ModelFactory.createDefaultModel();
                                FileInputStream fileInputStream = new FileInputStream(file);
                                model.read(fileInputStream, null);
                                fileInputStream.close();
                                StmtIterator iter = model.listStatements();
                                while (iter.hasNext()){
                                        Statement statement = iter.nextStatement();
                                        RDFNode subject = statement.getSubject();
                                        RDFNode predicate = statement.getPredicate();
                                        RDFNode object = statement.getObject();
                                        if (subject != null){
                                            if (subject.isURIResource()){
                                                URI uri = new URI(subject.asNode().getURI());
                                                if (uri != null){
                                                        this.reputableSourcesURISet.add(uri.getHost() + "/" + uri.getPath(false,false));
                                                }
                                            }
                                        }
                                        
                                        if (predicate != null){
                                            if (predicate.isURIResource()){
                                                    URI uri = new URI(predicate.asNode().getURI());
                                                    if (uri != null){
                                                            this.reputableSourcesURISet.add(uri.getHost() + "/" + uri.getPath(false,false));
                                                    }
                                            }
                                        }
                                        
                                        if (object != null){
                                            if (object.isURIResource()){
                                                    URI uri = new URI(object.asNode().getURI());
                                                    if (uri != null){
                                                            this.reputableSourcesURISet.add(uri.getHost() + "/" + uri.getPath(false,false));
                                                    }
                                            }
                                        }
                                }
                        }
                    }
                } catch (FileNotFoundException e) {
                        logger.debug(e.getStackTrace());
                        logger.error(e.getMessage());
                } catch (IOException e) {
                        logger.debug(e.getStackTrace());
                        logger.error(e.getMessage());
                }
        }
        
        /**
         * Retrieves URI (with path) of each subject, predicate and object.
         * Checks is those URI (with path) exists in the HashSet of reputable resources
         */
        @Override
        public void compute(Quad quad) {
                try {
                    
                    boolean isProblematicQuad = false;
                    if (quad.getSubject().isURI()){
                            this.totalResources++;    
                            URI uri = new URI(quad.getSubject().getURI());
                            if (!this.reputableSourcesURISet.contains(uri.getHost() + "/" + uri.getPath(false,false))){
                                    this.totalNotReputableResources++;
                                    isProblematicQuad = true;
                            }
                        }
                    
                    if (quad.getPredicate().isURI()){
                        this.totalResources++;    
                        URI uri = new URI(quad.getPredicate().getURI());
                        if (!this.reputableSourcesURISet.contains(uri.getHost() + "/" + uri.getPath(false,false))){
                                this.totalNotReputableResources++;
                                isProblematicQuad = true;
                        }
                    }
                    
                    if (quad.getObject().isURI()){
                            this.totalResources++;    
                            URI uri = new URI(quad.getObject().getURI());
                            if (!this.reputableSourcesURISet.contains(uri.getHost() + "/" + uri.getPath(false,false))){
                                    this.totalNotReputableResources++;
                                    isProblematicQuad = true;
                            }
                        }
                    
                    if (isProblematicQuad){
                            this.problemList.add(quad);
                    }
                    
                } catch (MalformedURIException e) {
                        logger.debug(e.getStackTrace());
                        logger.error(e.getMessage());
                } catch (Exception e){
                        logger.debug(e.getStackTrace());
                        logger.error(e.getMessage());
                }
        }
        
        /**
         * metric value = total number of NOT reputable resources divided by total number of reputable resources
         * 
         * @return  (total number of NOT reputable resources / total number of reputable resources)
         */
        @Override
        public double metricValue() {
                logger.debug("Total not Reputable Resources : " + this.totalNotReputableResources);
                logger.debug("Total Resources : " + this.totalResources);
                if (this.totalResources <= 0) {
                        logger.warn("Number of total resource are ZERO");
                        return 0;
                }
                return ((double) this.totalNotReputableResources / (double) this.totalResources);
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
