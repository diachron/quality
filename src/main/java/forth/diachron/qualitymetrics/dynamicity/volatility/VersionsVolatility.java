package forth.diachron.qualitymetrics.dynamicity.volatility;

import java.util.List;
import org.apache.log4j.Logger;
import org.openrdf.repository.RepositoryConnection;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Resource;
import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import forth.diachron.connectivity.VirtuosoHandler;
import forth.diachron.qualitymetrics.EvolutionGenHandler;
import forth.diachron.qualitymetrics.EvolutionQualityMetricInterface;

/**
 * @author Ioannis Chrysakis
 * 
 */
//This class defines the VersionsVolatility metric
public class VersionsVolatility implements EvolutionQualityMetricInterface {
	
	private final Resource METRIC_URI = null; //= DQM.CurrencyOfDocumentStatementsMetric;
	
	private static Logger logger = Logger.getLogger(VersionsVolatility.class);
	

	//Counts the number of changes
	private long numberOfChanges = 0;
	
	//declare here versions to be compared, otherwise the default lates ones
	//would be compared.
	private String old_version_uri ="";
	private String new_version_uri = "";
	
	private VirtuosoHandler chan = new VirtuosoHandler();
	private EvolutionGenHandler evohand = new EvolutionGenHandler();
	
	
	public void compute() {
	
		logger.trace("Start computing volatility accross two versions");

		RepositoryConnection con = chan.getVirtuosoConnection();
		if (con!= null){
			logger.trace("Connecting to Virtuoso");	
			System.out.println("Connecting to Virtuoso");
			numberOfChanges = evohand.countDeltas(old_version_uri,new_version_uri);
			logger.trace("Finish computing volatility accross two versions");
		}
		logger.trace("Error while connecting to Virtuoso!");	
	}

	
	public double metricValue() {
	double retValue = 0;
	
	retValue = (double) this.numberOfChanges;
	logger.trace("Returning VersionsVolatility Metric Value (Number): " +retValue);
		
	return retValue;
	}
	
	

	
	public Resource getMetricURI() {
		return METRIC_URI;
	}

	
	public ProblemList<?> getQualityProblems() {
		// TODO Auto-generated method stub
		return null;
	}


	public List<Triple> toDAQTriples() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	

	 
	

}