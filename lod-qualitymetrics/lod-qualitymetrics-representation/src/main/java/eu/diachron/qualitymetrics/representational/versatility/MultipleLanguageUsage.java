/**
 * 
 */
package eu.diachron.qualitymetrics.representational.versatility;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import eu.diachron.qualitymetrics.utilities.AbstractQualityMetric;

/**
 * @author Jeremy Debattista
 * 
 * In this metric we check if data (in this case literals) is
 * available in different languages, i.e. a dataset supports 
 * multilinguality. In this metric, we will check ALL literals
 * having a language tag. Those without a language tag will be
 * ignored.
 * 
 * The value of this metric is the average number of languages 
 * used throughout the dataset (per resource).
 * Therefore if we have the following:
 * R1 : [en,mt]
 * R2 : [en,mt]
 * R3 : [en]
 * R4 : [en]
 * R5 : [en]
 * the value would be 1 (7/5 = 1.4 ~ 1)
 * 
 * The value returns the number of multiple languages used
 */
public class MultipleLanguageUsage extends AbstractQualityMetric {
	
	private static Logger logger = LoggerFactory.getLogger(MultipleLanguageUsage.class);
	
//	static final String DEFAULT_TAG = "en";
	
//	private static DB mapDb = MapDbFactory.getMapDBAsyncTempFile();

	private ConcurrentHashMap<String, Set<String>> multipleLanguage = new ConcurrentHashMap<String, Set<String>>();
	
//	private Set<Quad> _problemList = new HashSet<Quad>();

	@Override
	public void compute(Quad quad) {
		logger.debug("Assessing {}",quad.asTriple().toString());
		Node object = quad.getObject();
		
		if (object.isLiteral()){
			String subject = quad.getSubject().toString();
			String lang = object.getLiteralLanguage();
			if (!(lang.equals(""))){
				Set<String> langList = new HashSet<String>();
				if (this.multipleLanguage.containsKey(subject)) langList = this.multipleLanguage.get(subject);
				langList.add(lang);
				this.multipleLanguage.put(subject,langList);
			}
		}
	}

	@Override
	public double metricValue() {
		double totLang = 0.0;
		for(Set<String> lst : this.multipleLanguage.values()) totLang += (double) lst.size();
		
		statsLogger.info("Multiple Language Usage. Dataset: {} - Total Languages {}, Multiple Languages Found {}", 
				EnvironmentProperties.getInstance().getDatasetURI(),  totLang, this.multipleLanguage.size()  );

		double val = totLang / (double) this.multipleLanguage.size();
		
		return (Math.round(val) == 0) ? 1 : Math.round(val) ;
	}

	@Override
	public Resource getMetricURI() {
		return DQM.MultipleLanguageUsageMetric;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
//		ProblemList<SerialisableQuad> pl = null;
//		try {
//			if(this._problemList != null && this._problemList.size() > 0) {
//				pl = new ProblemList<SerialisableQuad>(this._problemList);
//			} else {
//				pl = new ProblemList<SerialisableQuad>();
//			}
//		} catch (ProblemListInitialisationException e) {
//			logger.error(e.getMessage());
//		}
		return new ProblemList<Quad>();
	}

	@Override
	public boolean isEstimate() {
		return false;
	}

	@Override
	public Resource getAgentURI() {
		return 	DQM.LuzzuProvenanceAgent;
	}

}
