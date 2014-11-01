package eu.diachron.qualitymetrics.intrinsic.conciseness;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.assessment.ComplexQualityMetric;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.semantics.vocabulary.DQM;

/**
 * @author Santiago Londono
 * Provides a measure of the redundancy of the dataset at the data level, by calculating the 
 * Extensional Conciseness metric, which is part of the Conciseness dimension.
 */
public class ExtensionalConciseness implements QualityMetric {
	
	private static Logger logger = Logger.getLogger(ExtensionalConciseness.class);
	
	private final Resource METRIC_URI = DQM.ExtensionalConcisenessMetric;
	
	/**
	 * MapDB database, used to persist the Map containing the instances found to be declared in the dataset
	 */
	private DB mapDB = DBMaker.newTempFileDB()
			.closeOnJvmShutdown()
			.deleteFilesAfterClose()
        	.make();
	
	/**
	 * Map indexing the subjects detected during the computation of the metric. Every subject is identified 
	 * by a different id (URI), which serves as key of the map. The value of each subject consists of a 
	 * resource. A ConcurrentHashMap is used since it is synchronized and metric instances ought to be thread safe.
	 */
	private HTreeMap<String, ComparableSubject> pMapSubjects = this.mapDB.createHashMap("extensional-conciseness-map").make();
	
	/**
	 * Re-computes the value of the Extensional Conciseness Metric, by considering a new quad provided.
	 * @param quad The new quad to be considered in the computation of the metric. Must be not null.
	 */
	
	public void compute(Quad quad) {
		// Every time a new quad is considered, check whether the subject has already been identified
		ComparableSubject subject = pMapSubjects.get(quad.getSubject().getURI());

		if(subject == null) {
			// The subject does not exists in the map. Add it, indexed by its URI
			subject = new ComparableSubject(quad.getSubject().getURI());
			pMapSubjects.put(quad.getSubject().getURI(), subject);
//			logger.trace("Added new subject: " + quad.getSubject().getURI());
		}

		// Add or update the property stated by the current quad into the subject, as a predicate with a value.
		// The value of the property is extracted from the quad's object
		subject.addProperty(quad.getPredicate().getURI(), quad.getObject());
//		logger.trace(" - Added property to subject: " + subject.getUri() + " -> " + quad.getObject().toString());
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
		// Keep a list free from redundant subjects, that is with all its unique elements
		List<ComparableSubject> lstUniqueSubjects = new ArrayList<ComparableSubject>();
		boolean isCurSubjectUnique;
		
		// Compare each of the subjects with the ones already recognized as unique...
		for(ComparableSubject curSubject : pMapSubjects.values()) {
			isCurSubjectUnique = true;

			for(ComparableSubject curUniqueSubject : lstUniqueSubjects) {
				// and if the subject currently examined is equivalent to one of the existing 
				// unique subjects, do not regard it as unique, continue with the next one
				if(curSubject.isEquivalentTo(curUniqueSubject)) {
					isCurSubjectUnique = false;
					break;
				}
			}
			
			// Finally, if the current subject is not equivalent to any of 
			// the existing unique subjects, add it as unique
			if(isCurSubjectUnique) {
				lstUniqueSubjects.add(curSubject);
			}
		}
		
		// Compute metric value
		double metricValue = ((double)lstUniqueSubjects.size()) / ((double)pMapSubjects.size());
				
		// If any subject is equivalent to another, it will not be part of the list of unique subjects, then 
		// the size of this list is the "Count of Unique Subjects" required to calculate the metric
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
			if(this.pMapSubjects != null) {
				this.pMapSubjects.close();
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
	
	/**
	 * @author Santiago Londono
	 * Represents a subject (i.e. a resource), that is described by a set of properties. Furthermore, provides 
	 * the functionality necessary to determine the equivalence of subjects according to their properties. 
	 * - Notice: this class is not thread safe (since HashMaps are not synchronized).
	 */
	private static class ComparableSubject implements Serializable {
		private static final long serialVersionUID = 726524323796732234L;

		/**
		 * URI identifying the subject, serves as its id.
		 */
		private String uri;
		
		/**
		 * Map containing the properties that describe this subject. Maps allow properties to be 
		 * retrieved by URI efficiently, which is quite convenient when comparing the sets of 
		 * properties of subjects. HashMap is used, since instances of this class ought not to be thread safe.
		 */
		private HashMap<String, String> mapProperties;
		
		/**
		 * Creates a new instance of a subject, identified by the provided URI and with
		 * an empty set of properties.
		 * @param uri Id of the subject
		 */
		public ComparableSubject(String uri) {
			this.uri = uri;
			mapProperties = new HashMap<String, String>();
		}
		
		/**	
		 * Adds or updates a property describing this subject.
		 * @param predicateUri URI of the predicate corresponding to the property to be added
		 * @param objectValue Value of the property to be added
		 */
		public void addProperty(String predicateUri, Node objectValue) {
			// To keep the memory footprint low, store in the properties hash a string representation of the node value. 
			// Node.toString() is suitable, as it "Answers a human-readable representation of the Node" (see javadoc for Node class)
			String objectValString = "";
			if(objectValue != null) {
				objectValString = objectValue.toString();
			}
			
			mapProperties.put(predicateUri, objectValString);		
		}
		
		/**
		 * Determines if this subject is equivalent to the one provided as parameter. 
		 * Two subjects are equivalent if they have the same set of properties, 
		 * all with the same values (but not necessarily the same ids).
		 * @param subject Subject this instance will be compared with
		 * @return true if the parameter is not null and if both subjects are equivalent, false otherwise.
		 */
		public boolean isEquivalentTo(ComparableSubject subject) {
			boolean result = false;
			
			if(subject != null) {
				
				int countPropertiesS2inThis = 0;		// Counts how many of the properties in subject are in this and have the same value in both resources

				// For each property in subject, check if it is contained in this and if it has the same value here
				for(Entry<String, String> curProperty : subject.mapProperties.entrySet()) {
					String curEquivProperty = this.mapProperties.get(curProperty.getKey());
					
					// Compare the properties values using their string representation
					if(curEquivProperty != null && curEquivProperty.equals(curProperty.getValue())) {
						countPropertiesS2inThis++;
					}
				}
				
				// Consider both resources equivalent if they have the same number of properties and all properties in r2
				// exist in r1 and have the same value in both resources
				if((countPropertiesS2inThis == subject.mapProperties.size()) && (this.mapProperties.size() == countPropertiesS2inThis)) {
					result = true;
				}	
			}
			
			return result;
		}

		/**
		 * URI identifying the subject, serves as its id.
		 * @return
		 */
		public String getUri() {
			return uri;
		}

	}
}
