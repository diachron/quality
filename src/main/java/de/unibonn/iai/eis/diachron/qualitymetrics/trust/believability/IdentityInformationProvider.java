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
 * @author Carlos Montoya Verifies whether consumers of the dataset are
 *         explicitly granted permission to re-use it, under defined conditions,
 *         by annotating the resource with a machine-readable indication (e.g. a
 *         VoID description) of the license.
 */
public class IdentityInformationProvider extends AbstractQualityMetric {

	private final Resource METRIC_URI = DQM.IdentityInformationProviderMetric;

	private static Logger logger = LoggerFactory.getLogger(IdentityInformationProvider.class);

	private static String filePath = "src/main/resources/believabilityResources/trustproviders.txt";
	
	/**
	 * Set of all the URIs of properties known to provide information related
	 * with the provider and/or contributors
	 */
	private static HashSet<String> setTrustedProviders;
	public int counter =0;
	public int counterProviders=0;
	private double metricValue;
	
	static {
		try {
			File dir = new File(".");
			File fin = new File(dir.getCanonicalPath() + File.separator + filePath);
			setTrustedProviders = readFileTrustedProviders(fin);
		} catch (IOException e) {			
			e.printStackTrace();
		}
	}

	
	/**
	 * Processes a single quad being part of the dataset. First it try to figure
	 * if the quad contain information regarding the title if yes then it store
	 * the info in the variable provided to it If not then it try to whether the
	 * quad contains content information (by checking if the property is part of
	 * those known to be about the content of the dataset information) and if
	 * so, stores in the variable If not then it try to whether the quad
	 * contains homepage information (by checking if the property is part of
	 * those known to be about the homepage of the dataset information) and if
	 * so, stores in the variable
	 * 
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


		String curObjectURI = ((object.isURI()) ? (object.getURI()): (object.toString()));
		// Check if the property of the quad is known to provide licensing
		// information
		if (predicate != null && predicate.isURI() && subject != null) {
			// Search for the predicate's URI in the set of license
			// properties...
			if(predicate.getURI().equals(DCTerms.contributor.getURI())){
								
				this.counterProviders++;		
				if (setTrustedProviders.contains(curObjectURI)) {
					this.counter ++;
					String curSubjectURI = ((subject.isURI()) ? (subject.getURI()): (subject.toString()));
					logger.trace(
							"Quad providing contributor of the dataset info detected. Subject: {}, object: {}",
							curSubjectURI, object);
	
				}
				
			}
		}
	}

	/**
	 * 
	 */
	@Override
	public double metricValue() {
		if(counter>0){
			return new Double(this.counter)/new Double(this.counterProviders);
		}
		return 0;
	}

	/**
	 * This method load the file that contain the list of trusted providers
	 * @param fin Input File
	 * @throws IOException Exception in the program is not able to load the file 
	 */
	private static HashSet<String> readFileTrustedProviders(File fin) throws IOException {
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

}
