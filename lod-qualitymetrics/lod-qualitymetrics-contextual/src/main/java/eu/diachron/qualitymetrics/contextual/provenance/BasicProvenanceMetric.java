/**
 * 
 */
package eu.diachron.qualitymetrics.contextual.provenance;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.semantics.knownvocabs.DCAT;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.VOID;

/**
 * @author Jeremy Debattista
 * 
 * This metric measures if a dataset have the most basic
 * provenance information, that is, information about the creator 
 * or publisher of the dataset. Each dataset (voID, dcat) should 
 * have either a dc:creator or dc:publisher as a minimum requirement. 
 * 
 */
public class BasicProvenanceMetric implements QualityMetric {
	
	protected ConcurrentHashMap<String, String> dataset = new ConcurrentHashMap<String,String>();
	protected Set<String> isMetadata = new HashSet<String>();

	private static Logger logger = LoggerFactory.getLogger(BasicProvenanceMetric.class);

	@Override
	public void compute(Quad quad) {
		logger.debug("Assessing quad: " + quad.asTriple().toString());

		
		Node subject = quad.getSubject();
		Node predicate = quad.getPredicate();
		Node object = quad.getObject();
		
		if (predicate.hasURI(RDF.type.getURI()) && (object.hasURI(VOID.Dataset.getURI()) || object.hasURI(DCAT.Dataset.getURI()))){
			if (subject.isURI()){
				dataset.put(subject.getURI(), "");
				isMetadata.add(subject.getURI());
			}
			else {
				dataset.put(subject.toString(), "");
				isMetadata.add(subject.toString());
			}
		}
		
		//are there more basic provenance requirements? see flemmings thesis pg 154
		if (predicate.hasURI(DCTerms.creator.getURI()) || predicate.hasURI(DCTerms.publisher.getURI())){
			if (object.isURI()){
				if (subject.isURI()) {
					dataset.put(subject.getURI(), object.getURI());
				} else {
					dataset.put(subject.toString(), object.getURI());
				}
			}
		}
	}

	@Override
	public double metricValue() {
		double validProv = 0.0;
		for (String s : dataset.values()) 
			if (!(s.equals(""))) validProv++;
		
		statsLogger.info("Basic Provenance Metric. Dataset: {} - Valid Provenance {}, Dataset Size {}", 
				EnvironmentProperties.getInstance().getDatasetURI(), validProv, dataset.size());

		return (dataset.size() == 0) ? 0.0 : (validProv / (double)dataset.size());
	}

	@Override
	public Resource getMetricURI() {
		return DQM.BasicProvenanceMetric;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		// TODO Auto-generated method stub
		return new ProblemList<Quad>();
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
