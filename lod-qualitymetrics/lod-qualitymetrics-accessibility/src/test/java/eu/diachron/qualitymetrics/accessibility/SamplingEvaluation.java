/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.Pair;
import de.unibonn.iai.eis.luzzu.cache.CacheManager;
import de.unibonn.iai.eis.luzzu.properties.PropertyManager;
import eu.diachron.qualitymetrics.accessibility.availability.Dereferenceability;
import eu.diachron.qualitymetrics.accessibility.availability.EstimatedDereferenceability;
import eu.diachron.qualitymetrics.accessibility.availability.EstimatedDereferenceabilityByStratified;
import eu.diachron.qualitymetrics.accessibility.availability.EstimatedDereferenceabilityByTld;
import eu.diachron.qualitymetrics.cache.DiachronCacheManager;
import eu.diachron.qualitymetrics.utilities.TestLoader;

/**
 * @author Jeremy Debattista
 * 
 */
public class SamplingEvaluation {

	protected static TestLoader loader = new TestLoader();

	protected static int[] paramsEstDeref = { 1000, 3000, 5000};
	protected static Pair<?,?>[] paramsTLDExt = new Pair<?,?>[] { 
//		new Pair<Integer, Integer>(10,50) 
//		new Pair<Integer, Integer>(10,100), 
		
		new Pair<Integer, Integer>(10,1000), 
		new Pair<Integer, Integer>(50,1000), 
		new Pair<Integer, Integer>(10,3000),
		new Pair<Integer, Integer>(50,3000), 
		new Pair<Integer, Integer>(10,5000), 
		new Pair<Integer, Integer>(50,5000)
		
//		new Pair<Integer, Integer>(10,10000),
//		new Pair<Integer, Integer>(10,25000),
//		new Pair<Integer, Integer>(10,50000),
//		new Pair<Integer, Integer>(10,100000),
//		new Pair<Integer, Integer>(50,50), 
//		new Pair<Integer, Integer>(50,100), 
//		new Pair<Integer, Integer>(50,1000), 
//		new Pair<Integer, Integer>(50,5000),
//		new Pair<Integer, Integer>(50,10000),
//		new Pair<Integer, Integer>(50,25000),
//		new Pair<Integer, Integer>(50,50000),
//		new Pair<Integer, Integer>(50,100000)
	};
	
	protected static String[] datasets = {
		"/tmp/sampling_datasets/LAK-DATASET-DUMP.nt.gz",
		"/tmp/sampling_datasets/lsoa.nt.gz",
		"/tmp/sampling_datasets/soton.nt.gz",
		"/tmp/sampling_datasets/wn20.nt.gz",
		"/tmp/sampling_datasets/swetodblp_august2007.rdf.gz",
		"/tmp/sampling_datasets/semanticxbrl.nt.gz",
	};
	
	
	protected static Map<String,String> dsToURI = new HashMap<String,String>();
	static{
		dsToURI.put("/tmp/sampling_datasets/LAK-DATASET-DUMP.nt.gz", "http://data.linkededucation.org/resource/lak/reference/edm");
		dsToURI.put("/tmp/sampling_datasets/lsoa.nt.gz", "http://opendatacommunities.org/id/geography/lsoa");
		dsToURI.put("/tmp/sampling_datasets/soton.nt.gz", "http://data.southampton.ac.uk/dumps/eprints");
		dsToURI.put("/tmp/sampling_datasets/wn20.nt.gz", "http://www.w3.org/2006/03/wn/wn20");
		dsToURI.put("/tmp/sampling_datasets/swetodblp_august2007.rdf.gz", "http://dblp.uni-trier.de/rec/bibtex");
		dsToURI.put("/tmp/sampling_datasets/semanticxbrl.nt.gz", "http://rhizomik.net/semanticxbrl");
	}
	
	
	public static void dereferenceability(String dataset){
		loader.loadDataSet(dataset);
		PropertyManager.getInstance().addToEnvironmentVars("baseURI", dsToURI.get(dataset));

		System.out.println("Evaluating Dereferencability");
		long tMin = Long.MAX_VALUE;
		long tMax = Long.MIN_VALUE;
		long tAvg = 0;

		for (int i = 0; i < 1; i++){
			List<Quad> streamingQuads = loader.getStreamingQuads();
			long tStart = System.currentTimeMillis();
			Dereferenceability m = new Dereferenceability();
			m.setDatasetURI(dsToURI.get(dataset));

			for(Quad quad : streamingQuads){
				m.compute(quad);
			}
			
			if (i < 0){
				m.metricValue();
			} else {
				System.out.println("Count : " + i + " Value : "+ m.metricValue());
			}
			
			long tEnd = System.currentTimeMillis();
			if (i >= 0){
				long difference = tEnd - tStart;
				tAvg += difference;
				tMax = (tMax < difference) ? difference : tMax;
				tMin = (tMin > difference) ? difference : tMin;
			}
		}
		tAvg = tAvg/2;
		System.out.println("Min: "+ (tMin/1000.0) + " Max: "+ (tMax/1000.0) + " Avg: "+ (tAvg/1000.0));
	}

