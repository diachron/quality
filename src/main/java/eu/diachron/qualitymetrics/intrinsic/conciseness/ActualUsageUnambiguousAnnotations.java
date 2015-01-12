package eu.diachron.qualitymetrics.intrinsic.conciseness;

import java.io.Serializable;
import java.util.HashMap;

import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.commons.bigdata.MapDbFactory;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import eu.diachron.semantics.vocabulary.DQM;

/**
 * Provides a measure of the redundancy of the dataset at the data level, by calculating the 
 * Duplicate Instance metric, which is part of the Conciseness dimension.
 * @author Santiago Londono
 */
public class ActualUsageUnambiguousAnnotations implements QualityMetric {
	
	private static Logger logger = LoggerFactory.getLogger(ActualUsageUnambiguousAnnotations.class);
	
	private final Resource METRIC_URI = DQM.UsageUnambiguousAnnotationsMetric;
			
	/**
	 * MapDB database, used to persist the Map containing the instances found to be declared in the dataset
	 */
	private DB mapDB = MapDbFactory.createFilesystemDB();
	
	/**
	 * Map indexing the instances found to be declared in the dataset. Key of entries is a combination of the 
	 * URI of the subject and object of the statement (triple) declaring the instance. 
	 * A ConcurrentHashMap is used since it is synchronized and metric instances ought to be thread safe.
	 */
	private HTreeMap<String, Instance> pMapInstances = this.mapDB.createHashMap("unambiguous-annotations-map").make();
	
	@Override
	public void compute(Quad quad) {
		
		// Check whether current triple corresponds to an instance declaration
		logger.trace("Computing triple with predicate: " + quad.getPredicate().getURI());
		Node predicateEdge = quad.getPredicate();
		Node subjectEdge = quad.getSubject();
				
		if(predicateEdge != null && predicateEdge.isURI() && subjectEdge != null && subjectEdge.isURI() && quad.getObject() != null) {
			// Build the Id of the instance, concatenating the instance's (subject) URI and the URI of its class
			String subjectUri = subjectEdge.toString().trim();
			String predicateUri = predicateEdge.toString().trim();
			String objectValue = quad.getObject().toString().trim();
			
			// Check if the instance already exists in the instances map
			Instance instance = pMapInstances.get(subjectUri);
	
			if(instance == null) {
				// Put the new instance declaration in the map
				instance = new Instance();
				logger.trace("New instance declaration added to table {}", subjectUri);
			}
			
			// Get the current property value, if already part of the instance
			String curPropertyValue = instance.tblProperties.get(predicateUri); 
			
			if(curPropertyValue != null) {
				// Check if the value of the property, as previously set for the instance, is ambiguous wrt. to the new value
				if(!curPropertyValue.equals(objectValue)) {
					instance.isAmbiguous = true;
					logger.debug("Ambiguous property {} found for instance {}. Set value: {} - new value: {}", predicateUri, subjectUri, curPropertyValue, objectValue);
				}
			} else {
				// Set a new property for the instance
				instance.tblProperties.put(predicateUri, objectValue);
				
				// If the property corresponds to an instance declaration, mark the instance as created in the resource
				if(predicateEdge.hasURI(RDF.type.getURI())) {
					instance.isDeclared = true;
				}
				logger.trace("Instance {} got property {} with value {}", subjectUri, predicateUri, objectValue);
			}
			
			// Update table
			this.pMapInstances.put(subjectUri, instance);
		}
	}
	
	@Override
	public double metricValue() {
		// Count the number of ambiguous instances found in the document, and the total instances declared
		int countInstancesDecl = 0;
		int countAmbiguousInst = 0;
		
		for(Instance entryInstance : this.pMapInstances.values()) {
			// Instances declared in the document (for which an rdf:type triple was found)...
			if(entryInstance.isDeclared) {
				countInstancesDecl++;
				// Which are also found to be ambiguous, are counted as such. All others are IGNORED!
				if(entryInstance.isAmbiguous) {
					countAmbiguousInst++;
				}
			}
		}
		
		return 1.0 - ((double)countAmbiguousInst / (double)countInstancesDecl);
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

	/**
	 * Represents an instance declared in a semantic metadata document (by means of the rdf:type property)
	 * @author slondono
	 */
	private static class Instance implements Serializable {
		private static final long serialVersionUID = 2007651933549363670L;

		/**
		 * Table containing all the properties and their respective values for this instance
		 */
		private HashMap<String, String> tblProperties;
		
		/**
		 * Tells whether the instance has been found to be ambiguous
		 */
		private boolean isAmbiguous;
		
		/**
		 * If true, the instance was declared in the semantic resource by means of an rdf:type triple
		 */
		private boolean isDeclared;
		
		/**
		 * Constructor
		 */
		private Instance() {
			this.tblProperties = new HashMap<String, String>();
			this.isAmbiguous = false;
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		
		// Destroy persistent HashMap and the corresponding database
		try {
			if(this.pMapInstances != null) {
				this.pMapInstances.close();
			}
			if(this.mapDB != null && !this.mapDB.isClosed()) {
				this.mapDB.close();
			}
		} catch(Throwable ex) {
			logger.warn("Persistent HashMap or backing database could not be closed", ex);
		} finally {
			try {
				super.finalize();
			} catch(Throwable ex) {
				logger.warn("Persistent HashMap or backing database could not be closed", ex);
			}
		}
	}

}
