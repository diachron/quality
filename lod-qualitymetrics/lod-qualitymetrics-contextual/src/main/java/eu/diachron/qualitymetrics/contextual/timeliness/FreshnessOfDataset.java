/**
 * 
 */
package eu.diachron.qualitymetrics.contextual.timeliness;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.contextual.timeliness.TimelinessSharedResources.DatasetFreshness;

/**
 * @author Jeremy Debattista
 * 
 * In this metric we check if the data in a dataset is
 * fresh wrt currency and volatility. In order words
 * we check if the data in the dataset is being updated
 * with the original data. One problem in this metric
 * is that we might not be able to retrieve the updated
 * date of the original dataset.
 * 
 * For this metric we use the Timeliness formula defined
 * in Using Web Data Provenance for Quality Assessment -
 * Hartig and Zhao. For currency we use the same formula
 * used in Currency of Dataset Metric
 */
public class FreshnessOfDataset implements QualityMetric {

	private List<Quad> _problemList = new ArrayList<Quad>();
	
	final static Logger logger = LoggerFactory.getLogger(FreshnessOfDataset.class);


	@Override
	public void compute(Quad quad) {
		Node subject = quad.getSubject();
		if ((subject.isURI()) && (subject.getURI().equals(EnvironmentProperties.getInstance().getDatasetURI()))){
			logger.debug("Checking dataset {}, with predicate {}", subject.getURI(), quad.getPredicate().getURI());

			DatasetFreshness df = TimelinessSharedResources.getOrCreate(subject.getURI());
			
			if (TemporalDataAnalyzer.isLastUpdateTime(quad)) df.setUpdateDate(TemporalDataAnalyzer.extractTimeFromObject(quad));
			if (TemporalDataAnalyzer.isPublisingTime(quad)) df.setPublishedDate(TemporalDataAnalyzer.extractTimeFromObject(quad));
			if (TemporalDataAnalyzer.isValidTime(quad)) df.setExpiryDate(TemporalDataAnalyzer.extractTimeFromObject(quad));
			if (TemporalDataAnalyzer.isCreationTime(quad)) df.setCreationDate(TemporalDataAnalyzer.extractTimeFromObject(quad));
		}
	}

	@Override
	public double metricValue() {
		double maxFreshness = 0.0;

		for (String key : TimelinessSharedResources.getURIs()){
			double currency = TimelinessSharedResources.calculateCurrency(TimelinessSharedResources.getOrCreate(key));
			double volatility = calculateVolatility(TimelinessSharedResources.getOrCreate(key));
			logger.debug("Currency value for dataset {} is {}; Volatility value is {}",key,currency,volatility);

			if ((currency > -1.0) && (volatility > -1.0)) {
				maxFreshness = Math.max(0, (1 - (currency / volatility)));
			} else {
				Quad q = new Quad(null, ModelFactory.createDefaultModel().createResource(key).asNode(), QPRO.exceptionDescription.asNode(), DQM.MissingMetadataForFreshness.asNode());
				this._problemList.add(q);
			}
		}
		
		return maxFreshness;
	}
	
	private double calculateVolatility(DatasetFreshness df){
		double volatility = -1.0;
		if (df.getExpiryDate() != null){
			long age = df.calculateAge();
			Long inputTime = null;
			Long expiryTime = null;
			if (age > -1.0){
				inputTime = (df.getCreationDate() == null) ? null : df.getCreationDate().getTime();
				expiryTime = (df.getExpiryDate() == null) ? null : df.getExpiryDate().getTime();
			}
			
			if ((inputTime != null) && (expiryTime != null)){
				volatility = expiryTime - (inputTime + age);
			}
		}
		return volatility;
	}

	@Override
	public Resource getMetricURI() {
		return DQM.FreshnessOfDatasetMetric;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		ProblemList<Quad> pl = null;
		try {
			if(this._problemList != null && this._problemList.size() > 0) {
				pl = new ProblemList<Quad>(this._problemList);
			} else {
				pl = new ProblemList<Quad>();
			}
		} catch (ProblemListInitialisationException e) {
			logger.error(e.getMessage());
		}
		return pl;
	}

	@Override
	public boolean isEstimate() {
		return false;
	}

	@Override
	public Resource getAgentURI() {
		return DQM.LuzzuProvenanceAgent;
	}

}
