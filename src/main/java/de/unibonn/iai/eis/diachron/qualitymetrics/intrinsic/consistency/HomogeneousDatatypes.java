package de.unibonn.iai.eis.diachron.qualitymetrics.intrinsic.consistency;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;

public class HomogeneousDatatypes extends AbstractQualityMetric{

	static Logger logger = Logger.getLogger(HomogeneousDatatypes.class);

	protected static Long THRESHOLD = new Long(99);
	
	protected long propertiesWithHeterogeneousDatatype = 0;
	protected long totalProperties = 0;
	
	protected Hashtable<Node, Hashtable<RDFDatatype, Long>> propertiesDatatypeMatrix = new Hashtable<Node, Hashtable<RDFDatatype,Long>>();   
	
	protected List<Node> problemList = new ArrayList<Node>();
	
	public long getPropertiesWithHeterogeneousDatatype() {
		return propertiesWithHeterogeneousDatatype;
	}

	public long getTotalProperties() {
		return totalProperties;
	}
	
	@Override
	public void compute(Quad quad) {
		logger.trace("compute() --Started--");
		try{
			
			Node predicate  = quad.getPredicate(); //retrieve predicate
			Node object = quad.getObject(); //retrieve object
			if (object.isLiteral()) {
				
				this.totalProperties++;
				
				// retrieves rdfDataType from literal
				RDFDatatype rdfdataType = object.getLiteralDatatype();

				// check if rdf data type is a valid data type
				if (rdfdataType != null) {

					if (propertiesDatatypeMatrix.containsKey(predicate)){ //matrix contains given object
						Hashtable<RDFDatatype, Long> tmpObjectTypes = propertiesDatatypeMatrix.get(predicate);
						if (tmpObjectTypes != null){ //given datatype association already exists 
							
							Long tmpCount = new Long(0); //datatype count initial value ZERO
							
							if (tmpObjectTypes.containsKey(rdfdataType)){
								tmpCount = tmpObjectTypes.get(rdfdataType);
								tmpCount++;
								
								tmpObjectTypes.remove(rdfdataType);
								tmpObjectTypes.put(rdfdataType, tmpCount);
							} else
							{
								tmpCount++;
								tmpObjectTypes.put(rdfdataType,tmpCount);
							}
							propertiesDatatypeMatrix.remove(predicate);
							propertiesDatatypeMatrix.put(predicate, tmpObjectTypes);
						}
						else { //given datatype association does NOT exists
							
							tmpObjectTypes = new Hashtable<RDFDatatype, Long>();
							tmpObjectTypes.put(rdfdataType, new Long(1));
							
							propertiesDatatypeMatrix.remove(predicate);
							propertiesDatatypeMatrix.put(predicate, tmpObjectTypes);
						}
					}
					else {//matrix does not contain given object
						Hashtable<RDFDatatype, Long> tmpObjectTypes = new Hashtable<RDFDatatype, Long>();
						tmpObjectTypes.put(rdfdataType, new Long(1));
						propertiesDatatypeMatrix.put(predicate, tmpObjectTypes);
					}
				}
			}
		}
		catch (Exception exception){
			logger.debug(exception);
        	logger.error(exception.getMessage());
		}
		logger.trace("compute() --Ended--");
	}

	protected boolean isHeterogeneousDataType(Hashtable<RDFDatatype, Long> givenTable, Long threshold){
		
		Long tmpMax = new Long(0); //for count of Max dataType
		Long tmpTotal = new Long(0); //for count of total
		
		Enumeration<RDFDatatype> enumKey = givenTable.keys();
		
		while (enumKey.hasMoreElements()){
			RDFDatatype key = enumKey.nextElement();
			Long value = givenTable.get(key);
			tmpMax = (value > tmpMax) ? value : tmpMax; //get Max Datatype
			tmpTotal += value; // count total
		}
		
		return (((tmpMax/tmpTotal) * 100) >= threshold) ? true : false;
	}
	
	protected long countHeterogeneousDataTypePropeties(){
		long tmpCount = 0;
		Enumeration<Node> enumKey = propertiesDatatypeMatrix.keys();
		while (enumKey.hasMoreElements()){
			Node key = enumKey.nextElement();
			if (!isHeterogeneousDataType(propertiesDatatypeMatrix.get(key), HomogeneousDatatypes.THRESHOLD)){
				tmpCount++;
				this.problemList.add(key);
			}
		}
		return tmpCount;
	}
	
	@Override
	public double metricValue() {
		
		logger.trace("metricValue() --Started--");
		
		this.propertiesWithHeterogeneousDatatype = countHeterogeneousDataTypePropeties();
		
		logger.info("Total Properties with Heterogeneous Datatype :: " + getPropertiesWithHeterogeneousDatatype());
		logger.info("Total Properties :: " + getTotalProperties());
		
		//return ZERO if total number of properties are ZERO [WARN]
		if (totalProperties <= 0) {
			logger.warn("Total number of properties in given document is found to be zero.");
			return 0.0;
		}
		
		double metricValue = (double) propertiesWithHeterogeneousDatatype / totalProperties;
		
		logger.trace("metricValue() --Ended--");
		
		return metricValue;
	}

	@Override
	public Resource getMetricURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		ProblemList<Node> tmpProblemList = null;
		try {
			tmpProblemList = new ProblemList<Node>(this.problemList); 
		} 
		catch (ProblemListInitialisationException problemListInitialisationException){
			logger.debug(problemListInitialisationException);
        	logger.error(problemListInitialisationException.getMessage());
		}
		return tmpProblemList;	
	}

}
