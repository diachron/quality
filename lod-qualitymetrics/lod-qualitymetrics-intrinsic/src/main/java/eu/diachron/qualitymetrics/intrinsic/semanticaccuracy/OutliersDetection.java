/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.semanticaccuracy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.luzzu.assessment.ComplexQualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.AfterException;
import de.unibonn.iai.eis.luzzu.exceptions.BeforeException;
import eu.diachron.qualitymetrics.intrinsic.semanticaccuracy.helper.PredicateProbability;
import eu.diachron.qualitymetrics.utilities.AbstractComplexQualityMetric;

/**
 * @author Jeremy Debattista
 * 
 * Outliers Detection / Semantic Accuracy
 * http://www.heikopaulheim.com/docs/ijswis_2014.pdf
 * 
 * https://d.docs.live.net/18e42970155ff7b6/Documents/Bits'n'Pieces/LODCloud%20Survey.one#Detecting%20Outliers&section-id={B9375B85-CE1C-4A4D-93DB-2F409825DA95}&page-id={C4C5335D-3CC2-0D41-A5FE-C608BE7CF2B2}&object-id={6792311B-A19A-EB48-921F-52738EC2EF1A}&8B
 */
public class OutliersDetection extends AbstractComplexQualityMetric{
	
	private Map<String, Set<String>> resourceTypes = new HashMap<String, Set<String>>(); // ResourceURI, Resource Type
	private Map<String, Set<PredicateProbability>> predicates = new HashMap<String, Set<PredicateProbability>>() ; //ResourceURI, Predicates attached to it
	
	private Map<String, Set<PredicateProbability>> unknownSubjects = new HashMap<String, Set<PredicateProbability>>() ; // ResourceURI at Subject Position, Predicates attached to it
	private Map<String, Set<PredicateProbability>> unknownObjects = new HashMap<String, Set<PredicateProbability>>() ;  // ResourceURI at Object Position, Predicates attached to it

	private Map<String, PredicateProbability> uniquePredicates  = new HashMap<String, PredicateProbability>() ;
	

	@Override
	public void compute(Quad quad) {
		Node subject = quad.getSubject();
		String subjectURI = subject.getURI();
		
		Node predicate = quad.getPredicate();
		
		Node object = quad.getObject();
		String objectURI = object.getURI();
		
		if (predicate.getURI().equals(RDF.type.getURI())){
			Set<String> types = new HashSet<String>();
			if (this.resourceTypes.containsKey(subjectURI)) types = this.resourceTypes.get(subjectURI);
			types.add(objectURI);
			
			this.resourceTypes.put(subjectURI,types);
			this.propogateProbabilities(subjectURI, objectURI, null); //add this type to all resources being in a subject or an object of a statement
		} else {
			if (resourceTypes.containsKey(subjectURI)) 
				this.addAndPropogate(resourceTypes.get(subjectURI), quad, TriplePosition.SUBJECT);
			else {
				Set<PredicateProbability> set = new HashSet<PredicateProbability>();
				PredicateProbability pp = new PredicateProbability(quad);
				if (this.uniquePredicates.containsKey(quad.getPredicate().getURI())) pp = this.uniquePredicates.get(quad.getPredicate().getURI());
				else this.uniquePredicates.put(quad.getPredicate().getURI(), pp);
				pp.addQuad(quad);
				set.add(pp);
				unknownSubjects.put(subjectURI, set);
			}
			
			if (resourceTypes.containsKey(objectURI)) 
				this.addAndPropogate(resourceTypes.get(objectURI), quad, TriplePosition.OBJECT);
			else {
				Set<PredicateProbability> set = new HashSet<PredicateProbability>();
				PredicateProbability pp = new PredicateProbability(quad);
				if (this.uniquePredicates.containsKey(quad.getPredicate().getURI())) pp = this.uniquePredicates.get(quad.getPredicate().getURI());
				else this.uniquePredicates.put(quad.getPredicate().getURI(), pp);
				pp.addQuad(quad);
				
				set.add(pp);	
				unknownObjects.put(objectURI, set);
			}
		}
		
	}
	
	private void addAndPropogate(Set<String> typeURI, Quad quad, TriplePosition position){
		String resourceURI = "";
		String predicateURI = quad.getPredicate().getURI();
		
		if (position == TriplePosition.SUBJECT) resourceURI = quad.getSubject().getURI();
		else resourceURI = quad.getObject().getURI();
		
		
		if (predicates.containsKey(resourceURI)) {
			Set<PredicateProbability> set = predicates.get(resourceURI);
			if (!(set.contains(predicateURI))) {
				PredicateProbability pp = new PredicateProbability(quad);
				if (this.uniquePredicates.containsKey(quad.getPredicate().getURI())) pp = this.uniquePredicates.get(quad.getPredicate().getURI());
				else this.uniquePredicates.put(quad.getPredicate().getURI(), pp);
				
				pp.addQuad(quad);
				set.add(pp);	
			}
		} else {
			Set<PredicateProbability> set = new HashSet<PredicateProbability>();
			PredicateProbability pp = new PredicateProbability(quad);
			if (this.uniquePredicates.containsKey(quad.getPredicate().getURI())) pp = this.uniquePredicates.get(quad.getPredicate().getURI());
			else this.uniquePredicates.put(quad.getPredicate().getURI(), pp);
			
			pp.addQuad(quad);
			set.add(pp);	
			predicates.put(resourceURI, set);
		}
		
		for(String s : typeURI) this.propogateProbabilities(resourceURI, s, position);
	}
	
