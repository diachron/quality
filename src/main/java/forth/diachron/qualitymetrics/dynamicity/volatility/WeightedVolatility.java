package forth.diachron.qualitymetrics.dynamicity.volatility;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
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
public class WeightedVolatility implements EvolutionQualityMetricInterface {
	
	private final Resource METRIC_URI = null; //= DQM.CurrencyOfDocumentStatementsMetric;
	private static Logger logger = Logger.getLogger(WeightedVolatility.class);
	
	private VirtuosoHandler chan = new VirtuosoHandler();
	private EvolutionGenHandler evohand = new EvolutionGenHandler();
	private double retValue = 0;
	private int versionsNO = 1;
	private double aggregDeltas = 0;
	private double evo_weights[] = {0.1,0.05,0.12,0.18,0.20,0.1,0.01,0.09,0.15}; // An array do declare evolution weights for each pair of versions
	private ArrayList weightList = null; 
	
	
	
	public void compute() {
	
		//STEPS:
		//1. Load evolution weights preference table. The total number of versions (nversions) should agree with the size of weights table. 	
		//2. Find deltas per pair and multiply with the corresponding weight
		//3. Calculate the ratio weighted sum of deltas / nversions - 1
		
		weightList = new ArrayList();
		for(int i=0; i<evo_weights.length; i++){
			weightList.add(evo_weights[i]);
		}
			
		double cur_weight = 0.0;
		double cur_sum = 0.0;
		
		String versionsQ = "select distinct ?oversion ?nversion "
                + "FROM <http://detected_changes/copy>"
                + " WHERE {"
                + "?instance co:new_version ?nversion." 
                + "?instance co:old_version ?oversion." 
                +" filter(?nversion != diachron:Entity && ?oversion != diachron:Entity )" 
                + "}  ORDER BY ?nversion"; 
		
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
				Iterator it = weightList.iterator();	
				
				
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
					logger.trace("Computing total number of deltas and versions");
					
					
					// Fetching weights to each pair
					
					
				      Object weig = it.next();
				      cur_weight = new Double(weig.toString());
				      System.out.println("--------------------------------------------------------");
				      cur_sum =  cur_weight* this.evohand.countDeltas(rv[0].stringValue(),rv[1].stringValue());
				      //System.out.println("cur_weight:"+cur_weight);
				     // System.out.println("cur_sum:"+cur_sum);
				      aggregDeltas = aggregDeltas + cur_sum;			   
				      versionsNO ++;
				}
				      

					//results.add(rv);
					System.out.println("-----------------------------deltasTotal:"+aggregDeltas);
					System.out.println("-----------------------------versionsNO:"+versionsNO);
					System.out.println("-----------------------------evo_weightsNO:"+evo_weights.length);
				
		
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
	
    retValue = aggregDeltas / (versionsNO-1);
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