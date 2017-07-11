/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.semanticaccuracy.helper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;

import eu.diachron.qualitymetrics.utilities.SerialisableTriple;


/**
 * @author Jeremy Debattista
 * 
 */
public class PredicateProbability implements Serializable {

	private static final long serialVersionUID = -220634839564654372L;

	private Set<SerialisableTriple> triples = new HashSet<SerialisableTriple>();
	
	private Map<String, Double> subjectColumn = new HashMap<String,Double>(); // type, #instances (which will change to probability counter)
	private Map<String, Double> objectColumn = new HashMap<String,Double>();  // type, #instances (which will change to probability counter)
	
	private Set<String> subjects = new HashSet<String>();
	private Set<String> objects = new HashSet<String>();
	
	private String predicate = "";
	
	public PredicateProbability(Quad q){
		this.addQuad(q);
		this.predicate = q.getPredicate().getURI();
	}
	
	public void addQuad(Quad q){
		SerialisableTriple t = new SerialisableTriple(q.asTriple());
		if (!triples.contains(t)) this.triples.add(t);
		subjects.add(q.getSubject().getURI());
		objects.add(q.getObject().getURI());
	}
	
	public void addTriple(Triple t){
		this.addQuad(new Quad(null,t));
	}
	
	public void addToSubjectColumn(String uri, String resourceURI){
		if (subjects.contains(resourceURI)){
			Double counter = 0.0d;
			if (this.subjectColumn.containsKey(uri)) counter = this.subjectColumn.get(uri);
			counter++;
			this.subjectColumn.put(uri, counter);
		}
	}
	
	public void addToObjectColumn(String uri, String resourceURI){
		if (objects.contains(resourceURI)){
			Double counter = 0.0d;
			if (this.objectColumn.containsKey(uri)) counter = this.objectColumn.get(uri);
			counter++;
			this.objectColumn.put(uri, counter);
		}
	}
	
	public void deriveStatisticalDistribution(){
		for(String s : subjectColumn.keySet()){
			Double cnt = subjectColumn.get(s);
			subjectColumn.put(s, (cnt/(double)triples.size()));
		}
		
		for(String s : objectColumn.keySet()){
			Double cnt = objectColumn.get(s);
			objectColumn.put(s, (cnt/(double)triples.size()));
		}
	}
	
	public String getString(){
		StringBuilder sb = new StringBuilder();
		
		sb.append(this.predicate);
		sb.append(System.getProperty("line.separator"));
		sb.append("\t| Subjects\t| Objects");
		sb.append(System.getProperty("line.separator"));
		
		for (String s : subjectColumn.keySet()){
			sb.append(s+"|\t"+subjectColumn.get(s)+"\t|");
			if (objectColumn.containsKey(s))
				sb.append(objectColumn.get(s));
			else 
				sb.append("0.0");
			sb.append(System.getProperty("line.separator"));
		}
		
		for (String s : objectColumn.keySet()){
			if (subjectColumn.containsKey(s)) continue;
			else {
				sb.append(s+"|\t0.0\t|"+objectColumn.get(s));
			}
			sb.append(System.getProperty("line.separator"));
		}
		
		return sb.toString();
		
	}
	
	
	 @Override
	 public int hashCode() {
		 return predicate.hashCode();
	 }

	 @Override
	 public boolean equals(Object obj) {
	    	
		 if (obj instanceof PredicateProbability){
			 PredicateProbability other = (PredicateProbability)obj;
			 return this.predicate.equals(other.predicate);
		 }
		 
		 return false;
	 }

}
