package eu.diachron.qualitymetrics.representational.understandability;

import org.apache.log4j.Logger;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.semantics.vocabulary.DQM;
/**
 * @author jeremy
 *
 * This measures the percentage of entities having an rdfs:label or rdfs:comment
 */
public class HumanReadableLabelling implements QualityMetric {
	
	private static Logger logger = Logger.getLogger(HumanReadableLabelling.class);

	private final Resource METRIC_URI = DQM.HumanReadableLabellingMetric;
	
	/**
	 * MapDB database, used to persist the Map containing the instances found to be declared in the dataset
	 */
	private DB mapDB = DBMaker.newTempFileDB().closeOnJvmShutdown().deleteFilesAfterClose().make();
			
	private HTreeMap<String, Integer> pEntities = this.mapDB.createHashMap("human-readable-labelling-map").make();
	
	/**
	 * Each entity is checked for a Human Readable label <rdfs:comment> or <rdfs:label>.
	 * In this metric we are assuming that each entity has exactly 1 comment and/or label,
	 * thus we are not checking for contradicting labeling or commenting of entities.
	 */
	
	public void compute(Quad quad) {
		if (quad.getSubject().isURI() && quad.getPredicate().getURI().equals(RDF.type.getURI())){
			// we've got an instance!
			if (!(pEntities.containsKey(quad.getSubject().getURI()))) { // an instance might have more than 1 type defined
				pEntities.put(quad.getSubject().getURI(), 0);
			}
		}
	
		if (quad.getSubject().isURI() && (quad.getPredicate().getURI().equals(RDFS.label.getURI())) || (quad.getPredicate().getURI().equals(RDFS.comment.getURI()))){
			// we'll check if the provider is cheating and is publishing empty string labels and comments
			if (!(quad.getObject().getLiteralValue().equals(""))) pEntities.put(quad.getSubject().getURI(), 1);
		}
	}

	
	public double metricValue() {
		double entities = 0.0;
		double humanLabels = 0.0;
		
		for (String n : this.pEntities.keySet()){
			entities+=1;
			humanLabels += this.pEntities.get(n);
		}

		return humanLabels/entities; // at most we should have 1 label/comment for each entity
	}

	
	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	
	public ProblemList<?> getQualityProblems() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected void finalize() throws Throwable {
		
		// Destroy persistent HashMap and the corresponding database
		try {
			if(this.pEntities != null) {
				this.pEntities.close();
			}
			if(this.mapDB != null && !this.mapDB.isClosed()) {
				this.mapDB.close();
			}
		} catch(Throwable ex) {
			logger.warn("Persistent HashMap or backing database could not be closed", ex);
		} finally {
			super.finalize();
		}
	}

}