	private void propogateProbabilities(String resourceURI, String typeURI, TriplePosition position){
		if (position == null){
			if (unknownSubjects.containsKey(resourceURI)){
				Set<PredicateProbability> sets = this.unknownSubjects.get(resourceURI);
				for(PredicateProbability p : sets) p.addToSubjectColumn(typeURI, resourceURI);
				unknownSubjects.remove(resourceURI);
			} else {
				this.propogateProbabilities(resourceURI, typeURI, TriplePosition.SUBJECT);
			}
			
			if (unknownObjects.containsKey(resourceURI)){
				Set<PredicateProbability> sets = this.unknownObjects.get(resourceURI);
				for(PredicateProbability p : sets) p.addToObjectColumn(typeURI, resourceURI);
				unknownObjects.remove(resourceURI);
			} else {
				this.propogateProbabilities(resourceURI, typeURI, TriplePosition.OBJECT);
			}
		}
		
		if (this.predicates.containsKey(resourceURI)){
			Set<PredicateProbability> sets = this.predicates.get(resourceURI);
			for(PredicateProbability p : sets){
				if (position == TriplePosition.SUBJECT) 
					p.addToSubjectColumn(typeURI, resourceURI);
				if (position == TriplePosition.OBJECT) 
					p.addToObjectColumn(typeURI, resourceURI);
			}
		}
	}

	@Override
	public double metricValue() {
		for(PredicateProbability pp: this.uniquePredicates.values()){
			pp.deriveStatisticalDistribution();
		}
		
		for(PredicateProbability pp: this.uniquePredicates.values()){
			System.out.println(pp.getString());
		}
		
		return 0;
	}

	@Override
	public Resource getMetricURI() {
		return null;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		return null;
	}

	@Override
	public boolean isEstimate() {
		return false;
	}

	@Override
	public Resource getAgentURI() {
		return null;
	}

	@Override
	public void before(Object[] args) throws BeforeException {
		// TODO Auto-generated method stub
	}

	@Override
	public void after(Object[] args) throws AfterException {
		// TODO Auto-generated method stub
	}
	
	private enum TriplePosition{
		SUBJECT, PREDICATE, OBJECT;
	}
	
	
//	:a (type1) 	dbp:author :b  (type2) 
//	:c (type1)  dbp:author :d (type1) 
//	:e (type1)  dbp:author :f (type3) 
//	:g (type2)  dbp:author :h (type1) 
	
	public static void main(String [] args){
		OutliersDetection od = new OutliersDetection();
		
		Quad q = new Quad(null, NodeFactory.createURI("<http://example.org/a>"),  RDF.type.asNode(),  NodeFactory.createURI("<http://example.org/type1>"));
		od.compute(q);
		q = new Quad(null, NodeFactory.createURI("<http://example.org/a>"),  NodeFactory.createURI("<http://example.org/property>"),  NodeFactory.createURI("<http://example.org/b>"));
		od.compute(q);
		q = new Quad(null, NodeFactory.createURI("<http://example.org/c>"),  NodeFactory.createURI("<http://example.org/property>"),  NodeFactory.createURI("<http://example.org/d>"));
		od.compute(q);
		q = new Quad(null, NodeFactory.createURI("<http://example.org/b>"), RDF.type.asNode(),  NodeFactory.createURI("<http://example.org/type2>"));
		od.compute(q);
		q = new Quad(null, NodeFactory.createURI("<http://example.org/c>"), RDF.type.asNode(),  NodeFactory.createURI("<http://example.org/type1>"));
		od.compute(q);
		q = new Quad(null, NodeFactory.createURI("<http://example.org/g>"),  NodeFactory.createURI("<http://example.org/property>"),  NodeFactory.createURI("<http://example.org/h>"));
		od.compute(q);
		q = new Quad(null, NodeFactory.createURI("<http://example.org/d>"), RDF.type.asNode(),  NodeFactory.createURI("<http://example.org/type1>"));
		od.compute(q);
		q = new Quad(null, NodeFactory.createURI("<http://example.org/e>"), RDF.type.asNode(),  NodeFactory.createURI("<http://example.org/type1>"));
		od.compute(q);
		q = new Quad(null, NodeFactory.createURI("<http://example.org/f>"), RDF.type.asNode(),  NodeFactory.createURI("<http://example.org/type3>"));
		od.compute(q);
		q = new Quad(null, NodeFactory.createURI("<http://example.org/e>"),  NodeFactory.createURI("<http://example.org/property>"),  NodeFactory.createURI("<http://example.org/f>"));
		od.compute(q);
		q = new Quad(null, NodeFactory.createURI("<http://example.org/g>"), RDF.type.asNode(),  NodeFactory.createURI("<http://example.org/type2>"));
		od.compute(q);
		q = new Quad(null, NodeFactory.createURI("<http://example.org/h>"), RDF.type.asNode(),  NodeFactory.createURI("<http://example.org/type1>"));
		od.compute(q);
		
		od.metricValue();
	}
}
