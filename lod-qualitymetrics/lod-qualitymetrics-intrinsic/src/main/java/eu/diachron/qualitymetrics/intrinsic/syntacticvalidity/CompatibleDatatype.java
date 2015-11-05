/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.syntacticvalidity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Bag;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.XSD;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import de.unibonn.iai.eis.luzzu.semantics.utilities.Commons;

/**
 * @author Jeremy Debattista
 * 
 */
public class CompatibleDatatype implements QualityMetric {

	private static Logger logger = LoggerFactory.getLogger(CompatibleDatatype.class);
	
	private Model problemModel = ModelFactory.createDefaultModel();
	private  Resource bagURI = Commons.generateURI();
	private Bag problemBag = problemModel.createBag(bagURI.getURI());
	{
		//TODO: fix
		problemModel.createStatement(bagURI, RDF.type, problemModel.createResource(DQM.NAMESPACE+"CompatibleDatatypeException"));
	}
	
	private int numberCorrectLiterals = 0;
	private int numberIncorrectLiterals = 0;
	private int numberUnknownLiterals = 0;

	@Override
	public void compute(Quad quad) {
		Node obj = quad.getObject();
		
		if (obj.isLiteral()){
			
			if (obj.getLiteralDatatype() != null){
				// we will try parse the value and check if it corresponds to
				// assigned datatype
				if (this.compatibleDatatype(obj)) 
					numberCorrectLiterals++; 
				else {
					this.addToProblem(quad);
					numberIncorrectLiterals++;
				}
			} else {
				// unknown datatypes cannot be checked for their correctness,
				// but in the UsageOfIncorrectDomainOrRangeDatatypes metric
				// we check if these literals are used correctly against their
				// defined property.
				logger.debug("Literal: {} has an unknown datatype", obj.getLiteralValue().toString());
				numberUnknownLiterals++; 
			}
		}
	}
	
	//TODO: Fix
    private void addToProblem(Quad q){
    	Resource anon = problemModel.createResource(AnonId.create());
    	
    	problemModel.createStatement(anon, RDF.subject, Commons.asRDFNode(q.getSubject()));
    	problemModel.createStatement(anon, RDF.predicate, Commons.asRDFNode(q.getPredicate()));
    	problemModel.createStatement(anon, RDF.object, Commons.asRDFNode(q.getObject()));
    	
    	problemBag.add(anon);
    }
    

	@Override
	public double metricValue() {
		statsLogger.info("CompatibleDatatype. Dataset: {} - Total # Correct Literals : {}; # Incorrect Literals : {}; # Unknown Literals : {}", 
				EnvironmentProperties.getInstance().getDatasetURI(), numberCorrectLiterals, numberIncorrectLiterals, numberUnknownLiterals);

		return (double) numberCorrectLiterals / ((double)numberIncorrectLiterals + (double)numberCorrectLiterals + (double)numberUnknownLiterals);
	}

	@Override
	public Resource getMetricURI() {
		return DQM.CompatibleDatatype;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		ProblemList<Model> tmpProblemList = null;
		try {
			if(this.problemModel != null && this.problemModel.size() > 0) {
				List<Model> problemList = new ArrayList<Model>();
				problemList.add(problemModel);
				tmpProblemList = new ProblemList<Model>(problemList);
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
	
	private boolean compatibleDatatype(Node lit_obj){
		RDFNode n = Commons.asRDFNode(lit_obj);
		Literal lt = (Literal) n;
		RDFDatatype dt = lt.getDatatype();
		String stringValue = lt.getLexicalForm();
		
		//datetime
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
		try {
			logger.debug("Checking literal value: {}, if it is a valid boolean", stringValue);
			sdf.parse(stringValue);
			return true;
		} catch (ParseException ex){}
		if ((stringValue.equalsIgnoreCase("true")) || (stringValue.equalsIgnoreCase("false"))){
			logger.debug("Checking literal value: {}, if it is a valid boolean", stringValue);
			return dt.getURI().equals(XSD.xboolean.getURI());
		}
		else if (stringValue.matches(".*[a-zA-Z]+.*") && (StringUtils.isAlphanumericSpace(stringValue))){
			logger.debug("Checking literal value: {}, if it is a valid string", stringValue);
			return dt.getURI().equals(XSD.xstring.getURI()); 
		}
		else if (stringValue.matches("[-]*[0-9]+") ){
			// numeric non-decimal
			boolean nonNumericDecimal = false;
			try {
				logger.debug("Checking literal value: {}, if it is a valid byte", stringValue);
				Byte.parseByte(stringValue);
				nonNumericDecimal = dt.getURI().equals(XSD.xbyte.getURI());
			} catch (NumberFormatException nfe){}
			
			if (!nonNumericDecimal)
				try {
					logger.debug("Checking literal value: {}, if it is a valid short", stringValue);
					Short.parseShort(stringValue);
					nonNumericDecimal = dt.getURI().equals(XSD.xshort.getURI());
				} catch (NumberFormatException nfe){}
			
			if (!nonNumericDecimal)
				try {
					logger.debug("Checking literal value: {}, if it is a valid integer", stringValue);
					Integer.parseInt(stringValue);
					nonNumericDecimal = dt.getURI().equals(XSD.xint.getURI());
				} catch (NumberFormatException nfe){}
			
			if (!nonNumericDecimal)
				try {
					logger.debug("Checking literal value: {}, if it is a valid long", stringValue);
					Long.parseLong(stringValue);
					nonNumericDecimal = dt.getURI().equals(XSD.xlong.getURI());
				} catch (NumberFormatException nfe){}
			
			return nonNumericDecimal;
		} else if (stringValue.contains(".") && !(stringValue.matches(".*[a-zA-Z]+.*"))){
			// numeric float or double
			boolean floatOrDouble = false;
			try {
				logger.debug("Checking literal value: {}, if it is a valid float", stringValue);
				Float.parseFloat(stringValue);
				floatOrDouble = dt.getURI().equals(XSD.xfloat.getURI());
			} catch (NumberFormatException nfe){}
			
			if (!floatOrDouble)
				try {
					logger.debug("Checking literal value: {}, if it is a valid double", stringValue);
					Double.parseDouble(stringValue);
					floatOrDouble = dt.getURI().equals(XSD.xdouble.getURI());
				} catch (NumberFormatException nfe){}
				
			return floatOrDouble;
		} 	else if (!StringUtils.isAlphanumeric(stringValue)){
			logger.debug("Checking literal value: {}, if it is a valid string with non-alphanumeric symbols", stringValue);
			return dt.getURI().equals(XSD.xstring.getURI()); 
		}
		
		
		return false;
	}
}
