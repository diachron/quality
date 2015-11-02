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
import org.mapdb.HTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
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
	
	private HTreeMap<String, String> mapResourceType = MapDbFactory.createFilesystemDB().createHashMap("resource-type").make();
	
	//If the boolean is true, then we need to check the domain, else check the range
	private Set<SerialisableTriple> unknownTriples = MapDbFactory.createFilesystemDB().createHashSet("unknown-triples").make();

	
	private long totalPredicates = 0;
	private long incorrectDomain = 0;
	private long incorrectRange = 0;
	private long undereferenceablePredicates = 0;
	private long unknownDomainAndRange = 0;

	private ModelCom mc = new ModelCom(Graph.emptyGraph);
	
	private Map<Node, Set<RDFNode>> ranges = new HashMap<Node, Set<RDFNode>>();
	private Map<Node, Set<RDFNode>> domains = new HashMap<Node, Set<RDFNode>>();
	private Map<String, Set<RDFNode>> objectTypes = new HashMap<String, Set<RDFNode>>();
	
	@Override
	public void compute(Quad quad) {
		
		logger.debug("Computing : {} ", quad.asTriple().toString());
		
		if (quad.getPredicate().getURI().equals(RDF.type.getURI())) {
			String s = "";
			if (quad.getSubject().isBlank()) s = quad.getSubject().getBlankNodeLabel();
			else s = quad.getSubject().getURI();
			String o = quad.getObject().getURI();
			
			mapResourceType.put(s,o);
		}
		else {
			totalPredicates++; 
			this.unknownTriples.add(new SerialisableTriple(quad.asTriple()));
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
		
		for (SerialisableTriple trip : this.unknownTriples){
			Triple t = trip.getTriple();
			
			//check domain
			Set<RDFNode> _dom = null; 
			if (this.domains.containsKey(t.getPredicate())) _dom = this.domains.get(t.getPredicate());
			else {
				_dom = VocabularyLoader.getPropertyDomain(t.getPredicate());
				this.domains.put(t.getPredicate(), _dom);
			}
			
			String subURI = (t.getSubject().isBlank()) ? t.getSubject().toString() : t.getSubject().getURI();
			
			if (mapResourceType.containsKey(subURI)){
				String type = mapResourceType.get(subURI);
				
				Set<RDFNode> types = null;
				if (this.objectTypes.containsKey(type)) types = this.objectTypes.get(type);
				else {
					types = VocabularyLoader.inferParent(mc.createResource(type).asNode(), null, true);
					this.objectTypes.put(type, types);
				}
				
				if(!(_dom.removeAll(types))){
					addToProblem(new Quad(null, t),'d');
					incorrectDomain++;
				}
			} else {
				addToProblem(new Quad(null, t),'u');
				incorrectDomain++;
			}
			
			//check range
			Set<RDFNode> _ran = null; 
			if (this.ranges.containsKey(t.getPredicate())) _ran = this.ranges.get(t.getPredicate());
			else {
				_ran = VocabularyLoader.getPropertyRange(t.getPredicate());
				this.ranges.put(t.getPredicate(), _ran);
			}
			
			if (t.getObject().isLiteral()){
				Resource litRes = this.getLiteralType(t.getObject());
				if (!_ran.contains(litRes)){
					addToProblem(new Quad(null, t),'r');
					incorrectRange++;
				}
			} else {
				String objURI = (t.getObject().isBlank()) ? t.getObject().toString() : t.getObject().getURI();
				
				if (mapResourceType.containsKey(objURI)){
					String type = mapResourceType.get(objURI);
					
					Set<RDFNode> types = null;
					if (this.objectTypes.containsKey(type)) types = this.objectTypes.get(type);
					else {
						types = VocabularyLoader.inferParent(mc.createResource(type).asNode(), null, true);
						this.objectTypes.put(type, types);
					}
					
					if(!(_ran.removeAll(types))){
						addToProblem(new Quad(null, t),'r');
						incorrectRange++;
					}
				} else {
					addToProblem(new Quad(null, t),'u');
					incorrectRange++;
				}
			}
		}
		
		this.unknownTriples.clear();

		
		logger.info("Dataset: {} - # Incorrect Domains : {}; # Incorrect Ranges : {}; # Predicates Assessed : {}; # Undereferenceable Predicates : {}; # Unknown Domain and Range : {}; "
				, EnvironmentProperties.getInstance().getDatasetURI(), incorrectDomain, incorrectRange, totalPredicates, undereferenceablePredicates, unknownDomainAndRange); //TODO: these store in a seperate file - statistics

		double value = 1 - ((double) incorrectDomain + (double) incorrectRange + (double) undereferenceablePredicates + (double) unknownDomainAndRange) / ((double) totalPredicates * 2);
		
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
