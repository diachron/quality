package de.unibonn.iai.eis.diachron.qualitymetrics.trust.believability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DCTerms;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;
import de.unibonn.iai.eis.diachron.vocabularies.DQM;

/**
 * @author Carlos Montoya Verifies whether dataset contained such as attributes defined to provide
 * Trustworthiness information of the database
 */
public class TrustworthinessRDFStatement extends AbstractQualityMetric {

	private final Resource METRIC_URI = DQM.TrustworthinessRDFStatementMetric;

	private static Logger logger = LoggerFactory.getLogger(TrustworthinessRDFStatement.class);

	/**
	 * Attributes to verify in the dataset
	 */
	private int publisher = 0;
	private int creator = 0;
	private int created = 0;
	private int source = 0;
	private int title = 0;
	private int content = 0;
	private int homeurl = 0;
	private int provenance = 0;
	private double metricValue;
	
	private String uriDataset;
	
	/**
	 * Processes a single quad being part of the dataset. It try to obtain relevant information that is identify as
	 * trust information for a dataset, then it count every value to give the value of the metric.
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
				
		String curSubjectURI = ((subject.isURI())?(subject.getURI()):(subject.toString()));
		
		// Check if the property of the quad is known to provide licensing
		// information
		if (predicate != null && predicate.isURI() && subject != null) {
			// Search for the predicate's URI in the set of trustworthiness values
			// properties...
			if(predicate.getURI().equals(DCTerms.creator.getURI()) || predicate.getURI().equals("http://purl.org/pav/createdBy")) {
				creator = 1;				
				logger.trace("Quad providing creator of the dataset info detected. Subject: {}, object: {}", curSubjectURI, object);
			}//
			else if(predicate.getURI().equals(DCTerms.created.getURI())) {
				created = 1;				
				logger.trace("Quad providing created of the dataset info detected. Subject: {}, object: {}", curSubjectURI, object);
			}
			else if(predicate.getURI().equals(DCTerms.publisher.getURI())) {
				publisher = 1;				
				logger.trace("Quad providing publisher of the dataset info detected. Subject: {}, object: {}", curSubjectURI, object);
			}
			else if(predicate.getURI().equals(DCTerms.source.getURI())) {
				publisher = 1;				
				logger.trace("Quad providing publisher of the dataset info detected. Subject: {}, object: {}", curSubjectURI, object);
			}
			else if(predicate.getURI().equals(DCTerms.title.getURI())) {
				title = 1;				
				logger.trace("Quad providing title of the dataset info detected. Subject: {}, object: {}", curSubjectURI, object);
			}//Check if the quad contain info related with the content of the data set
			else if(predicate.getURI().equals(DCTerms.description.getURI())){
				content = 1;
				logger.trace("Quad providing content of the dataset info detected. Subject: {}, object: {}", curSubjectURI, object);				
			}
			else if(predicate.getURI().equals(FOAF.homepage.getURI())){
				//Check that if the homepage makes reference to it self.
				if(curSubjectURI.equals(this.getUriDataset())){
					homeurl = 1;
					logger.trace("Quad providing homepage of the dataset info detected. Subject: {}, object: {}", curSubjectURI, object);
				}
			}
			else if(predicate.getURI().equals(DCTerms.provenance.getURI())){
				provenance = 1;
				logger.trace("Quad providing provenance of the dataset info detected. Subject: {}, object: {}", curSubjectURI, object);
			}
		}
	}

	/**
	 * Returns the current value of the Machine-readable indication of a
	 * believability of the dataset metric in milliseconds, the value of the
	 * metric will be 1, if the dataset containing the processed quads contains
	 * all the attributes known to be trusworthiness, -1 otherwise
	 * 
	 * @return Current value of the Machine-readable indication of a license
	 *         metric, measured for the whole dataset. [-1,1] depending the amount of attributes 
	 */
	@Override
	public double metricValue() {
		//Count all the attributes that should have to be perfect trust or not
		int count = publisher + creator + created + source + title + content + homeurl + provenance;
		
		//If the data source doesn't have info it return -1 and it start to count the values depending in the amount of values
		if(count == 0)
			return -1;
		else if(count == 1)
			return -0.75;
		else if(count == 2)
			return -0.50;
		else if(count == 3)
			return -0.25;
		else if(count == 4)
			return 0;
		else if(count == 5)
			return 0.25;
		else if(count == 6)
			return 0.50;
		else if(count == 7)
			return 0.75;
		else if(count == 8)
			return 1;
		else
			return (Double) null;
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
	 * @return the uriDataset
	 */
	public String getUriDataset() {
		return uriDataset;
	}

	/**
	 * @param uriDataset the uriDataset to set
	 */
	public void setUriDataset(String uriDataset) {
		this.uriDataset = uriDataset;
	}

}
