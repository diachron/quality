package eu.diachron.qualitymetrics.intrinsic.conciseness;

import java.io.Serializable;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.apache.spark.Accumulator;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

import scala.Tuple2;

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
public class ExtensionalConcisenessSpark implements ComplexQualityMetric, Serializable {
	
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
//	transient private static HTreeMap<String, Tuple2<String,List<TupleValue>>> pMapSubjects = mapDB.createHashMap("extensional-conciseness-map").make();
	transient private static HTreeMap<String, Tuple2<String,String>> pMapSubjects = mapDB.createHashMap("extensional-conciseness-map").make();

	
	/**
	 * Re-computes the value of the Extensional Conciseness Metric, by considering a new quad provided.
	 * @param quad The new quad to be considered in the computation of the metric. Must be not null.
	 */
	public void compute(Quad quad) {
		
		Tuple2<String,String> lst = pMapSubjects.get(quad.getSubject().getURI()); // _1 is the prop&objects concat, _2 is subject
		
		String value = quad.getPredicate().getURI().toString() + " " + quad.getObject().toString() + " ";
		if (lst == null) { 
			Tuple2<String,String> tuple = new Tuple2<String,String>(value, quad.getSubject().getURI());
			pMapSubjects.put(quad.getSubject().getURI(), tuple);
		} 
		else {
			String concat = lst._1() + value;
			Tuple2<String,String> tuple = new Tuple2<String,String>(concat, quad.getSubject().getURI());
			pMapSubjects.put(quad.getSubject().getURI(), tuple);
		}
		
//		Tuple2<String,List<TupleValue>> lst = pMapSubjects.get(quad.getSubject().getURI());
//		
//		if (lst == null) { 
//			final List<TupleValue> value = new ArrayList<TupleValue>(); 
//			value.add(new TupleValue(quad.getPredicate().getURI(), quad.getObject().toString()));
//			final Tuple2<String,List<TupleValue>> tuple = new Tuple2<String,List<TupleValue>>(quad.getSubject().getURI(), value);
//			pMapSubjects.put(quad.getSubject().getURI(), tuple);
//		} 
//		else lst._2().add(new TupleValue(quad.getPredicate().getURI(), quad.getObject().toString()));

	}

	public void before(Object... args) {
		//Do nothing
	}

	
	private static SparkConf conf = new SparkConf().setAppName("Metrics").setMaster("local[4]"); // TODO: fix appname and master
	private static JavaSparkContext sc = new JavaSparkContext(conf);
	protected static long uniqueInstances = 0;
	private boolean afterInvoked = false;
	
	public void after(Object... args) {
		//for problem list we show those instances that are duplicated.. x  a problem; x maintriple abc; x hasduplicates list[ def, ghi ]
		afterInvoked = true;
		final Accumulator<Integer> accum = sc.accumulator(0);
		
		JavaPairRDD<String, String> instances = sc.parallelizePairs(new ArrayList<Tuple2<String,String>>(pMapSubjects.values()));
		
		JavaPairRDD<String, String> uniques = instances.reduceByKey(new Function2<String, String, String>(){
			private static final long serialVersionUID = 6119916898726575889L;

			public String call(String v1, String v2) throws Exception {
				accum.add(1);
				return null;
			}
		});
		
		uniqueInstances = uniques.count();

		
//		JavaPairRDD<String, List<TupleValue>> instances = sc.parallelizePairs(new ArrayList<Tuple2<String,List<TupleValue>>>(pMapSubjects.values()));
//		
//				
//		JavaPairRDD<String, List<TupleValue>> duplicates = instances.reduceByKey(new Function2<List<TupleValue>,List<TupleValue>,List<TupleValue>>(){
//			private static final long serialVersionUID = 6119916898726575889L;
//
//			public List<TupleValue> call(List<TupleValue> v1, List<TupleValue> v2) throws Exception {
//				List<TupleValue> retLst = new ArrayList<TupleValue>();
//				for(TupleValue v : v1){
//					for(TupleValue _v : v2){
//						if (v.compareTo(_v) == -1) retLst.add(v);
//					}
//				}
//				return retLst;
//			}
//		});
//		
//		duplicates.foreach(new VoidFunction<Tuple2<String,List<TupleValue>>>(){
//			private static final long serialVersionUID = -6139461891424722153L;
//
//			public void call(Tuple2<String, List<TupleValue>> t) throws Exception {
//				if (t._2.size() > 0) accum.add(1);
//			}
//		});
		
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
		
		double metricValue = ((double)uniqueInstances) / ((double)pMapSubjects.size()); // number of unique instances / tot number of instance representation
				
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
	
	private class TupleValue implements Comparable<TupleValue>, Serializable{
		
		protected String property = "";
		protected String object = "";
		
		TupleValue(String property, String object){
			this.property = property;
			this.object = object;
		}
		
		
		public int compareTo(TupleValue o) {
			if ((this.property.equals(o.property)) && (this.object.equals(o.object))) return 0;
			else return -1;
		}
	}
}