	public static void estimatedDereferenceability(String dataset){
		loader.loadDataSet(dataset);
		PropertyManager.getInstance().addToEnvironmentVars("baseURI", "http://social.mercedes-benz.com/de/");

		System.out.println("Evaluating EstimatedDereferenceability");
		
		for (int param : paramsEstDeref){
			System.out.println("Parameter: "+ param);
			long tMin = Long.MAX_VALUE;
			long tMax = Long.MIN_VALUE;
			long tAvg = 0;

			for (int i = 0; i < 1; i++){
				List<Quad> streamingQuads = loader.getStreamingQuads();
				long tStart = System.currentTimeMillis();
				EstimatedDereferenceability m = new EstimatedDereferenceability();
				m.setMAX_FQURIS(param);

				for(Quad quad : streamingQuads){
					m.compute(quad);
				}
				
				if (i < 0){
					m.metricValue();
				} else {
					System.out.println("Count : " + i + " Value : "+ m.metricValue());
				}
				
				long tEnd = System.currentTimeMillis();
				if (i >= 0){
					long difference = tEnd - tStart;
					tAvg += difference;
					tMax = (tMax < difference) ? difference : tMax;
					tMin = (tMin > difference) ? difference : tMin;
				}
				CacheManager.getInstance().clearCache(DiachronCacheManager.HTTP_RESOURCE_CACHE);
			}
			tAvg = tAvg/2;
			System.out.println("Min: "+ (tMin/1000.0) + " Max: "+ (tMax/1000.0) + " Avg: "+ (tAvg/1000.0));
		}
	}
	
	public static void estimatedDereferenceabilityTLD(String dataset){
		loader.loadDataSet(dataset);
		PropertyManager.getInstance().addToEnvironmentVars("baseURI", "http://social.mercedes-benz.com/de/");

		System.out.println("Evaluating EstimatedDereferenceabilityTLD");
		
		for (Pair<?,?> param : paramsTLDExt){
			System.out.println("Parameter: "+ param);
			long tMin = Long.MAX_VALUE;
			long tMax = Long.MIN_VALUE;
			long tAvg = 0;

			for (int i = 0; i < 2; i++){
				List<Quad> streamingQuads = loader.getStreamingQuads();
				long tStart = System.currentTimeMillis();
				EstimatedDereferenceabilityByTld m = new EstimatedDereferenceabilityByTld();
				m.MAX_TLDS = (Integer) param.getFirstElement();
				m.MAX_FQURIS_PER_TLD = (Integer) param.getSecondElement();

				for(Quad quad : streamingQuads){
					m.compute(quad);
				}
				
				if (i < 0){
					m.metricValue();
				} else {
					System.out.println("Count : " + i + " Value : "+ m.metricValue());
				}
				
				long tEnd = System.currentTimeMillis();
				if (i >= 0){
					long difference = tEnd - tStart;
					tAvg += difference;
					tMax = (tMax < difference) ? difference : tMax;
					tMin = (tMin > difference) ? difference : tMin;
				}
				CacheManager.getInstance().clearCache(DiachronCacheManager.HTTP_RESOURCE_CACHE);
			}
			tAvg = tAvg/2;
			System.out.println("Min: "+ (tMin/1000.0) + " Max: "+ (tMax/1000.0) + " Avg: "+ (tAvg/1000.0));
		}
	}

	public static void estimatedDereferenceabilityByStratified(String dataset){
		PropertyManager.getInstance().addToEnvironmentVars("baseURI", dsToURI.get(dataset));
		loader.loadDataSet(dataset);

		System.out.println("Evaluating EstimatedDereferenceabilityByStratified");
		
		for (Pair<?,?> param : paramsTLDExt){
			System.out.println("Parameter: "+ param);
			long tMin = Long.MAX_VALUE;
			long tMax = Long.MIN_VALUE;
			long tAvg = 0;

			for (int i = -1; i < 2; i++){
				List<Quad> streamingQuads = loader.getStreamingQuads();
				long tStart = System.currentTimeMillis();
				EstimatedDereferenceabilityByStratified m = new EstimatedDereferenceabilityByStratified();
				m.MAX_TLDS = (Integer) param.getFirstElement();
				m.MAX_FQURIS_PER_TLD = (Integer) param.getSecondElement();
				m.setDatasetURI(dsToURI.get(dataset));
				
				for(Quad quad : streamingQuads){
					m.compute(quad);
				}
				
				if (i < 0){
					m.metricValue();
				} else {
					System.out.println("Count : " + i + " Value : "+ m.metricValue());
				}
				
				long tEnd = System.currentTimeMillis();
				if (i >= 0){
					long difference = tEnd - tStart;
					tAvg += difference;
					tMax = (tMax < difference) ? difference : tMax;
					tMin = (tMin > difference) ? difference : tMin;
				}
				CacheManager.getInstance().clearCache(DiachronCacheManager.HTTP_RESOURCE_CACHE);
			}
			tAvg = tAvg/3;
			System.out.println("Min: "+ (tMin/1000.0) + " Max: "+ (tMax/1000.0) + " Avg: "+ (tAvg/1000.0));
		}
	}
	
	
	public static void main (String [] args) {
		for (String d : datasets){
			System.out.println(d);
			estimatedDereferenceabilityByStratified(d);
			estimatedDereferenceabilityTLD(d);
			dereferenceability(d);
//			estimatedDereferenceability(d);
		}
	}
}

