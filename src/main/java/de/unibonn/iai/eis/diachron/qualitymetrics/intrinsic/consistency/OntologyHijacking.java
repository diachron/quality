package de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.consistency;

import org.apache.log4j.Logger;
import org.apache.xerces.util.URI;
import org.apache.xerces.util.URI.MalformedURIException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;

/**
 * Detects the redefinition by third parties of external classes/properties such that 
 * reasoning over data using those external terms is affected.
 * 
 * @author Muhammad Ali Qasmi
 * @date 10th June 2014
 */
public class OntologyHijacking extends AbstractQualityMetric{
        
        /**
         * Metic URI
         */
        private final Resource METRIC_URI = DQM.OntologyHijackingMetric;
        
        /**
         * logger static object
         */
        static Logger logger = Logger.getLogger(OntologyHijacking.class);

        protected long totalClassesCount = 0;
        protected long totalPropertiesCount = 0;
        
        protected long hijackedClassesCount = 0;
        protected long hijacekdPropertiesCount = 0;
        
        
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
        
        @Override
        public void compute(Quad quad) {
                
                if (isDefinedClassOrProperty(quad, "type")){ // quad represent a locally defined statement
                        
                        Node subject = quad.getSubject(); // retrieve subject
                        Node predicate = quad.getPredicate(); // retrieve predicate
                        Node object = quad.getObject(); // retrieve object
                        
                        System.out.print("Subject : " + subject);
                        System.out.print(" -- Predicate " + predicate );
                        System.out.println(" --> Object : " + object);
                        
                }
                else if (isDefinedClassOrProperty(quad, "domain")){
                        
                        Node subject = quad.getSubject(); // retrieve subject
                        Node predicate = quad.getPredicate(); // retrieve predicate
                        Node object = quad.getObject(); // retrieve object
                        
                        System.out.print("Subject : " + subject);
                        System.out.print(" -- Predicate " + predicate );
                        System.out.println(" --> Object : " + object);
                }
                
                /*
                if (subject.isURI()){ // for subject only
                        
                        this.totalClassesCount++;
                        if (true){
                                this.hijackedClassesCount++;
                        }
                }
                
                
                if (predicate.isURI()){ // for predicate only
                
                        this.totalPropertiesCount++;
                        if (true){
                                this.hijacekdPropertiesCount++;
                        }
                }
                
                if (object.isURI()){ // for object only
                        
                        this.totalClassesCount++;
                        if (true){
                                this.hijackedClassesCount++;
                        }
                }
                */
        }

        @Override
        public double metricValue() {
                // TODO Auto-generated method stub
                return 0;
        }

        @Override
        public Resource getMetricURI() {
                return this.METRIC_URI;
        }

        @Override
        public ProblemList<?> getQualityProblems() {
                // TODO Auto-generated method stub
                return null;
        }

}
