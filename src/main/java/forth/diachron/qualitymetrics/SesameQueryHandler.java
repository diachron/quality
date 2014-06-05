package forth.diachron.qualitymetrics;

import java.util.List;
import java.util.Vector;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
/**
 * @author Ioannis Chrysakis
 * 
 */
//This class contains methods for handling query results from virtuoso-sesame infrastructure
public class SesameQueryHandler {

	public int countTupleQueryResult(RepositoryConnection con, String query)
	 
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
	

	public Value[][] doTupleQuery(RepositoryConnection con, String query) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
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
