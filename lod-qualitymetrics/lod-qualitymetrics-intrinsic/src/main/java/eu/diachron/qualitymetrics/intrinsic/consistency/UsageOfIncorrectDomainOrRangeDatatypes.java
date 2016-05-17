/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.consistency;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
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
import eu.diachron.qualitymetrics.utilities.AbstractQualityMetric;
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
public class UsageOfIncorrectDomainOrRangeDatatypes extends AbstractQualityMetric {

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
			if (VocabularyLoader.getInstance().checkTerm(t.getPredicate())){
				checkDomain(t);
				checkRange(t);
			} else {
				undereferenceablePredicates++;
			}
		}
		
		this.unknownTriples.clear();

		double value = 1 - ((double) incorrectDomain + (double) incorrectRange + (double) undereferenceablePredicates + (double) unknownDomainAndRange) / ((double) totalPredicates * 2);

		
		statsLogger.info("Dataset: {} - # Incorrect Domains : {}; # Incorrect Ranges : {}; # Predicates Assessed : {}; # Undereferenceable Predicates : {}; # Unknown Domain and Range : {}; Metric Value: {} "
				, EnvironmentProperties.getInstance().getDatasetURI(), incorrectDomain, incorrectRange, totalPredicates, undereferenceablePredicates, unknownDomainAndRange, value);
		
		return value;
	}

	
	private void checkDomain(Triple t){
		String subURI = (t.getSubject().isBlank()) ? t.getSubject().toString() : t.getSubject().getURI();

		if (mapResourceType.containsKey(subURI)){
			String type = mapResourceType.get(subURI);
			
			Set<RDFNode> types = new LinkedHashSet<RDFNode>();
			types.add(mc.createResource(type));
			types.addAll(VocabularyLoader.getInstance().inferParentClass(mc.createResource(type).asNode()));

			Set<RDFNode> _dom = VocabularyLoader.getInstance().getPropertyDomain(t.getPredicate());

			if(Sets.intersection(_dom, types).size() == 0){
				addToProblem(new Quad(null, t),'d');
				incorrectDomain++;
			}
		} else {
			addToProblem(new Quad(null, t),'x');
			unknownDomainAndRange++;
		}
	}
	
	private void checkRange(Triple t){
		Set<RDFNode> _ran = VocabularyLoader.getInstance().getPropertyRange(t.getPredicate());
		
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
				
				Set<RDFNode> types = new LinkedHashSet<RDFNode>();;
				types.add(mc.createResource(type));
				types.addAll(VocabularyLoader.getInstance().inferParentClass(mc.createResource(type).asNode()));
				
				
				if(Sets.intersection(_ran, types).size() == 0){
					addToProblem(new Quad(null, t),'r');
					incorrectRange++;
				}
			} else {
				addToProblem(new Quad(null, t),'u');
				unknownDomainAndRange++;
			}
		}
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
		Literal lt = (Literal) n;
		
		if (((Literal)n).getDatatype() != null){
			return  ModelFactory.createDefaultModel().createResource(lt.getDatatype().getURI());
		} else {
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
