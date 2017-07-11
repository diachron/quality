/**
 * 
 */
package eu.diachron.qualitymetrics.contextual.understandability;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.mapdb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.semantics.DQMPROB;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ReservoirSampler;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.utilities.AbstractQualityMetric;

/**
 * @author Jeremy Debattista
 * 
 * This measures the percentage of entities having an rdfs:label or rdfs:comment
 */
public class HumanReadableLabelling extends AbstractQualityMetric{
	private final Resource METRIC_URI = DQM.HumanReadableLabellingMetric;
	
	final static Logger logger = LoggerFactory.getLogger(HumanReadableLabelling.class);


	private static DB mapDb = MapDbFactory.getMapDBAsyncTempFile();

	private Set<String> entitiesWO = MapDbFactory.createHashSet(mapDb, UUID.randomUUID().toString());
	private Set<String> entitiesWith = MapDbFactory.createHashSet(mapDb, UUID.randomUUID().toString());
	private Set<String> entitiesUnknown = MapDbFactory.createHashSet(mapDb, UUID.randomUUID().toString()); // have human readable label/description but don't know if it is 
																										   // a described entity in the dataset - ie. with a type definition
	
	private long entitiesWOSize = 0l;
	private long entitiesWithSize = 0l;

	
	private ReservoirSampler<Quad> problemSampler = new ReservoirSampler<Quad>(100000, false);
	
	private double value = 0.0d;
	
	private Set<String> labelProperties = new HashSet<String>();
	{
		labelProperties.add(RDFS.label.getURI());
		labelProperties.add(RDFS.comment.getURI());
		labelProperties.add(DC.title.getURI());
		labelProperties.add(DC.description.getURI());
		labelProperties.add(DCTerms.title.getURI());
		labelProperties.add(DCTerms.alternative.getURI());
		labelProperties.add(DCTerms.description.getURI());
		labelProperties.add("http://www.w3.org/2004/02/skos/core#altLabel");
		labelProperties.add("http://www.w3.org/2004/02/skos/core#prefLabel");
		labelProperties.add("http://www.w3.org/2004/02/skos/core#note");
		labelProperties.add("http://www.w3.org/2007/05/powder-s#text");
		labelProperties.add("http://www.w3.org/2008/05/skos-xl#altLabel");
		labelProperties.add("http://www.w3.org/2008/05/skos-xl#hiddenLabel");
		labelProperties.add("http://www.w3.org/2008/05/skos-xl#prefLabel");
		labelProperties.add("http://www.w3.org/2008/05/skos-xl#literalForm");
		labelProperties.add("http://schema.org/name");
		labelProperties.add("http://schema.org/description");
		labelProperties.add("http://schema.org/alternateName");
	}
	
	/**
	 * Each entity is checked for a Human Readable label.
	 * In this metric we are assuming that each entity has exactly 1 comment and/or label,
	 * thus we are not checking for contradicting labeling or commenting of entities.
	 */
	public void compute(Quad quad) {
		logger.debug("Computing : {} ", quad.asTriple().toString());
		
		if (quad.getSubject().isURI() && quad.getPredicate().getURI().equals(RDF.type.getURI())){
			String entity = quad.getSubject().getURI();
			if (!entityInASet(entity)) {
				entitiesWO.add(entity);
				entitiesWOSize++;
			}
			else {
				if (entitiesUnknown.contains(entity)){
					entitiesWith.add(entity);
					entitiesUnknown.remove(entity);
					entitiesWithSize++;
				}
			}
		}
	
		if (quad.getSubject().isURI() && (labelProperties.contains(quad.getPredicate().getURI()))){
			String entity = quad.getSubject().getURI();
			if (entitiesWO.contains(entity)){
				entitiesWith.add(entity);
				entitiesWO.remove(entity);
				entitiesWithSize++;
				entitiesWOSize--;
			} else {
				entitiesUnknown.add(entity);
			}
			
		}
	}
	
	private boolean entityInASet(String entity){
		return ( entitiesWO.contains(entity) || 
				entitiesWith.contains(entity) ||
				entitiesUnknown.contains(entity) );
	}
	
	public double metricValue() {
		double entities = (entitiesWOSize + entitiesWithSize);
		double humanLabels = entitiesWithSize;
			
		value = (humanLabels/entities); 	
		statsLogger.info("Dataset: {} - Total # Human Readable Labels : {}; # Entities : {};", this.getDatasetURI(), humanLabels, entities); 

		return value;
	}

	public Resource getMetricURI() {
		return this.METRIC_URI;
	}
	
	@SuppressWarnings("unused")
	private void createProblemQuads(){
		for (String entity : entitiesWO){
			Quad q = new Quad(null, ModelFactory.createDefaultModel().createResource(entity).asNode(), QPRO.exceptionDescription.asNode(), DQMPROB.NoHumanReadableLabel.asNode());
			Boolean isAdded = this.problemSampler.add(q);
			if (!isAdded) q = null;
		}
	}
	
	public ProblemList<?> getQualityProblems() {
//		createProblemQuads();
//		ProblemList<Quad> pl = null;
//		try {
//			if(this.problemSampler != null && this.problemSampler.size() > 0) {
//				pl = new ProblemList<Quad>(this.problemSampler.getItems());
//			}
//			else {
//				pl = new ProblemList<Quad>();
//			}
//		} catch (ProblemListInitialisationException e) {
//			logger.error(e.getMessage());
//		}
//		return pl;
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
