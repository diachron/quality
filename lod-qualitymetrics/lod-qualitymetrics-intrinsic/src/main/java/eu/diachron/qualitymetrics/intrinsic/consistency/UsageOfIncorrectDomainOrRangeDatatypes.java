/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.consistency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.mapdb.HTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

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
	
	private HTreeMap<String, List<String>> mapResourceType = MapDbFactory.createFilesystemDB().createHashMap("resource-type").make();
	
	//If the boolean is true, then we need to check the domain, else check the range
	private Set<SerialisableTriple> unknownTypesDomain = MapDbFactory.createFilesystemDB().createHashSet("unknown-types-domain").make();
	private Set<SerialisableTriple> unknownTypesRange = MapDbFactory.createFilesystemDB().createHashSet("unknown-types-range").make();

	private HTreeMap<String, Set<SerialisableTriple>> unknownTypesDomainMap = MapDbFactory.createFilesystemDB().createHashMap("unknown-types-domain-map").make();
	private HTreeMap<String, Set<SerialisableTriple>> unknownTypesRangeMap = MapDbFactory.createFilesystemDB().createHashMap("unknown-types-range-map").make();

	
	
	
	private long totalPredicates = 0;
	private long incorrectDomain = 0;
	private long incorrectRange = 0;
	private long undereferenceablePredicates = 0;
	private long unknownDomainAndRange = 0;

	private ModelCom mc = new ModelCom(Graph.emptyGraph);
	
	private Map<Node, Set<RDFNode>> ranges = new HashMap<Node, Set<RDFNode>>();
	private Map<Node, Set<RDFNode>> domains = new HashMap<Node, Set<RDFNode>>();
	
	
	@Override
	public void compute(Quad quad) {
		
		logger.debug("Computing : {} ", quad.asTriple().toString());
		
		Resource subject = mc.asRDFNode(quad.getSubject()).asResource();
		Resource predicate = mc.asRDFNode(quad.getPredicate()).asResource();
		RDFNode object = mc.asRDFNode(quad.getObject());
		
		if (predicate.equals(RDF.type)) {
			String s = "";
			if (quad.getSubject().isBlank()) s = subject.getId().getLabelString();
			else s = subject.getURI();
			String o = object.asResource().getURI();
			List<String> types = new ArrayList<String>();
			types.add(o);
			
			Set<RDFNode> infer = VocabularyLoader.inferParent(object.asNode(), null, true);
			for(RDFNode nd : infer) types.add(nd.asResource().getURI());
			
			mapResourceType.put(s,types);
			
		}
		else {
			if (VocabularyLoader.checkTerm(predicate.asNode())){
				totalPredicates++; 

				Set<RDFNode> domains = null; 
				if (this.domains.containsKey(predicate.asNode())) domains = this.domains.get(predicate.asNode());
				else {
					domains = VocabularyLoader.getPropertyDomain(predicate.asNode());
					this.domains.put(predicate.asNode(), domains);
				}
				
				Set<RDFNode> range = null;
				if (this.ranges.containsKey(predicate.asNode())) range = this.ranges.get(predicate.asNode());
				else {
					range = VocabularyLoader.getPropertyRange(predicate.asNode());
					this.ranges.put(predicate.asNode(), range);
				}
				
				if ((domains.size() > 0) || (range.size() > 0)){
					// we will only check those properties that get dereferenced
					// and those which have a domain or a range identified
					
					// Domain Checker
					String uri = "";
					if (subject.isAnon()) uri = subject.toString();
					else uri = subject.getURI();
					
					if (mapResourceType.containsKey(uri)){
						Set<RDFNode> types = this.toRDFNodeSet(mapResourceType.get(uri));
	
						if(!(domains.removeAll(types))){
							addToProblem(quad,'d');
							incorrectDomain++;
						}
					} else {
						//type is unknown try again later
						unknownTypesDomain.add(new SerialisableTriple(quad.asTriple()));
					}
					// Range Checker
					if (object instanceof Literal){
						Resource litRes = this.getLiteralType(object.asNode());
						if (!range.contains(litRes)){
							addToProblem(quad,'r');
							incorrectRange++;
						}
					} else {
						//object is an instance of some URI
						String uriO = "";
						if (object.isAnon()) uri = object.asResource().toString();
						else uriO = object.asResource().getURI();
						
						if (mapResourceType.containsKey(uriO)){
							Set<RDFNode> types = this.toRDFNodeSet(mapResourceType.get(uriO));
	
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
					logger.debug("Domain and Range for {} unknown.", predicate.toString());
					unknownDomainAndRange++;
				}
			} else {
				logger.debug("Predicate {} not dereferenced.", predicate.toString());
				undereferenceablePredicates++;
				//problem unknown but do not reduce quality here
				//maybe report some statistics here instead of problem
			}
		}
	}
	
	//TODO: fix
    private void addToProblem(Quad q, char type){
//    	Model m = ModelFactory.createDefaultModel();
//    	
//    	Resource gen = Commons.generateURI();
//    	if (type == 'd')
//    		m.createStatement(gen, RDF.type, DQM.IncorrectDomain);
//    	if (type == 'r')
//    		m.createStatement(gen, RDF.type, DQM.IncorrectRange);
//    	if (type == 'u')
//    		m.createStatement(gen, RDF.type, DQM.IncorrectRange);
//
//    	
//    	Resource anon = m.createResource(AnonId.create());
//    	m.createStatement(gen, DQM.problematicTriple, anon);
//    	m.createStatement(anon, RDF.subject, Commons.asRDFNode(q.getSubject()));
//    	m.createStatement(anon, RDF.predicate, Commons.asRDFNode(q.getPredicate()));
//    	m.createStatement(anon, RDF.object, Commons.asRDFNode(q.getObject()));
//    	
//    	this.problemList.add(m);

    }
	
	@Override
	public double metricValue() {

		for(SerialisableTriple trip : unknownTypesDomain){
			Triple t = trip.getTriple();
			Set<RDFNode> domains = this.domains.get(t.getPredicate());
			String uri = "";
			if (t.getSubject().isBlank()) uri = t.getSubject().getBlankNodeLabel();
			else uri = t.getSubject().getURI();
			if (mapResourceType.containsKey(uri)){
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
			Set<RDFNode> range = this.ranges.get(t.getPredicate());
			String uri = "";
			
			if (t.getObject().isBlank()) uri = t.getObject().getBlankNodeLabel();
			else uri = t.getObject().getURI();
			
			if (mapResourceType.containsKey(uri)){
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

		
		logger.info("Dataset: {} - # Incorrect Domains : {}; # Incorrect Ranges : {}; # Predicates Assessed : {}; # Undereferenceable Predicates : {}; # Unknown Domain and Range : {}; "
				, EnvironmentProperties.getInstance().getDatasetURI(), incorrectDomain, incorrectRange, totalPredicates, undereferenceablePredicates, unknownDomainAndRange); //TODO: these store in a seperate file - statistics

		double value = 1 - ((double) incorrectDomain + (double) incorrectRange + (double) undereferenceablePredicates + (double) unknownDomainAndRange) / (((double) totalPredicates * 2) + (double) undereferenceablePredicates);
		
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
	
	private Resource getLiteralType(Node lit_obj){
		RDFNode n = Commons.asRDFNode(lit_obj);
		
		if (((Literal)n).getDatatype() != null){
			return ModelFactory.createDefaultModel().createResource(lit_obj.getLiteralDatatype().getURI());
		} else {
			Literal lt = (Literal) n;
			if (lt.getValue() instanceof Double) return XSD.xdouble;
			else if (lt.getValue() instanceof Integer) return XSD.xint;
			else if (lt.getValue() instanceof Boolean) return XSD.xboolean;
			else if (lt.getValue() instanceof String) return XSD.xstring;
			else if (lt.getValue() instanceof Float) return XSD.xfloat;
			else if (lt.getValue() instanceof Short) return XSD.xshort;
			else if (lt.getValue() instanceof Long) return XSD.xlong;
			else if (lt.getValue() instanceof Byte) return XSD.xbyte;
			else return RDFS.Literal;
		}
	}
}
