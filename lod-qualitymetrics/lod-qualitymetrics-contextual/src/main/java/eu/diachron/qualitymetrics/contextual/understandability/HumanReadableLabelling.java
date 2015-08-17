/**
 * 
 */
package eu.diachron.qualitymetrics.contextual.understandability;

import java.util.ArrayList;
import java.util.List;

import org.mapdb.HTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;

/**
 * @author Jeremy Debattista
 * 
 * This measures the percentage of entities having an rdfs:label or rdfs:comment
 */
public class HumanReadableLabelling implements QualityMetric{
	private final Resource METRIC_URI = DQM.HumanReadableLabellingMetric;
	
	final static Logger logger = LoggerFactory.getLogger(HumanReadableLabelling.class);

	private boolean computed = false;
			
	private HTreeMap<String, Integer> pEntities = MapDbFactory.createFilesystemDB().createHashMap("labelled-entities").make();
	
	private List<Quad> _problemList = new ArrayList<Quad>();
	
	private double value = 0.0d;
	
	
	/**
	 * Each entity is checked for a Human Readable label <rdfs:label>.
	 * In this metric we are assuming that each entity has exactly 1 comment and/or label,
	 * thus we are not checking for contradicting labeling or commenting of entities.
	 */
	public void compute(Quad quad) {
		logger.debug("Computing : {} ", quad.asTriple().toString());

		
		if (quad.getSubject().isURI() && quad.getPredicate().getURI().equals(RDF.type.getURI())){
			// we've got an instance!
			if (!(pEntities.containsKey(quad.getSubject().getURI()))) { // an instance might have more than 1 type defined
				pEntities.put(quad.getSubject().getURI(), 0);
			}
		}
	
		if (quad.getSubject().isURI() && (quad.getPredicate().getURI().equals(RDFS.label.getURI()))){
			// we'll check if the provider is cheating and is publishing empty string labels and comments
			if (!(quad.getObject().getLiteralValue().equals(""))) pEntities.put(quad.getSubject().getURI(), 1);
		}
	}

	
	public double metricValue() {
		if (!computed){
			computed = true;
			
			double entities = 0.0;
			double humanLabels = 0.0;
			
			for (String n : this.pEntities.keySet()){
				entities+=1;
				humanLabels += this.pEntities.get(n);
				if (this.pEntities.get(n) == 0) this.createProblemQuad(n);
			}

			value = humanLabels/entities; // at most we should have 1 label for each entity
			logger.info("Dataset: {} - Total # Human Readable Labels : {}; # Violated Entities : {};"
					, EnvironmentProperties.getInstance().getDatasetURI(), humanLabels, entities); //TODO: these store in a seperate file

		}

		return value;
	}

	
	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	
	private void createProblemQuad(String resource){
		Quad q = new Quad(null, ModelFactory.createDefaultModel().createResource(resource).asNode(), QPRO.exceptionDescription.asNode(), DQM.NoHumanReadableLabel.asNode());
		this._problemList.add(q);
	}
	
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
