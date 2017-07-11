/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.riot.lang.PipedRDFIterator;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.Pair;
import de.unibonn.iai.eis.luzzu.assessment.ComplexQualityMetric;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.cache.CacheManager;
import de.unibonn.iai.eis.luzzu.datatypes.Object2Quad;
import de.unibonn.iai.eis.luzzu.exceptions.AfterException;
import de.unibonn.iai.eis.luzzu.exceptions.BeforeException;
import de.unibonn.iai.eis.luzzu.properties.PropertyManager;
import eu.diachron.qualitymetrics.accessibility.availability.Dereferenceability;
import eu.diachron.qualitymetrics.accessibility.availability.EstimatedDereferenceabilityByStratified;
import eu.diachron.qualitymetrics.accessibility.interlinking.EstimatedInterlinkDetectionMetric;
import eu.diachron.qualitymetrics.accessibility.interlinking.EstimatedLinkExternalDataProviders;
import eu.diachron.qualitymetrics.accessibility.interlinking.LinkExternalDataProviders;
import eu.diachron.qualitymetrics.cache.DiachronCacheManager;
import eu.diachron.qualitymetrics.utilities.TestLoader;

/**
 * @author Jeremy Debattista
 * 
 */
public class RuntimeEvaluation {

	protected static TestLoader loader = new TestLoader();

//	CorrectURIUsage.class -> not a quality indicator per se.. why would a slash URI be wrong for example in our case regarding the quality metadata?
//  DigitalSignatureUsage.class -> we dont really need this because this metric is only related for foaf docs
	
	protected static QualityMetric m; //EstimatedDereferenceabilityByStratified.class EstimatedMisreportedContentType.class EstimatedLinkExternalDataProviders.class
	protected static Class<?>[] testing = new Class<?>[] { 
		EstimatedLinkExternalDataProviders.class,
		LinkExternalDataProviders.class
	};
	
	
	
	protected static String[] datasets = {
		"/Volumes/KINGSTON/sampling_datasets/LAK-DATASET-DUMP.nt.gz",
		"/Volumes/KINGSTON/sampling_datasets/lsoa.nt.gz",
		"/Volumes/KINGSTON/sampling_datasets/soton.nt.gz",
		"/Volumes/KINGSTON/sampling_datasets/wn20.nt.gz",
		"/Volumes/KINGSTON/sampling_datasets/swetodblp_august2007.rdf.gz",
		"/Volumes/KINGSTON/sampling_datasets/semanticxbrl.nt.gz",
	};
	
	
	protected static Map<String,String> dsToURI = new HashMap<String,String>();
	static{
		dsToURI.put("/Volumes/KINGSTON/sampling_datasets/LAK-DATASET-DUMP.nt.gz", "http://data.linkededucation.org/resource/lak/reference/edm");
		dsToURI.put("/Volumes/KINGSTON/sampling_datasets/lsoa.nt.gz", "http://opendatacommunities.org/id/geography/lsoa");
		dsToURI.put("/Volumes/KINGSTON/sampling_datasets/soton.nt.gz", "http://data.southampton.ac.uk/dumps/eprints");
		dsToURI.put("/Volumes/KINGSTON/sampling_datasets/wn20.nt.gz", "http://www.w3.org/2006/03/wn/wn20");
		dsToURI.put("/Volumes/KINGSTON/sampling_datasets/swetodblp_august2007.rdf.gz", "http://dblp.uni-trier.de/rec/bibtex");
		dsToURI.put("/Volumes/KINGSTON/sampling_datasets/semanticxbrl.nt.gz", "http://rhizomik.net/semanticxbrl");
	}
	
	
	protected static int[] params = {
//		0.1, 0.15, 0.2, 0.25, 0.3
		5,10,25,100,250,1000,10000
	};
	
	
	
	public static void nonEstimate(String dataset){

		System.out.println("Evaluating Non-Estimate");
		long tMin = Long.MAX_VALUE;
		long tMax = Long.MIN_VALUE;
		long tAvg = 0;

		for (int i = -1; i < 2; i++){
//			List<Quad> streamingQuads = loader.getStreamingQuads();
			PipedRDFIterator<?> iter = loader.streamParser(dataset);

			long tStart = System.currentTimeMillis();
			LinkExternalDataProviders m = new LinkExternalDataProviders();
			m.setDatasetURI(dsToURI.get(dataset));

			while(iter.hasNext()){
				Object nxt = iter.next();
				Object2Quad quad = new Object2Quad(nxt);
				m.compute(quad.getStatement());
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
	
	
	public static void estimate(String dataset){

		System.out.println("Evaluating Estimate");
		
		for (Integer param : params){
			System.out.println("Parameter: "+ param);
			long tMin = Long.MAX_VALUE;
			long tMax = Long.MIN_VALUE;
			long tAvg = 0;

			for (int i = -1; i < 2; i++){
				//List<Quad> streamingQuads = loader.getStreamingQuads();
				PipedRDFIterator<?> iter = loader.streamParser(dataset);
				
				long tStart = System.currentTimeMillis();
				EstimatedLinkExternalDataProviders m = new EstimatedLinkExternalDataProviders();
				m.reservoirsize = param;
				m.setDatasetURI(dsToURI.get(dataset));
				
				while(iter.hasNext()){
					Object nxt = iter.next();
					Object2Quad quad = new Object2Quad(nxt);
					m.compute(quad.getStatement());
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
	}
	
	
	
	public static void main (String [] args){
		for (String dataset : datasets){
			System.out.println(dataset);
			estimate(dataset);
//			nonEstimate(dataset);
		}
	}
}


