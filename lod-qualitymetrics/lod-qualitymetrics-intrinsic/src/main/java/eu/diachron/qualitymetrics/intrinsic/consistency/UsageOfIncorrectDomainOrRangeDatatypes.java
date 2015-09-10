/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.consistency;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mapdb.HTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import de.unibonn.iai.eis.luzzu.semantics.utilities.Commons;
import eu.diachron.qualitymetrics.utilities.SerialisableTriple;
import eu.diachron.qualitymetrics.utilities.VocabularyLoader;

/**
 * @author Jeremy Debattista
 * 
 * This metric tests if a property's domain and range
 * are of the same type as declared in the corresponding
 * schema.
 * 
 */
public class UsageOfIncorrectDomainOrRangeDatatypes implements QualityMetric {

	private final Resource METRIC_URI = DQM.UsageOfIncorrectDomainOrRangeDatatypesMetric;

	private static Logger logger = LoggerFactory.getLogger(UsageOfIncorrectDomainOrRangeDatatypes.class);
	
    private List<Model> problemList = new ArrayList<Model>();
	
	private HTreeMap<String, List<String>> mapResourceType = MapDbFactory.createAsyncFilesystemDB().createHashMap("resource-type").make();
	
	//If the boolean is true, then we need to check the domain, else check the range
	private Set<SerialisableTriple> unknownTypesDomain = MapDbFactory.createAsyncFilesystemDB().createHashSet("unknown-types-domain").make();
	private Set<SerialisableTriple> unknownTypesRange = MapDbFactory.createAsyncFilesystemDB().createHashSet("unknown-types-range").make();

	
	private long totalPredicates = 0;
	private long incorrectDomain = 0;
	private long incorrectRange = 0;
	private long undereferenceablePredicates = 0;

	@Override
	public void compute(Quad quad) {
		
		logger.debug("Computing : {} ", quad.asTriple().toString());
		
		Resource subject = Commons.asRDFNode(quad.getSubject()).asResource();
		Resource predicate = Commons.asRDFNode(quad.getPredicate()).asResource();
		RDFNode object = Commons.asRDFNode(quad.getObject());
		
		if (predicate.equals(RDF.type)) {
			String s = subject.getURI();
			String o = object.asResource().getURI();
			List<String> types = new ArrayList<String>();
			types.add(o);
			
			Set<RDFNode> infer = VocabularyLoader.inferParent(object.asNode(), null, true);
			for(RDFNode nd : infer) types.add(nd.asResource().getURI());
			
			mapResourceType.put(s,types);
		}
		else {
			if (VocabularyLoader.checkTerm(predicate.asNode())){
				totalPredicates++; // we will only check those properties that get dereferenced
				
				// Domain Checker
				Set<RDFNode> domains = VocabularyLoader.getPropertyDomain(predicate.asNode());
				if (mapResourceType.containsKey(subject)){
					Set<RDFNode> types = this.toRDFNodeSet(mapResourceType.get(subject.getURI()));

					if(!(domains.removeAll(types))){
						addToProblem(quad,'d');
						incorrectDomain++;
					}
				} else {
					//type is unknown try again later
					unknownTypesDomain.add(new SerialisableTriple(quad.asTriple()));
				}
				// Range Checker
				Set<RDFNode> range = VocabularyLoader.getPropertyRange(predicate.asNode());
				if (object instanceof Literal){
					RDFDatatype _objDT = ((Literal) object).getDatatype();
					if (_objDT == null){
						addToProblem(quad,'r');
						incorrectRange++;
					} else {
						Resource litRes = ModelFactory.createDefaultModel().createResource(_objDT.getURI());
						if (!range.contains(litRes)){
							addToProblem(quad,'r');
							incorrectRange++;
						}
					}
				} else {
					//object is an instance of some URI
					if (mapResourceType.containsKey(object.asResource())){
						Set<RDFNode> types = this.toRDFNodeSet(mapResourceType.get(object.asResource().getURI()));

						if (!range.removeAll(types)){
							addToProblem(quad,'r');
							incorrectRange++;
						}
					} else {
						//type is unknown try again later
						unknownTypesRange.add(new SerialisableTriple(quad.asTriple()));
					}
				}
			} else {
				logger.debug("Predicate {} not dereferenced.", predicate.toString());
				undereferenceablePredicates++;
				//problem unknown but do not reduce quality here
				//maybe report some statistics here instead of problem
			}
		}
	}
	
