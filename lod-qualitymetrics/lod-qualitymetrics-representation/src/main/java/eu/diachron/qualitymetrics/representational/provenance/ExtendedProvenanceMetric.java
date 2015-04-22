/**
 * 
 */
package eu.diachron.qualitymetrics.representational.provenance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.PROV;

/**
 * @author Jeremy Debattista
 * 
 * According the the Data on the Web BP W3C WG, "consumers
 * need to know the origin or history of the published data... 
 * data published should include or link to provenance information".
 * 
 * This metric checks if a dataset has the required provenance information
 * that would enable the consumer to know the origin. In this metric
 * we consider the following requirements:
 * 1) Identification of an Agent;
 * 2) Identification of Activities in an Entity;
 * 3) Identification of DataSource in an Activity;
 * 4) Identification of an Agent for an Activity .
 * 
 * The metric value is calculated as follows:
 * SUM(value of Entities) / number of entities.
 * 
 * The value of entities calculation is done as follows:
 * An agent and activities are both given a weighting of 0.5.
 * The 0.5 weighting for activities are split amongst all activities.
 * 
 * Agent and Datasource are equally given a weighting of 0.5 per activity.
 * 
 * In this metric, we measure how much datasets conform to the W3C standard
 * PROV-O ontology.
 */
public class ExtendedProvenanceMetric implements QualityMetric {
	
	private ConcurrentMap<String, Entity> entityDirectory = new ConcurrentHashMap<String, Entity>();
	private ConcurrentMap<String, Activity> activityDirectory =  new ConcurrentHashMap<String, Activity>();

	@Override
	public void compute(Quad quad) {
		Node predicate = quad.getPredicate();
		Node object = quad.getObject();
		
		//is it an identification of an Agent?
		if (predicate.hasURI(PROV.wasAttributedTo.getURI())){
			Entity e = this.getOrPutEntity(quad.getSubject().getURI());
			e.agent = quad.getObject().getURI();
		}
		
		//is it an activity in an entity?
		if (predicate.hasURI(PROV.wasGeneratedBy.getURI())){
			Activity a = this.getOrPutActivity(quad.getObject().getURI());
			activityDirectory.put(quad.getSubject().getURI(), a);
			Entity e = this.getOrPutEntity(quad.getSubject().getURI());
			e.activities.add(a);
		}
		
		//is it an identification of a datasource in an activity?
		if (predicate.hasURI(PROV.used.getURI())){
			Activity a = this.getOrPutActivity(quad.getSubject().getURI());
			a.datasource = quad.getObject().getURI();
		}
		
		//is it an identification of an agent in an activity?
		if (predicate.hasURI(PROV.wasAssociatedWith.getURI()) || predicate.hasURI(PROV.actedOnBehalfOf.getURI())){
			Activity a = this.getOrPutActivity(quad.getSubject().getURI());
			a.agent = quad.getObject().getURI();
		}
		
		//is it an entity?
		if (object.hasURI(PROV.Entity.getURI())){
			this.getOrPutEntity(quad.getSubject().getURI());
		}
		
		//is it an activity?
		if (object.hasURI(PROV.Activity.getURI())){
			this.getOrPutActivity(quad.getSubject().getURI());
		}
	}

	private Entity getOrPutEntity(String uri){
		Entity e = new Entity();
		if (entityDirectory.containsKey(uri))
			e = entityDirectory.get(uri);
		else entityDirectory.put(uri, e);
		e.uri = (e.uri == null) ? uri : e.uri;
		return e;
	}
	
	private Activity getOrPutActivity(String uri){
		Activity a = new Activity();
		if (activityDirectory.containsKey(uri))
			a = activityDirectory.get(uri);
		else activityDirectory.put(uri, a);
		a.uri = (a.uri == null) ? uri : a.uri;
		return a;
	}
	
	
	@Override
	public double metricValue() {
		// The metric value is calculated by taking the summation
		// of all entities and divide them by the number of entities
		// available in a dataset.
		double val = 0.0;
		for (Entity e : entityDirectory.values()) val += e.getBasicValue();
		
		return (val / (double)entityDirectory.size());
	}

	@Override
	public Resource getMetricURI() {
		return DQM.ExtendedProvenanceMetric;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		// TODO Auto-generated method stub
		return new ProblemList<Quad>();
	}

	@Override
	public boolean isEstimate() {
		return false;
	}

	@Override
	public Resource getAgentURI() {
		return DQM.LuzzuProvenanceAgent;
	}
	
	
	class Entity {
		protected String uri = null;
		protected String agent = null;
		protected boolean publisherOrCreator = false;
		protected List<Activity> activities = new ArrayList<Activity>();
		
		protected double getBasicValue(){
			// An agent and activities are both given a weighting of 0.5.
			// The 0.5 weighting for activities are split amongst all activities
			double val = 0.0;
			val = (agent == null) ? val + 0.0 : val + 0.5;
			
			double valAct = 0.0;
			for(Activity a : activities) valAct += a.getBasicValue();
			
			if (valAct > 0.0){
				valAct = 0.5 * Math.abs((valAct / activities.size())); //normalising value between 0 and 0.5 
				val += valAct;
			}	
			return val;
		}
	}
	
	
	class Activity {
		protected String uri = null;
		protected String agent = null;
		protected String datasource = null;
		
		protected double getBasicValue(){
			double val = 0.0;
			val = (datasource == null) ? val + 0.0 : val + 0.5;
			val = (agent == null) ? val + 0.0 : val + 0.5;
			return val;
		}
	}

}
