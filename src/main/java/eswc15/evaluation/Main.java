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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.commons.bigdata.MapDbFactory;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.Object2Quad;
import de.unibonn.iai.eis.luzzu.exceptions.ProcessorNotInitialised;
import eswc15.evaluation.settings.EvaluationCase;
import eu.diachron.qualitymetrics.accessibility.availability.ActualDereferencibility;
import eu.diachron.qualitymetrics.accessibility.availability.ActualDereferencibilityBackLinks;
import eu.diachron.qualitymetrics.accessibility.availability.DereferencibilityForwardLinks;
import eu.diachron.qualitymetrics.accessibility.interlinking.ActualClusteringCoefficiency;
import eu.diachron.qualitymetrics.accessibility.interlinking.ActualLinkExternalDataProviders;
import eu.diachron.qualitymetrics.intrinsic.conciseness.ActualExtensionalConciseness;
import eu.diachron.qualitymetrics.intrinsic.conciseness.ActualDuplicateInstance;
import eu.diachron.qualitymetrics.utilities.HTTPRetriever;

public class Main {

	private static long tStart;
	private static long tEnd;
	
	private static Logger logger = LoggerFactory.getLogger(Main.class);
	
	private static List<EvaluationCase> eCases = new ArrayList<EvaluationCase>();
	private static String datasetURI = "/Users/jeremy/Downloads/lak-dataset-dump.nt"; //to setup
	
	private static void setUp() throws ClassNotFoundException, IOException{
		// Setup of metrics to be evaluated
//		eCases.add(new EvaluationCase("Actual value for Extensional Conciseness", new ActualExtensionalConciseness()));
//		eCases.add(new EvaluationCase("Actual value for Duplicate Instance", new ActualDuplicateInstance()));
//		eCases.add(new EvaluationCase("Actual value for Dereferenceability", new ActualDereferencibility()));
		eCases.add(new EvaluationCase("Actual value for Dereferenceability of Back-links", new ActualDereferencibilityBackLinks()));
//		eCases.add(new EvaluationCase("Actual value for Dereferenceability of Forward-links", new DereferencibilityForwardLinks()));
//		eCases.add(new EvaluationCase("Actual value for Clustering Coefficiency", new ActualClusteringCoefficiency()));
		eCases.add(new EvaluationCase("Actual value for Link to External Data Providers", new ActualLinkExternalDataProviders()));
	}
	
