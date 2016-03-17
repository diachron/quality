/**
 * 
 */
package eu.diachron.qualitymetrics.representational.representationalconciseness;

import java.util.Set;
import java.util.UUID;

import org.mapdb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.semantics.DQMPROB;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.utilities.SerialisableQuad;

/**
 * @author Jeremy Debattista
 * 
 * This metric detects the use of standard RDF Prolix Features.
 * These features are Collections (rdf:Alt, rdf:Bag, rdf:List, rdf:Seq), Containers and Reification (rdf:Statement).
 * 
 * The value returns a ratio of the total number of prolix (RCC) triples against the total number of triples
 */
public class NoProlixRDF implements QualityMetric {

	private double totalTriples = 0.0;
	
	private double totalRCC = 0.0;
	private static DB mapDb = MapDbFactory.getMapDBAsyncTempFile();
	private Set<SerialisableQuad> _problemList = MapDbFactory.createHashSet(mapDb, UUID.randomUUID().toString());
	private static Logger logger = LoggerFactory.getLogger(NoProlixRDF.class);

	@Override
	public void compute(Quad quad) {
		logger.debug("Assessing quad: " + quad.asTriple().toString());

		Node predicate = quad.getPredicate();
		Node object = quad.getObject();
		Node subject = quad.getSubject();
		
		totalTriples++;
		
		if (predicate.hasURI(RDF.type.getURI())){
			if (object.hasURI(RDF.Statement.getURI())){
				Quad q = new Quad(null, subject, QPRO.exceptionDescription.asNode(), DQMPROB.UsageOfReification.asNode());
				this._problemList.add(new SerialisableQuad(q));
				totalRCC++;
			} else if ((object.hasURI(RDFS.Container.getURI())) || object.hasURI(RDFS.ContainerMembershipProperty.getURI())) {
				Quad q = new Quad(null, subject, QPRO.exceptionDescription.asNode(), DQMPROB.UsageOfContainers.asNode());
				this._problemList.add(new SerialisableQuad(q));
				totalRCC++;
			} else if ( (object.hasURI(RDF.Alt.getURI())) || (object.hasURI(RDF.Bag.getURI())) || (object.hasURI(RDF.List.getURI())) || (object.hasURI(RDF.Seq.getURI()))){
				Quad q = new Quad(null, subject, QPRO.exceptionDescription.asNode(), DQMPROB.UsageOfCollections.asNode());
				this._problemList.add(new SerialisableQuad(q));
				totalRCC++;
			}
		} else {
			this.isRCCpredicate(subject, predicate);
		}
		

	}

	private void isRCCpredicate(Node subject, Node predicate){
		if ((predicate.hasURI(RDF.subject.getURI())) || (predicate.hasURI(RDF.predicate.getURI())) || (predicate.hasURI(RDF.object.getURI()))){
			Quad q = new Quad(null, subject, QPRO.exceptionDescription.asNode(), DQMPROB.UsageOfReification.asNode());
			this._problemList.add(new SerialisableQuad(q));
			totalRCC++;
		}
		if (predicate.hasURI(RDFS.member.getURI())){
			Quad q = new Quad(null, subject, QPRO.exceptionDescription.asNode(), DQMPROB.UsageOfContainers.asNode());
			this._problemList.add(new SerialisableQuad(q));
			totalRCC++;
		}
		if ((predicate.hasURI(RDF.first.getURI())) || (predicate.hasURI(RDF.rest.getURI())) || (predicate.hasURI(RDF.nil.getURI()))){
			Quad q = new Quad(null, subject, QPRO.exceptionDescription.asNode(), DQMPROB.UsageOfCollections.asNode());
			this._problemList.add(new SerialisableQuad(q));
			totalRCC++;
		}
		// for rdf:_n where n is a number
		if (predicate.getURI().matches(RDF.getURI()+"_[0-9]+")){
			Quad q = new Quad(null, subject, QPRO.exceptionDescription.asNode(), DQMPROB.UsageOfContainers.asNode());
			this._problemList.add(new SerialisableQuad(q));
			totalRCC++;
		}
	}
	
	
	
	

	@Override
	public double metricValue() {
		statsLogger.info("No Prolix RDF. Dataset: {} - Total RCC {}, Total Triples {}", 
				EnvironmentProperties.getInstance().getDatasetURI(), this.totalRCC, this.totalTriples );


		return (this.totalRCC == 0) ? 1.0 : 1.0 - (this.totalRCC / this.totalTriples);
	}

	@Override
	public Resource getMetricURI() {
		return DQM.NoProlixRDFMetric;
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
