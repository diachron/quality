package eu.diachron.qualitymetrics.intrinsic.consistency;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import de.unibonn.iai.eis.luzzu.semantics.utilities.Commons;
import eu.diachron.qualitymetrics.utilities.VocabularyLoader;

/**
 * The Ontology Hijacking detects the redefinition by analyzing defined classes or 
 * properties in data set and looks of same definition in its respective vocabulary. 
 * 
 * This metric uses table 1 from http://www.aidanhogan.com/docs/saor_aswc08.pdf
 * in order to identify the rules for Ontology Hijacking.
 * 
 * @author Jeremy Debattista
 * 
 */
public class OntologyHijacking implements QualityMetric{
        
        private final Resource METRIC_URI = DQM.OntologyHijackingMetric;
        static Logger logger = LoggerFactory.getLogger(OntologyHijacking.class);

        private double totalPossibleHijacks = 0; // total number of redefined classes or properties
        private double totalHijacks = 0;

        private List<Model> problemList = new ArrayList<Model>();
        
        private List<HijackingRule> hijackingRules = new CustomList<HijackingRule>();
        {
        	hijackingRules.add(new HijackingRule(RDFS.subClassOf, TriplePosition.SUBJECT));
        	hijackingRules.add(new HijackingRule(OWL.equivalentClass, TriplePosition.SUBJECT));
        	hijackingRules.add(new HijackingRule(OWL.equivalentClass, TriplePosition.OBJECT));
        	hijackingRules.add(new HijackingRule(RDFS.subPropertyOf, TriplePosition.SUBJECT));
        	hijackingRules.add(new HijackingRule(OWL.equivalentProperty, TriplePosition.SUBJECT));
        	hijackingRules.add(new HijackingRule(OWL.equivalentProperty, TriplePosition.OBJECT));
        	hijackingRules.add(new HijackingRule(OWL.inverseOf, TriplePosition.SUBJECT));
        	hijackingRules.add(new HijackingRule(OWL.inverseOf, TriplePosition.OBJECT));
        	hijackingRules.add(new HijackingRule(RDFS.domain, TriplePosition.SUBJECT));
        	hijackingRules.add(new HijackingRule(RDFS.range, TriplePosition.SUBJECT));
        	hijackingRules.add(new HijackingRule(OWL.SymmetricProperty, TriplePosition.SUBJECT));
        	hijackingRules.add(new HijackingRule(OWL.SymmetricProperty, TriplePosition.SUBJECT));
        	hijackingRules.add(new HijackingRule(OWL.onProperty, TriplePosition.OBJECT));
        	hijackingRules.add(new HijackingRule(OWL.hasValue, TriplePosition.SUBJECT));
        	hijackingRules.add(new HijackingRule(OWL.unionOf, TriplePosition.OBJECT));
        	hijackingRules.add(new HijackingRule(OWL.intersectionOf, TriplePosition.SUBJECT));
        	hijackingRules.add(new HijackingRule(OWL.intersectionOf, TriplePosition.OBJECT));
        	hijackingRules.add(new HijackingRule(OWL.FunctionalProperty, TriplePosition.SUBJECT));
        	hijackingRules.add(new HijackingRule(OWL.InverseFunctionalProperty, TriplePosition.SUBJECT));
        	hijackingRules.add(new HijackingRule(OWL.TransitiveProperty, TriplePosition.SUBJECT));
        }
        
        
        public void compute(Quad quad) {
        	if (!quad.getObject().isURI()) return; //we do not need to test this
        	
        	Resource subject = Commons.asRDFNode(quad.getSubject()).asResource();
        	Resource predicate = Commons.asRDFNode(quad.getPredicate()).asResource();
        	Resource object = Commons.asRDFNode(quad.getObject()).asResource();
        	
        	// class type hijacking
        	if (predicate.equals(RDF.type)){
        		if (hijackingRules.contains(object)){
        			// we triggered a hijacking rule

        			if (!isAuthorative(subject)) {
        				totalHijacks++;
        				this.addToProblem(quad);
        			}
        			totalPossibleHijacks++;
        		}
        	} else if (hijackingRules.contains(predicate)){
        		// property type hijacking - we might have multiple rules here
        		List<HijackingRule> rules = new ArrayList<HijackingRule>();
        		for (HijackingRule rule : hijackingRules){
        			if (rule.equals(predicate)) rules.add(rule);
        		}
        		
        		for (HijackingRule r : rules){
        			Resource authoritativeCheck;
            		if (r.authorativeSource == TriplePosition.SUBJECT) authoritativeCheck = subject;
            		else authoritativeCheck = object;
            		
            		if (!isAuthorative(authoritativeCheck)) {
        				totalHijacks++;
        				this.addToProblem(quad);
        			}
        		}
        		totalPossibleHijacks++;

        	}
        }
        