	public static void main (String [] args) throws ProcessorNotInitialised, IOException, ClassNotFoundException {
		
		// Verify if the path of the dataset was specified as argument
		if(args != null && args.length > 0) {
			// The dataset path should be specified as first argument
			if(args.length > 0 && args[0] != null && !args[0].trim().equals("")) {
				Main.datasetURI = args[0].trim();
				logger.debug("Dataset URI set to: " + Main.datasetURI);
			}			
			// The mapdb path should be specified as second argument
			if(args.length > 1 && args[1] != null && !args[1].trim().equals("")) {
				MapDbFactory.setMapDbDirectory(args[1].trim());
				logger.debug("Mapdb filesystem path set to: " + args[1].trim());
			}
			// The webproxy to be used to issue HTTP requests should be specified as third argument
			if(args.length > 2 && args[2] != null && !args[2].trim().equals("")) {
				HTTPRetriever.setWebProxy(args[2].trim());
				logger.debug("HTTP retriever web-proxy set to: " + HTTPRetriever.getWebProxy());
			}
		}
		
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
		int iterations = 1;
		
		try {
			for (EvaluationCase eCase : eCases) {
				System.out.println("Evaluating " + eCase.getCaseName());
				System.out.println("=================================");

				//Run benchmark for 10 iterations + 3 cold starts
				for(int i = 0; i <= iterations; i++){
					if (i >= 1){
						logger.debug("Starting iteration #: {}...", i);
						
						//process
						tStart = System.currentTimeMillis();
						long totalTriples = processDataSet(eCase.getMetric());
						
						logger.debug("Computing metric value...");
						eCase.addMetricValue(eCase.getMetric().metricValue());
						tEnd = System.currentTimeMillis();
						
						long difference = tEnd - tStart;
						eCase.setDifference(difference);
						eCase.setTotalTriples(totalTriples);
						System.out.println("Iteration # : " + i + " - " + (difference / 1000.0) + " - Triples : " + totalTriples);
					} else {
						logger.debug("Starting cold-run #: {}...", i);
						long totalTriples = processDataSet(eCase.getMetric());
						logger.debug("Computing metric value...");
						eCase.getMetric().metricValue();
						System.out.println("Cold Run.." + (i + 1) + " - Total triples: " + totalTriples);
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
			logger.debug("Evaluation completed!");

		} catch(Throwable ex) {
			logger.error("Error performing evaluation. Process aborted", ex);
		}
	}
	
	private static PipedRDFIterator<?> iterator;
	protected  static PipedRDFStream<?> rdfStream;
	private static ExecutorService executor = Executors.newSingleThreadExecutor();
	
	private static long processDataSet(QualityMetric metric) {
		File datasetFile = new File(datasetURI);
		long totalTriples = 0;
		logger.debug("Reading dataset: {}...", datasetURI);
		
		// If the dataset to be processed is a directory, process every file in it as a resource
		if(datasetFile.isDirectory()) {
			logger.debug("Dataset comprises multiple resources, processing them...");
			
			for(File dsResource : datasetFile.listFiles()) {
				totalTriples += processResource(metric, dsResource.getAbsolutePath());
			}
		} else {
			logger.debug("Dataset is a single resource...");
			
			// Process the dataset as a single file resource
			totalTriples += processResource(metric, datasetURI);
		}
		
		return totalTriples;
	}
	
	@SuppressWarnings("unchecked")
	private static long processResource(QualityMetric metric, final String resourceURI) {
		final Lang lang  = guessLanguage(resourceURI);
		long totalTriples = 0;
		logger.debug("Processing resource: {}, guessed language: {}", resourceURI, lang);
		
		if ((lang == Lang.NQ) || (lang == Lang.NQUADS)) {
			logger.debug("Resource is in NQ format, initializing Piped Quad Stream");
			iterator = new PipedRDFIterator<Quad>();
			rdfStream = new PipedQuadsStream((PipedRDFIterator<Quad>) iterator);
		} else {
			logger.debug("Resource is in triple collection format, initializing Piped Triples Stream");
			iterator = new PipedRDFIterator<Triple>();
			rdfStream = new PipedTriplesStream((PipedRDFIterator<Triple>) iterator);
		}
		
		Runnable parser = new Runnable(){
			public void run() {
				logger.debug("RDF parser thread started on resource: {}", resourceURI);
				RDFDataMgr.parse(rdfStream, resourceURI, lang, null);
			}
		};
		
		executor.submit(parser);
		executor.shutdown();
		logger.debug("Starting computation on statements...");
		
		while (iterator.hasNext()){
			try {
				Object2Quad stmt = new Object2Quad(iterator.next());
				metric.compute(stmt.getStatement());
				logger.trace("Computed metric on statement: {}", stmt.getStatement());
				totalTriples++;
			} catch(JenaException jex) {
				logger.warn("Jena error processing triple # {}, triple omitted. Reason: ", totalTriples, jex);
			}
		}
		logger.debug("Computation on statements completed!");
		
		iterator = null;
		rdfStream = null;
		executor = Executors.newSingleThreadExecutor();
		
		return totalTriples;
	}
	
	/**
	 * Try to guess the resource's language according to its URI
	 * @param resourceURI URI of the resource
	 * @return Language guessed
	 */
	private static Lang guessLanguage(String resourceURI) {
		Lang lang  = RDFLanguages.filenameToLang(resourceURI);

		// Hack to prevent errors reading RDFS files, Jena is not able to guess the language in those cases, event though is still RDF
		if(lang == null && resourceURI != null && resourceURI.toLowerCase().endsWith("rdfs")) {
			lang = Lang.RDFXML;
		}
		
		return lang;
	}
}
