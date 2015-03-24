/**
 * 
 */
package eu.diachron.qualitymetrics.representational.versatility;

import java.util.ArrayList;
import java.util.List;

import org.mapdb.HTreeMap;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;

/**
 * @author Jeremy Debattista
 * 
 * In this metric we check if data (in this case literals) is
 * available in different languages, i.e. a dataset supports 
 * multilinguality. In this metric, we will check ALL resources
 * whose properties define a String literal.
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
public class MultipleLanguageUsage implements QualityMetric {
	
	static final String DEFAULT_TAG = "en";
	
	private HTreeMap<String, List<String>> multipleLanguage = MapDbFactory.createFilesystemDB().createHashMap("multi-lingual-map").make();
	

	@Override
	public void compute(Quad quad) {
		Node object = quad.getObject();
		
		if (object.isLiteral()){
			String subject = quad.getSubject().getURI();
			if (object.getLiteralDatatypeURI().equals("http://www.w3.org/2001/XMLSchema#string")){
				String lang = (object.getLiteralLanguage().equals("")) ? DEFAULT_TAG : object.getLiteralLanguage();
				List<String> langList = new ArrayList<String>();
				if (this.multipleLanguage.containsKey(subject)) langList = this.multipleLanguage.get(subject);
				langList.add(lang);
				this.multipleLanguage.put(subject,langList);
			}
		}
	}

	@Override
	public double metricValue() {
		double totLang = 0.0;
		for(List<String> lst : this.multipleLanguage.values()) totLang += (double) lst.size();
		
		double val = totLang / (double) this.multipleLanguage.size();
		
		return Math.round(val);
	}

	@Override
	public Resource getMetricURI() {
		return DQM.MultipleLanguageUsageMetric;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		return null;
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