    private void addToProblem(Quad q, char type){
    	Model m = ModelFactory.createDefaultModel();
    	
    	Resource gen = Commons.generateURI();
    	if (type == 'd')
    		m.createStatement(gen, RDF.type, DQM.IncorrectDomain);
    	if (type == 'r')
    		m.createStatement(gen, RDF.type, DQM.IncorrectRange);
    	if (type == 'u')
    		m.createStatement(gen, RDF.type, DQM.IncorrectRange);

    	
    	Resource anon = m.createResource(AnonId.create());
    	m.createStatement(gen, DQM.problematicTriple, anon);
    	m.createStatement(anon, RDF.subject, Commons.asRDFNode(q.getSubject()));
    	m.createStatement(anon, RDF.predicate, Commons.asRDFNode(q.getPredicate()));
    	m.createStatement(anon, RDF.object, Commons.asRDFNode(q.getObject()));
    	
    	this.problemList.add(m);

    }
	
	@Override
	public double metricValue() {

		logger.debug("Total number of unknown domain types : {}; Total number of unknown range types: {}", unknownTypesDomain.size(), unknownTypesRange.size() );

		for(SerialisableTriple trip : unknownTypesDomain){
			Triple t = trip.getTriple();
			Set<RDFNode> domains = VocabularyLoader.getPropertyDomain(t.getPredicate());
			if (mapResourceType.containsKey(t.getSubject().getURI())){
				Set<RDFNode> types = this.toRDFNodeSet(mapResourceType.get(t.getSubject().getURI()));

				if(!(domains.removeAll(types))){
					addToProblem(new Quad(null, t),'d');
					incorrectDomain++;
				}
			} else {
				addToProblem(new Quad(null, t),'u');
				incorrectDomain++;
			}
		}
		
		for(SerialisableTriple trip : unknownTypesRange){
			Triple t = trip.getTriple();
			Set<RDFNode> range = VocabularyLoader.getPropertyRange(t.getPredicate());
			if (mapResourceType.containsKey(t.getObject().getURI())){
				Set<RDFNode> types = this.toRDFNodeSet(mapResourceType.get(t.getObject().getURI()));

				if (!range.removeAll(types)){
					addToProblem(new Quad(null, t),'r');
					incorrectRange++;
				}
			} else {
				addToProblem(new Quad(null, t),'u');
				incorrectRange++;
			}
		}
			
			
		if (unknownTypesRange.size() > 0) unknownTypesRange.clear();
		if (unknownTypesDomain.size() > 0) unknownTypesDomain.clear();

		
		logger.info("Dataset: {} - # Incorrect Domains : {}; # Incorrect Ranges : {}; # Predicates Assessed : {}; # Undereferenceable Predicates : {} "
				, EnvironmentProperties.getInstance().getDatasetURI(), incorrectDomain, incorrectRange, totalPredicates, undereferenceablePredicates); //TODO: these store in a seperate file

		double value = 1 - ((double) incorrectDomain + (double) incorrectRange + (double) undereferenceablePredicates) / (((double) totalPredicates * 2) + (double) undereferenceablePredicates);
		
		return value;
	}

	@Override
	public Resource getMetricURI() {
		return METRIC_URI;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		ProblemList<Model> tmpProblemList = null;
		try {
			if(this.problemList != null && this.problemList.size() > 0) {
				tmpProblemList = new ProblemList<Model>(this.problemList);
			} else {
				tmpProblemList = new ProblemList<Model>();
			}		} catch (ProblemListInitialisationException problemListInitialisationException) {
			logger.error(problemListInitialisationException.getMessage());
		}
		return tmpProblemList;
	}

	@Override
	public boolean isEstimate() {
		return false;
	}

	@Override
	public Resource getAgentURI() {
		return DQM.LuzzuProvenanceAgent;
	}
	
	private Resource toResource(String uri){
		return ModelFactory.createDefaultModel().createResource(uri);
	}
	
	private Set<RDFNode> toRDFNodeSet(List<String> objects){
		Set<RDFNode> lst = new HashSet<RDFNode>();
		for(String o : objects){
			lst.add(this.toResource(o));
		}
		return lst;
	}
	
}
