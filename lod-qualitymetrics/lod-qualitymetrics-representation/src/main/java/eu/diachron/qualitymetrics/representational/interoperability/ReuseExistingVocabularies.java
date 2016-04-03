/**
 * 
 */
package eu.diachron.qualitymetrics.representational.interoperability;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.mapdb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.semantics.DQMPROB;
import de.unibonn.iai.eis.luzzu.assessment.ComplexQualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.AfterException;
import de.unibonn.iai.eis.luzzu.exceptions.BeforeException;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.utilities.LOVInterface;
import eu.diachron.qualitymetrics.utilities.SerialisableQuad;
import eu.diachron.qualitymetrics.utilities.VocabularyLoader;

/**
 * @author Jeremy Debattista
 * 
 * This metric calculates the number of unique vocabularies
 * used in a dataset, but suggested to be useful in the
 * dataset's domain. This metric is similar to the
 * Reuse Existing Terms metric with the difference that
 * we do not look at overlapping terms. In this metric,
 * we also check if the term used is defined or not before
 * adding to the seen-suggested set.
 * 
 * The value of this metric is calculated as follows:
 * 	# of used ns from suggested list / total # of suggested vocabularies  
 * 
 * Config File Example:
 * For example:
 * <http://linkedgeodata.org> :hasDomain "Geographic" ;
 *  :hasVocabularies <http://www.w3.org/2003/01/geo/wgs84_pos#> , <http://www.geonames.org/ontology#> ;
 *  :getFromLOV "True" 
 */
public class ReuseExistingVocabularies implements ComplexQualityMetric {

	private Model categories = ModelFactory.createDefaultModel();

	private Set<String> suggestedVocabs = new HashSet<String>();
	private Set<String> seenSuggested = new HashSet<String>();
	
//	private SharedResources shared = SharedResources.getInstance();
	private static DB mapDb = MapDbFactory.getMapDBAsyncTempFile();

	private Set<String> seenSet = MapDbFactory.createHashSet(mapDb, UUID.randomUUID().toString());
	private Set<SerialisableQuad> _problemList = MapDbFactory.createHashSet(mapDb, UUID.randomUUID().toString());
	
	private static Logger logger = LoggerFactory.getLogger(ReuseExistingVocabularies.class);

	@Override
	public void compute(Quad quad) {
		Node predicate = quad.getPredicate();
		
		if (predicate.hasURI(RDF.type.toString())){
			//it is a class
			Node object = quad.getObject();

			if (!(object.isBlank())){
				logger.info("checking class: {}", object.getURI());

				if (!(this.seenSet.contains(object.getURI()))){
					if (suggestedVocabs.contains(object.getNameSpace())){
//						Boolean seen = shared.classOrPropertyDefined(object.getURI());
//						Boolean defined = null;
//						if (seen == null) {
//							defined = VocabularyLoader.getInstance().checkTerm(object);
//							shared.addClassOrProperty(object.getURI(), defined);
//						}
//						else defined = seen;
						Boolean defined = VocabularyLoader.getInstance().checkTerm(object);
						
						if (defined){
							seenSuggested.add(object.getNameSpace());
						}
						this.seenSet.add(object.getURI());
					}
				}
			}
		}
		
		if (!(this.seenSet.contains(predicate.getURI()))){
			if (suggestedVocabs.contains(predicate.getNameSpace())){
				Boolean defined = VocabularyLoader.getInstance().checkTerm(predicate);				
				if (defined){
					seenSuggested.add(predicate.getNameSpace());
				}
			}
			this.seenSet.add(predicate.getURI());
		}
	}

	@Override
	public double metricValue() {
		double reusedvoc = ((double)this.seenSuggested.size()) / ((double) this.suggestedVocabs.size());
		
		statsLogger.info("Reuse Existing Vocabs. Dataset: {} - Total # Seen Suggested : {}; # Suggested Vocab : {};", 
				EnvironmentProperties.getInstance().getDatasetURI(), this.seenSuggested.size(), this.suggestedVocabs.size());

		for(String s : suggestedVocabs){
			if (!(seenSuggested.contains(s))){
				Quad q = new Quad(null, ModelFactory.createDefaultModel().createResource(s).asNode(), QPRO.exceptionDescription.asNode(), DQMPROB.UnusedSuggestedVocabulary.asNode());
				this._problemList.add(new SerialisableQuad(q));
			}
		}
		
		return reusedvoc;
	}

	@Override
	public Resource getMetricURI() {
		return DQM.ReuseExistingVocabularyMetric;
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

	/** 
	 * The before method in this method should accept only
	 * a string with the trig filename 
	 */
	@Override
	public void before(Object... args) throws BeforeException {
		if (args == null || args.length <= 0 || args[0] == null){
			logger.error("Argument in the Reuse Existing Vocabulary metric should not be null.");
			throw new BeforeException("Argument should not be a string with a filename.");
		}
		String fileName = (String) args[0];
		
		try{
			categories = RDFDataMgr.loadModel(fileName);
		} catch (RiotException e){
			logger.error("Error loading configuration file: {}", fileName);
			throw new BeforeException("Invalid configuration file passed to the Reuse Existing Vocabulary metric. The configuration file should be RDF " + 
					"(or any other RDF serialisation format), having this following format: <http://linkedgeodata.org> :hasDomain \"geographic\"^^xsd:string ; " + 
					" :hasVocabularies <http://www.w3.org/2003/01/geo/wgs84_pos#> , <http://www.geonames.org/ontology#> .  ");
		}
		
		//Get dataset/base URI 
		String baseURI = EnvironmentProperties.getInstance().getBaseURI();
		if (baseURI == null){
			//Try using the dataset URI
			baseURI = EnvironmentProperties.getInstance().getBaseURI();
			if (baseURI == null){
				logger.error("Unknown Dataset URI");
				throw new BeforeException("Dataset URI is not known. This should be set in Luzzu.");
			}
		}
		
		//Load Vocabularies
		logger.info("Getting vocabularies from user configuration file for {}",baseURI);
		List<RDFNode> lst = categories.listObjectsOfProperty(categories.createResource(baseURI), categories.createProperty(":hasVocabularies")).toList();
		logger.info("Found {} vocabularies", lst.size());
		for(RDFNode n : lst){
			logger.info("Loading {}", n.toString());
			VocabularyLoader.getInstance().loadVocabulary(n.toString());
			suggestedVocabs.add(n.toString());
		}
		
		List<RDFNode> lov = categories.listObjectsOfProperty(categories.createResource(baseURI), categories.createProperty(":getFromLOV")).toList();
		boolean getFromLov = false;
		if (lov.size() > 0) getFromLov = lov.get(0).asLiteral().getBoolean();
		
		if (getFromLov){
			List<RDFNode> domainNode = categories.listObjectsOfProperty(categories.createResource(baseURI), categories.createProperty(":hasDomain")).toList();
			for(RDFNode n : domainNode){
				try {
					List<String> vocabs = LOVInterface.getKnownVocabsPerDomain(n.asLiteral().getString());
					for(String v : vocabs) suggestedVocabs.add(v);
				} catch (IOException e) {
					logger.error("Could not load vocabularies from LOD. Error Message: {}", e.getMessage());
				}
			}
		}
	}

	@Override
	public void after(Object... args) throws AfterException {
		// nothing to do
	}
	
}