        private void addToProblem(Quad q){
        	Model m = ModelFactory.createDefaultModel();
        	
        	Resource gen = Commons.generateURI();
        	m.createStatement(gen, RDF.type, DQM.OntologyHijackingException);
        	
        	Resource anon = m.createResource(AnonId.create());
        	m.createStatement(gen, DQM.hijackedTripleStatement, anon);
        	m.createStatement(anon, RDF.subject, Commons.asRDFNode(q.getSubject()));
        	m.createStatement(anon, RDF.predicate, Commons.asRDFNode(q.getPredicate()));
        	m.createStatement(anon, RDF.object, Commons.asRDFNode(q.getObject()));
        	
        	this.problemList.add(m);
        }
        
        
		/**
		 * @param Concept being check for authority
		 * @return true if the assessed dataset is authorative to the concept
		 */
		private boolean isAuthorative(Resource node){
			/* A source s is authorative of concept c if:
			 *   1) c is a blank node OR
			 *   2) s is retrievable and is part of the namespace identifying c - given that c exists.
			 */
			
			if (node.isAnon()) return true;
			
			if (node.getNameSpace().equals(EnvironmentProperties.getInstance().getBaseURI())) 
				return true;
			else 
				return !(VocabularyLoader.checkTerm(node.asNode()));
		}
		
        
        /**
         * Returns metric value for between 0 to 1. Where 1 as the best case and 0 as worst case 
         * @return double - range [0 - 1] 
         */
        
        public double metricValue() {
        	logger.debug("Total Hijacks: {}; Total Possible: {}"); 
        		double value = 1.0 - (this.totalHijacks / this.totalPossibleHijacks);
                return value;
        }

        
        public Resource getMetricURI() {
                return this.METRIC_URI;
        }

        
    	public ProblemList<?> getQualityProblems() {
    		ProblemList<Model> tmpProblemList = null;
    		try {
    			if(this.problemList != null && this.problemList.size() > 0) {
    				tmpProblemList = new ProblemList<Model>(this.problemList);
    			} else {
    				tmpProblemList = new ProblemList<Model>();
    			}		} catch (ProblemListInitialisationException problemListInitialisationException) {
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
			return DQM.LuzzuProvenanceAgent;
		}
		
		private class HijackingRule{
			Resource hijackProperty; // could be a property or resource
			TriplePosition authorativeSource;
			
			HijackingRule(Resource resource, TriplePosition pos){
				hijackProperty = resource;
				authorativeSource = pos;
			}
			
			@Override
			public boolean equals(Object object){
				boolean sameSame = false;
				
				if (object != null && object instanceof Resource){
					sameSame = this.hijackProperty.equals(((Resource) object));
				}
				
				return sameSame;
			}
		}
		
		private enum TriplePosition{
			SUBJECT, PREDICATE, OBJECT;
		}
		
		private class CustomList<T> extends ArrayList<T>{
			private static final long serialVersionUID = 1L;

			@Override
		    public int indexOf(Object o) {
		        if (o == null) {
		            for (int i = 0; i < this.size(); i++)
		                if (this.get(i)==null)return i;
		        } else {
		            for (int i = 0; i < this.size(); i++)
		                if (this.get(i).equals(o))
		                    return i;
		        }
		        return -1;
		    }
		}
}
