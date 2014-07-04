package forth.diachron.qualitymetrics.dynamicity.volatility;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import forth.diachron.connectivity.VirtuosoHandler;
import forth.diachron.qualitymetrics.EvolutionGenHandler;
import forth.diachron.qualitymetrics.EvolutionQualityMetricInterface;
import forth.diachron.qualitymetrics.SesameQueryHandler;

/**
 * @author Ioannis Chrysakis
 * 
 */

//This class defines the AverageVolatility metric
public class AverageVolatility implements EvolutionQualityMetricInterface {
	
	private final Resource METRIC_URI = null; //= DQM.CurrencyOfDocumentStatementsMetric;
	private static Logger logger = Logger.getLogger(AverageVolatility.class);
	
	private VirtuosoHandler chan = new VirtuosoHandler();
	private EvolutionGenHandler evohand = new EvolutionGenHandler();
	private SesameQueryHandler seshand = new SesameQueryHandler();
	private double retValue = 0;
	private int versionsNO = 1;
	private int changesTotal = 0;
	
	
	public void compute() {
	
		//STEPS:
		//1. find total number of versions - versionsQ
		//2. find simple changes per pair sc[versions]  (sparql2...N)
		//3. aggregate simple changes sum per pair
		//4  calculate the ratio sum of sc / nversions - 1
		
			
		String versionsQ = "select distinct ?oversion ?nversion "
                + "FROM <http://detected_changes/copy>"
                + " WHERE {"
                + "?instance co:new_version ?nversion." 
                + "?instance co:old_version ?oversion." 
                +" filter(?nversion != diachron:Entity && ?oversion != diachron:Entity )" 
                + "} ORDER BY ?nversion"; 
		//System.out.println("versionsQ:" +versionsQmetr);
		RepositoryConnection con = chan.getVirtuosoConnection();
		if (con!= null){
			logger.trace("Connecting to Virtuoso:");
			
			try {
				//seshand.doTupleQuery(con, versionsQ);
				TupleQuery resultsTable = con.prepareTupleQuery(QueryLanguage.SPARQL, versionsQ);
				TupleQueryResult bindings = resultsTable.evaluate();
				String name = "";
				Value value = null;
				int row;
				//Vector<Value[]> results = new Vector<Value[]>();
				BindingSet pairs = null;
								
				for (row = 0; bindings.hasNext(); row++) {
					//System.out.println("RESULT " + (row + 1) + ": ");
					pairs = bindings.next();
					
					List<String> names = bindings.getBindingNames();
					Value[] rv = new Value[names.size()];
					for (int i = 0; i < names.size(); i++) {
						 name = names.get(i);
						 //System.out.println("Name:" +name);
						 value = pairs.getValue(name);
		                 //System.out.println("Value:" +value);
						 rv[i] = value;
						
						
					}
					logger.trace("Computing total number of changes and versions");
					changesTotal = changesTotal + this.evohand.countSimpleChanges(rv[0].stringValue(),rv[1].stringValue());			
					versionsNO ++;
					//System.out.println("-----------------------------");
					//results.add(rv);
				}	
				
		
			} catch (RepositoryException e) {
				logger.trace("RepositoryException:" +e.getMessage());
				e.printStackTrace();
			} catch (MalformedQueryException e) {
				logger.trace("MalformedQueryException:" +e.getMessage());
				e.printStackTrace();
			} catch (QueryEvaluationException e) {
				logger.trace("QueryEvaluationException:" +e.getMessage());
				e.printStackTrace();
			}
			
	
		}
		logger.trace("Error while connecting to Virtuoso");	
	}

	
	public double metricValue() {
	
    retValue = changesTotal / (versionsNO-1);
    logger.trace("Returning AverageVolatility Metric Value (Ratio): " +retValue);
	return retValue;
	}
	
	

	
	public Resource getMetricURI() {
		return METRIC_URI;
	}

	
	public ProblemList<?> getQualityProblems() {
		// TODO Auto-generated method stub
		return null;
	}

	public void compute(Quad quad) {
		// TODO Auto-generated method stub
		
	}

	public List<Triple> toDAQTriples() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	

}