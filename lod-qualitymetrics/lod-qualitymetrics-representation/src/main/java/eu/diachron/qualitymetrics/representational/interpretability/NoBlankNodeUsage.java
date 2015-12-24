/**
 * 
 */
package eu.diachron.qualitymetrics.representational.interpretability;

import java.util.Set;
import java.util.UUID;

import org.mapdb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import eu.diachron.qualitymetrics.utilities.SerialisableQuad;

/**
 * @author Jeremy Debattista
 * 
 * This metric calculates the number of blank nodes found in the subject or the object
 * of a triple. The metric value returns a value [0-1] where a higher number of blank nodes
 * will result in a value closer to 0.
 */
public class NoBlankNodeUsage implements QualityMetric {

	private static Logger logger = LoggerFactory.getLogger(NoBlankNodeUsage.class);
	
	//we will store all data level constraints that are URIs
	private static DB mapDb = MapDbFactory.getMapDBAsyncTempFile();
	private Set<String> uniqueDLC = MapDbFactory.createHashSet(mapDb, UUID.randomUUID().toString());
	private Set<String> uniqueBN = MapDbFactory.createHashSet(mapDb, UUID.randomUUID().toString());
	private Set<SerialisableQuad> _problemList = MapDbFactory.createHashSet(mapDb, UUID.randomUUID().toString());
	
	
	@Override
	public void compute(Quad quad) {
		Node predicate = quad.getPredicate();
		Node subject = quad.getSubject();
		Node object = quad.getObject();

		logger.debug("Assessing quad: " + quad.asTriple().toString());
		
		// we will skip all "typed" triples
		if (!(predicate.hasURI(RDF.type.getURI()))){
			if (subject.isBlank()) {
				uniqueBN.add(subject.getBlankNodeLabel());
				_problemList.add(new SerialisableQuad(quad));
			}
			else uniqueDLC.add(subject.getURI());
			
			if (!(object.isLiteral())){
				if (object.isBlank()){
					uniqueBN.add(object.getBlankNodeLabel());
					_problemList.add(new SerialisableQuad(quad));
				}
				else uniqueDLC.add(object.getURI());
			}
		}
	}

	@Override
	public double metricValue() {
		statsLogger.info("No Blank Node Usage. Dataset: {} - Unique DLC {}, Unique BN {}", 
				EnvironmentProperties.getInstance().getDatasetURI(), uniqueDLC.size(), uniqueBN.size() );

		return ((double) uniqueDLC.size()) / ((double) uniqueDLC.size() + (double) uniqueBN.size());
	}

	@Override
	public Resource getMetricURI() {
		return DQM.NoBlankNodeMetric;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		ProblemList<SerialisableQuad> pl = null;
		try {
			if(this._problemList != null && this._problemList.size() > 0) {
				pl = new ProblemList<SerialisableQuad>(this._problemList);	
			} else {
				pl = new ProblemList<SerialisableQuad>();
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
		return 	DQM.LuzzuProvenanceAgent;
	}
}
