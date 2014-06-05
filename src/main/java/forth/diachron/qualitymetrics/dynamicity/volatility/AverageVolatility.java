package forth.diachron.qualitymetrics.dynamicity.volatility;

import java.util.List;

import org.apache.log4j.Logger;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
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
public class AverageVolatility implements EvolutionQualityMetricInterface {
	
	private final Resource METRIC_URI = null; //= DQM.CurrencyOfDocumentStatementsMetric;
	
	private static Logger logger = Logger.getLogger(AverageVolatility.class);
	
	private VirtuosoHandler chan = new VirtuosoHandler();
	private EvolutionGenHandler evohand = new EvolutionGenHandler();
	private SesameQueryHandler seshand = new SesameQueryHandler();
	
	
	
	public void compute() {
	
		
		//1. find total number of versions (sparql1)
		//2. find deltas per pair deltas[versions]  (sparql2...N)
		//3  calculate the ratio
		
		logger.trace("Start computing volatility accross two versions");
		String versionsQ = "select distinct  ?nversion ?oversion "
                + "FROM <http://detected_changes/copy>"
                + " WHERE {"
                + "?instance co:new_version ?nversion." 
                + "?instance co:old_version ?oversion." 
                +" filter(?nversion != diachron:Entity && ?oversion != diachron:Entity )" 
                + "}"; 
		System.out.println("versionsQ:" +versionsQ);
		RepositoryConnection con = chan.getVirtuosoConnection();
		if (con!= null){
			logger.trace("Connecting to Virtuoso:");	
			try {
				seshand.doTupleQuery(con, versionsQ);
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
	double retValue = 0;
	////JCH to be fixed retvalue according to formula
	///retValue = (double) this.numberOfChanges / 2;
	///logger.trace("Returning Volatility accross version " +old_version +" and version " +new_version + "Ratio is" +retValue);
		
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