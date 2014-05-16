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
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import virtuoso.sesame2.driver.VirtuosoRepository;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;

/**
 * @author Ioannis Chrysakis
 * 
 */
public class VersionsVolatility extends AbstractQualityMetric {
	
	private final Resource METRIC_URI = null; //= DQM.CurrencyOfDocumentStatementsMetric;
	
	private static Logger logger = Logger.getLogger(VersionsVolatility.class);
	
	private static final String VIRTUOSO_INSTANCE = "139.91.183.65";
	private static final int VIRTUOSO_PORT = 1111;
	private static final String VIRTUOSO_USERNAME = "dba";
	private static final String VIRTUOSO_PASSWORD = "dba";
	private String old_version_uri = "http://www.diachron-fp7.eu/resource/recordset/efo/2.43/0CE6051A873C76DE869861B2858AC646";
	private String new_version_uri = "http://www.diachron-fp7.eu/resource/recordset/efo/2.44/EE4C343E460F87536B9C759803160143";
	
	//Counts the number of changes
	private long numberOfChanges = 0;
	private String old_version = "";
	private String new_version = "";
	
	
	
	@Override
	public void compute(Quad quad) {
	
		//TODO check if quad parsing can be used
		logger.trace("Start computing volatility accross two versions");
		String sparqlQuery = "select ?instance ?simple_change ?nversion ?oversion "
                + "FROM <http://detected_changes/copy>"
                + " where {"
                + "?instance a ?simple_change." 
                + "?simple_change rdfs:subClassOf co:Simple_Change." 
                + "?instance co:new_version ?nversion." 
                + "?instance co:old_version ?oversion." 
                +"filter(?oversion = <"+old_version_uri +">)" 
                +"filter(?nversion = <"+new_version_uri +">)"
                + "}"; 
		
		RepositoryConnection con = this.getVirtuosoConnection();
		if (con!= null){
			logger.trace("Connecting to Virtuoso:" +VIRTUOSO_INSTANCE +":" +VIRTUOSO_PORT);	
			numberOfChanges = countTupleQueryResult (con,sparqlQuery);
			logger.trace("Finish computing volatility accross two versions");
		}
		logger.trace("Error while connecting to Virtuoso:" +VIRTUOSO_INSTANCE +":" +VIRTUOSO_PORT);	
	}

	@Override
	public double metricValue() {
	double retValue = 0;
	
	retValue = (double) this.numberOfChanges / 2;
	logger.trace("Returning Volatility accross version " +old_version +" and version " +new_version + "Ratio is" +retValue);
		
	return retValue;
	}
	
	

	@Override
	public Resource getMetricURI() {
		return METRIC_URI;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private RepositoryConnection getVirtuosoConnection () {

		Repository repository = new VirtuosoRepository("jdbc:virtuoso://" + VIRTUOSO_INSTANCE + ":" + VIRTUOSO_PORT, VIRTUOSO_USERNAME, VIRTUOSO_PASSWORD);
		RepositoryConnection con = null;
		try {
			con = repository.getConnection();
			con = repository.getConnection();
			con.setAutoCommit(true);
			return con;
		} catch (RepositoryException e) {
			System.out.println("Virtuoso Repository Connection Error:" +e.getMessage());
			return null;
		}
	}
	
	

	 private static int countTupleQueryResult(RepositoryConnection con, String query)
	 
	 {
		int count = 0;	 
      TupleQuery resultsTable = null;
      TupleQueryResult result = null;
	try {
		resultsTable = con.prepareTupleQuery(QueryLanguage.SPARQL, query);
		result = resultsTable.evaluate();
		while (result.hasNext()) {
		    result.next();
		    count++;
		  }
		result.close();
	} catch (RepositoryException e) {		
		count = 0;
	} catch (MalformedQueryException e) {	
		count = 0;
	}
       catch (QueryEvaluationException e) {
		count = 0;
	}
	  
	  System.out.println("...............countTupleQueryResult......"+count);
	  return count;
	}
	
	 //Not currently in use
	private Value[][] doTupleQuery(RepositoryConnection con, String query) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		TupleQuery resultsTable = con.prepareTupleQuery(QueryLanguage.SPARQL, query);
		TupleQueryResult bindings = resultsTable.evaluate();
		
		
		Vector<Value[]> results = new Vector<Value[]>();
		for (int row = 0; bindings.hasNext(); row++) {
			//System.out.println("RESULT " + (row + 1) + ": ");
			BindingSet pairs = bindings.next();
			List<String> names = bindings.getBindingNames();
			Value[] rv = new Value[names.size()];
			for (int i = 0; i < names.size(); i++) {
				String name = names.get(i);
				Value value = pairs.getValue(name);
                                System.out.println("Value:" +value);
				rv[i] = value;
				// if(column > 0) System.out.print(", ");
				// System.out.println("\t" + name + "=" + value);
				// vars.add(value);
				// if(column + 1 == names.size()) System.out.println(";");
			}
			results.add(rv);
		}
                System.out.println("VECTOR RESULTS:" +results.toString());
		return (Value[][]) results.toArray(new Value[0][0]);
	}
	

}