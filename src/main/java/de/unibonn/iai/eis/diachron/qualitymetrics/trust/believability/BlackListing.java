package de.unibonn.iai.eis.diachron.qualitymetrics.trust.believability;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.DCTerms;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;

/**
 * @author Carlos Montoya Verifies whether the contributor or creator of the dataset are known 
 * 			for provide untrust information. then it return 0 if the dataset should be trustful.
 * 
 */
public class BlackListing extends AbstractQualityMetric {

	private final Resource METRIC_URI = DQM.BlackListingMetric;

	private static Logger logger = LoggerFactory.getLogger(BlackListing.class);

	private static String filePath = "src/main/resources/believabilityResources/blacklist.txt";
	
	/**
	 * Set of all the URIs of properties known to provide information related
	 * with the provider and/or contributors
	 */
	private static HashSet<String> setBlackListingProviders;
	private int counter = 0;
	private int counterCreators = 0;
	
	private double metricValue;
	
	
	//Here the project load the file of black listing providers.
	static {	
		try {
			File dir = new File(".");
			File fin = new File(dir.getCanonicalPath() + File.separator + filePath);
			setBlackListingProviders = readFileBlacklist(fin);
		} catch (IOException e) {			
			e.printStackTrace();
		}
	}

	
	/**
	 * Processes a single quad being part of the dataset. First it try to figure it 
	 * if the information contained infor related with the contributor or with the 
	 * creator, then if is like that it check against the black list, if it is contained
	 * it increase the counter.
	 * @param quad
	 *            Quad to be processed and examined to try to extract the
	 *            dataset's URI
	 */
	@Override
	public void compute(Quad quad) {
		// Extract the predicate (property) of the statement, the described
		// resource (subject) and the value set (object)
		Node subject = quad.getSubject();
		Node predicate = quad.getPredicate();
		Node object = quad.getObject();

		
		// Check if the property of the quad is known to provide licensing
		// information
		if (predicate != null && predicate.isURI() && subject != null) {
			// Search for the predicate's URI in the set of license
			// properties...
			if(predicate.getURI().equals(DCTerms.contributor.getURI())){
				
				String curObjectURI = ((object.isURI()) ? (object.getURI()): (object.toString()));
				setCounterCreators(getCounterCreators() + 1);				
				
				if (setBlackListingProviders.contains(curObjectURI)) {

					setCounter(getCounter() + 1);
					String curSubjectURI = ((subject.isURI()) ? (subject.getURI()): (subject.toString()));
					logger.trace(
							"Quad providing contributor or createor of the dataset info detected into the list. Subject: {}, object: {}",
							curSubjectURI, object);
	
				}
				
			}
		}
	}

	/**
	 * If the counter is higher that 0 means that the the provider is contained in the blacklist
	 * then it return 0, otherwise return 1, that means that the dataset is trustful
	 * The function is defined as
	 * f(x)={ 0.5  , if # untrusted publishers or creators is = 0, or
	 * 		  (1 - (# of untrusted publishers  or creators)/(# of publisher or creators)),   otherwise
	 */
	@Override
	public double metricValue() {
		if(getCounter()>0){			
			return new Double(1- new Double(this.getCounter())/new Double(this.getCounterCreators()));
		}
		else if (this.getCounterCreators() == 0 ){
			return new Double(0.5);
		}
		
		return 1;
	}

	/**
	 * This method load the file that contain the list of trusted providers
	 * @param fin Input File
	 * @throws IOException Exception in the program is not able to load the file 
	 */
	private static HashSet<String> readFileBlacklist(File fin) throws IOException {
		FileInputStream fis = new FileInputStream(fin);
		HashSet<String> providers = new HashSet<String>();
		
		//Construct BufferedReader from InputStreamReader
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
	 
		String line = null;
		while ((line = br.readLine()) != null) {
			providers.add(line);
		}
	 
		br.close();
		return providers;
	}
	
	@Override
	public Resource getMetricURI() {
		return METRIC_URI;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		return null;
	}

	/**
	 * @return the metricValue
	 */
	public double getMetricValue() {
		return metricValue;
	}

	/**
	 * @param metricValue the metricValue to set
	 */
	public void setMetricValue(double metricValue) {
		this.metricValue = metricValue;
	}

	/**
	 * @return the counterCreators
	 */
	public int getCounterCreators() {
		return counterCreators;
	}

	/**
	 * @param counterCreators the counterCreators to set
	 */
	public void setCounterCreators(int counterCreators) {
		this.counterCreators = counterCreators;
	}

	/**
	 * @return the counter
	 */
	public int getCounter() {
		return counter;
	}

	/**
	 * @param counter the counter to set
	 */
	public void setCounter(int counter) {
		this.counter = counter;
	}

}
