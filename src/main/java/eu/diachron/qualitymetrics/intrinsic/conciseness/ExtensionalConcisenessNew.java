package eu.diachron.qualitymetrics.intrinsic.conciseness;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.assessment.ComplexQualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.semantics.vocabulary.DQM;

/**
 * @author Santiago Londono
 * Provides a measure of the redundancy of the dataset at the data level, by calculating the 
 * Extensional Conciseness metric, which is part of the Conciseness dimension.
 */
public class ExtensionalConcisenessNew implements ComplexQualityMetric {
	
	transient private static Logger logger = Logger.getLogger(ExtensionalConciseness.class);
	
	transient private final Resource METRIC_URI = DQM.ExtensionalConcisenessMetric;
	
	/**
	 * MapDB database, used to persist the Map containing the instances found to be declared in the dataset
	 */
	transient private static DB mapDB = DBMaker.newTempFileDB()
			.closeOnJvmShutdown()
			.deleteFilesAfterClose()
        	.make();
	
	/**
	 * Map indexing the subjects detected during the computation of the metric. Every subject is identified 
	 * by a different id (URI), which serves as key of the map. The value of each subject consists of a 
	 * resource. A ConcurrentHashMap is used since it is synchronized and metric instances ought to be thread safe.
	 */
	 private HTreeMap<String, String> pMapSubjects = mapDB.createHashMap("extensional-conciseness-map").make(); //subject , "triples string"
	 
	 private int totalInstances = 0;

	 private boolean afterInvoked = false;
	
	
	/**
	 * Re-computes the value of the Extensional Conciseness Metric, by considering a new quad provided.
	 * @param quad The new quad to be considered in the computation of the metric. Must be not null.
	 */
	public void compute(Quad quad) {
		
		String triples = pMapSubjects.get(quad.getSubject().getURI());
		
		
		String value = quad.getPredicate().getURI().toString() + " " + quad.getObject().toString() + " ";
		if (triples == null) { 
			pMapSubjects.put(quad.getSubject().getURI(), value);
		} 
		else {
			String concat = triples + value;
			pMapSubjects.put(quad.getSubject().getURI(), concat);
		}
		
	}


	public void before(Object... args) {
		// Do Nothing
	}
	
	
	int nonUniqueInstances = 0;
	public void after(Object... args) {
		totalInstances = pMapSubjects.size();
		afterInvoked = true;
		
		
		Iterator<Map.Entry<String,String>> iter = pMapSubjects.entrySet().iterator();
		Set<String> duplicates = new HashSet<String>(); 
		while (iter.hasNext()) {
			Map.Entry<String, String> nxt = iter.next();
			String key = nxt.getKey();
			String value = nxt.getValue();
			if (duplicates.contains(key)) continue; //we do not check those metrics we already checked
			for(String otherKey : pMapSubjects.keySet()){
				if (key.equals(otherKey)) continue;
				if (value.equals(pMapSubjects.get(otherKey))) {
					nonUniqueInstances++;
					duplicates.add(otherKey);
					pMapSubjects.remove(otherKey);
				} 
			}
		}
	}
	
	
	/**
	 * Returns the current value of the Extensional Conciseness Metric, computed as the ratio of the 
	 * Number of Unique Subjects to the Total Number of Subjects. 
	 * Subjects are the objects being described by the quads provided on invocations to the compute 
	 * method, each subject is identified by its URI (the value of the subject attribute of the quad). 
	 * Uniqueness of subjects is determined from its properties: one subject is said to be unique 
	 * if and only if there is no other subject equivalent to it.
	 * - Note that two equivalent subjects may have different ids (URIs).
	 * @return Current value of the Extensional Conciseness Metric: (No. of Unique Subjects / Total No. of Subjects)
	 */
	
	public double metricValue() {
		if (!this.afterInvoked) this.after();
		
		double metricValue = ((double)totalInstances - nonUniqueInstances) / ((double)totalInstances); // number of unique instances / tot number of instance representation
				
		return metricValue;
	}
	
	
	public Resource getMetricURI() {
		return METRIC_URI;
	}

	public ProblemList<?> getQualityProblems() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected void finalize() throws Throwable {
		
		// Destroy persistent HashMap and the corresponding database
		try {
			if(pMapSubjects != null) {
				pMapSubjects.close();
			}
			if(mapDB != null && !mapDB.isClosed()) {
				mapDB.close();
			}
		} catch(Throwable ex) {
			logger.warn("Persistent HashMap or backing database could not be closed", ex);
		} finally {
			super.finalize();
		}
	}
}
