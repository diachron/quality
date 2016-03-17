/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.consistency;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import de.unibonn.iai.eis.diachron.semantics.DQMPROB;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ReservoirSampler;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
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
	
	private static DB mapDb = MapDbFactory.getMapDBAsyncTempFile();
	
	private HTreeMap<String, String> mapResourceType =  MapDbFactory.createHashMap(mapDb, UUID.randomUUID().toString());
	
	//If the boolean is true, then we need to check the domain, else check the range
	private Set<SerialisableTriple> unknownTriples =  MapDbFactory.createHashSet(mapDb, UUID.randomUUID().toString());

//	protected Set<SerialisableModel> problemList =  MapDbFactory.createHashSet(mapDb, UUID.randomUUID().toString());
	
	//Sampling of Problems
	ReservoirSampler<ProblemReport> problemSampler = new ReservoirSampler<ProblemReport>(1000, false);

	
	
	
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
	
//    private void addToProblem(Quad q, char type){
//    	Model m = ModelFactory.createDefaultModel();
//    	
//    	Resource gen = Commons.generateURI();
//    	if (type == 'd')
//    		m.add(gen, RDF.type, DQM.IncorrectDomain);
//    	if (type == 'r')
//    		m.add(gen, RDF.type, DQM.IncorrectRange);
//    	if (type == 'x')
//    		m.add(gen, RDF.type, DQM.IncorrectDomain);
//    	if (type == 'u')
//    		m.add(gen, RDF.type, DQM.IncorrectRange);
//
//    	
//    	Resource anon = m.createResource(AnonId.create());
//    	m.add(gen, DQM.problematicTriple, anon);
//    	m.add(anon, RDF.subject, Commons.asRDFNode(q.getSubject()));
//    	m.add(anon, RDF.predicate, Commons.asRDFNode(q.getPredicate()));
//    	m.add(anon, RDF.object, Commons.asRDFNode(q.getObject()));
//    	
//    	try{
//    		this.problemList.add(new SerialisableModel(m));
//    	} catch (Exception e){
//    		logger.error("Error adding to problem list :" + e.getMessage());
//    	}
//    }
	
	private void addToProblem(Quad q, char type){
		ProblemReport pr = new ProblemReport(q, type);
		Boolean isAdded = this.problemSampler.add(pr);
		if (!isAdded) pr = null;
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
				addToProblem(new Quad(null, t),'x');
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

		
		statsLogger.info("Dataset: {} - # Incorrect Domains : {}; # Incorrect Ranges : {}; # Predicates Assessed : {}; # Undereferenceable Predicates : {}; # Unknown Domain and Range : {}; "
				, EnvironmentProperties.getInstance().getDatasetURI(), incorrectDomain, incorrectRange, totalPredicates, undereferenceablePredicates, unknownDomainAndRange); //TODO: these store in a seperate file - statistics

		double value = 1 - ((double) incorrectDomain + (double) incorrectRange + (double) undereferenceablePredicates + (double) unknownDomainAndRange) / ((double) totalPredicates * 2);
		
		return value;
	}

	@Override
	public Resource getMetricURI() {
		return METRIC_URI;
	}

//	@Override
//	public ProblemList<?> getQualityProblems() {
//		ProblemList<SerialisableModel> tmpProblemList = null;
//		try {
//			if(this.problemList != null && this.problemList.size() > 0) {
//				tmpProblemList = new ProblemList<SerialisableModel>(new ArrayList<SerialisableModel>(this.problemList));
//			} else {
//				tmpProblemList = new ProblemList<SerialisableModel>();
//			}		} catch (ProblemListInitialisationException problemListInitialisationException) {
//			logger.error(problemListInitialisationException.getMessage());
//		}
//		return tmpProblemList;
//	}
	public ProblemList<?> getQualityProblems() {
		ProblemList<Model> tmpProblemList = new ProblemList<Model>();
		if(this.problemSampler != null && this.problemSampler.size() > 0) {
			for(ProblemReport pr : this.problemSampler.getItems()){
				tmpProblemList.getProblemList().add(pr.createProblemModel());
			}
		} else {
			tmpProblemList = new ProblemList<Model>();
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
	
	
	private Resource getLiteralType(Node lit_obj){
		RDFNode n = Commons.asRDFNode(lit_obj);
		
		if (((Literal)n).getDatatype() != null){
			return ModelFactory.createDefaultModel().createResource(lit_obj.getLiteralDatatype().getURI());
		} else {
			Literal lt = (Literal) n;
			if (lt.getValue() instanceof Byte) return XSD.xbyte;
			else if (lt.getValue() instanceof Boolean) return XSD.xboolean;
			else if (lt.getValue() instanceof Short) return XSD.xshort;
			else if (lt.getValue() instanceof Integer) return XSD.xint;
			else if (lt.getValue() instanceof Long) return XSD.xlong;
			else if (lt.getValue() instanceof Float) return XSD.xfloat;
			else if (lt.getValue() instanceof Double) return XSD.xdouble;
			else if (lt.getValue() instanceof String) return XSD.xstring;
			else return RDFS.Literal;
		}
	}
	
	// problems private class for sampling
	private class ProblemReport{
		
		private Quad q ;
		private char type;
		
		ProblemReport(Quad q, char type){
			this.q = q;
			this.type = type;
		}
		
	   Model createProblemModel(){
	    	Model m = ModelFactory.createDefaultModel();
	    	
	    	Resource gen = Commons.generateURI();
	    	if (type == 'd')
	    		m.add(gen, RDF.type, DQMPROB.IncorrectDomain);
	    	if (type == 'r')
	    		m.add(gen, RDF.type, DQMPROB.IncorrectRange);
	    	if (type == 'x')
	    		m.add(gen, RDF.type, DQMPROB.IncorrectDomain);
	    	if (type == 'u')
	    		m.add(gen, RDF.type, DQMPROB.IncorrectRange);

	    	
	    	Resource anon = m.createResource(AnonId.create());
	    	m.add(gen, DQMPROB.problematicTriple, anon);
	    	m.add(anon, RDF.subject, Commons.asRDFNode(q.getSubject()));
	    	m.add(anon, RDF.predicate, Commons.asRDFNode(q.getPredicate()));
	    	m.add(anon, RDF.object, Commons.asRDFNode(q.getObject()));
	    	
	    	try{
	    		return m;
	    	} catch (Exception e){
	    		logger.error("Error adding to problem list :" + e.getMessage());
	    		return ModelFactory.createDefaultModel();
	    	}

	    }
	}
}
