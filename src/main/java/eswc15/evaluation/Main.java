package eswc15.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.lang.PipedQuadsStream;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.apache.jena.riot.lang.PipedTriplesStream;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.Object2Quad;
import de.unibonn.iai.eis.luzzu.exceptions.ProcessorNotInitialised;
import eswc15.evaluation.settings.EvaluationCase;
import eu.diachron.qualitymetrics.accessibility.interlinking.ActualClusteringCoefficiency;
import eu.diachron.qualitymetrics.accessibility.interlinking.EstimatedClusteringCoefficiency;
import eu.diachron.qualitymetrics.intrinsic.conciseness.ActualExtensionalConciseness;
import eu.diachron.qualitymetrics.intrinsic.conciseness.EstimatedExtensionalConciseness;


public class Main {
	
	private static long tStart;
	private static long tEnd;
	
	private static List<EvaluationCase> eCases = new ArrayList<EvaluationCase>();
	private static String datasetURI = "";//to setup
	
	private static void setUp() throws ClassNotFoundException, IOException{
		//setup 
		
		//Clustering Coefficency
		eCases.add(new EvaluationCase("Estimation value for Clustering Coefficency", new EstimatedClusteringCoefficiency()));
		eCases.add(new EvaluationCase("Actual value for Clustering Coefficency",new ActualClusteringCoefficiency()));
		
		//Extensional Conciseness
		eCases.add(new EvaluationCase("Estimation value for Extensional Conciseness", new EstimatedExtensionalConciseness()));
		eCases.add(new EvaluationCase("Actual value for Extensional Conciseness", new ActualExtensionalConciseness()));

	}
	
	public static void main (String [] args) throws ProcessorNotInitialised, IOException, ClassNotFoundException{
		//create csv file
		File csv = new File("benchmark.csv");
		File values = new File("values.txt");
		
		if (!(csv.isFile())) {
			csv.createNewFile();
			String header = "triples,metrics,average time(s),min time(s),max time(s)";
			FileUtils.write(csv, header, true);
			FileUtils.write(csv, System.getProperty("line.separator"), true);
		}
		
		if (!(values.isFile())) {
			values.createNewFile();
		}
		
		eCases = new ArrayList<EvaluationCase>();
		setUp();
		int iterations = 10;
		
		for (EvaluationCase eCase : eCases){
			System.out.println("Evaluating " + eCase.getCaseName());
			System.out.println("=================================");
						
			//Run benchmark for 10 iterations + 3 cold starts
			for(int i = -2; i <= iterations; i++){
				if (i >= 1){
					//process
					processDataSet(eCase.getMetric());
					
					tStart = System.currentTimeMillis();
					eCase.addMetricValue(eCase.getMetric().metricValue());
					tEnd = System.currentTimeMillis();
					long difference = tEnd - tStart;
					eCase.setDifference(difference);
					System.out.println("Iteration # : " + i + " - " + (difference / 1000.0));
				} else {
					processDataSet(eCase.getMetric());
					eCase.getMetric().metricValue();
					System.out.println("Cold Run.."+ (i + 2));
				}
				
				eCase.resetMetric();
			}
			
			FileUtils.write(csv, eCase.toString(), true);
			FileUtils.write(csv, System.getProperty("line.separator"), true);
			
			String v_header = eCase.getCaseName();
			FileUtils.write(values, v_header, true);
			FileUtils.write(values, System.getProperty("line.separator"), true);
			FileUtils.write(values, eCase.valuesToString(), true);
		}
	}
	
	private static PipedRDFIterator<?> iterator;
	protected  static PipedRDFStream<?> rdfStream;
	private static ExecutorService executor = Executors.newSingleThreadExecutor();
	
	@SuppressWarnings("unchecked")
	private static void processDataSet(QualityMetric metric){
		Lang lang  = RDFLanguages.filenameToLang(datasetURI);

		if ((lang == Lang.NQ) || (lang == Lang.NQUADS)){
			iterator = new PipedRDFIterator<Quad>();
			rdfStream = new PipedQuadsStream((PipedRDFIterator<Quad>) iterator);
		} else {
			iterator = new PipedRDFIterator<Triple>();
			rdfStream = new PipedTriplesStream((PipedRDFIterator<Triple>) iterator);
		}
		
		Runnable parser = new Runnable(){
			public void run() {
				RDFDataMgr.parse(rdfStream, datasetURI);
			}
		};
		
		executor.submit(parser); 
		
		while (iterator.hasNext()){
			Object2Quad stmt = new Object2Quad(iterator.next());
			metric.compute(stmt.getStatement());
		}
		
		iterator = null;
		rdfStream = null;
		executor = Executors.newSingleThreadExecutor();
	}
}
