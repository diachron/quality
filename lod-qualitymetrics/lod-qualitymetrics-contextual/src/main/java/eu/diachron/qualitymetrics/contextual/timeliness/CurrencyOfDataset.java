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
 * In this metric we calculate the freshness (currency) of the data, i.e.
 * how fresh is the data when delivered to the user (or how old is the data). 
 * Having a low value does not mean that the data is outdated and wrong. For example 
 * historical facts will not be updated (if at all), but on the other hand, statistical
 * data might be updated daily. In this metric we do not take into consideration
 * the validity of the data
 * 
 * For this metric we use the definition of currency by Rula et al. in 
 * Capturing the Age of Linked Open Data: Towards a Dataset-independent Framework
 */
public class CurrencyOfDataset implements QualityMetric{

	private List<Quad> _problemList = new ArrayList<Quad>();
	
	final static Logger logger = LoggerFactory.getLogger(CurrencyOfDataset.class);

	//we cannot capture Age as required by [29] BUT we can say that age is the observed date, i.e. the date the dataset is assessed
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
		double avgSum = -1.0;
		double totalOk = 0.0;
		for (String key : TimelinessSharedResources.getURIs()){
			double currency = TimelinessSharedResources.calculateCurrency(TimelinessSharedResources.getOrCreate(key));
			logger.debug("Currency value for dataset {} is {}",key,currency);
			if (currency > -1.0) {
				avgSum += currency;
				totalOk++;
			} else {
				Quad q = new Quad(null, ModelFactory.createDefaultModel().createResource(key).asNode(), QPRO.exceptionDescription.asNode(), DQM.MissingMetadataForCurrency.asNode());
				this._problemList.add(q);
			}
		}
		
		return avgSum / totalOk;
	}

	@Override
	public Resource getMetricURI() {
		return DQM.CurrencyOfDatasetMetric;
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
		return null;
	}
}
