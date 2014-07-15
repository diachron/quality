package forth.diachron.connectivity;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import virtuoso.sesame2.driver.VirtuosoRepository;
/**
 * @author Ioannis Chrysakis
 * 
 */
public class VirtuosoHandler {

	// Virtuoso DB info
	public static final String VIRTUOSO_INSTANCE = "83.212.122.116";
	public static final int VIRTUOSO_PORT = 1111;
	public static final String VIRTUOSO_USERNAME = "dba";
	public static final String VIRTUOSO_PASSWORD = "dba";
	public static String detectedChangesGraph = "http://ontological/copy";
	
	//default values for versions
	public static String old_version_uri = "http://www.diachron-fp7.eu/resource/recordset/efo/2.46";
	public static String new_version_uri = "http://www.diachron-fp7.eu/resource/recordset/efo/2.47";
	
	
	public RepositoryConnection getVirtuosoConnection () {

		Repository repository = new VirtuosoRepository("jdbc:virtuoso://" + VIRTUOSO_INSTANCE + ":" + VIRTUOSO_PORT, VIRTUOSO_USERNAME, VIRTUOSO_PASSWORD);
		RepositoryConnection con = null;
		try {
		
			con = repository.getConnection();
			con.setAutoCommit(true);
			return con;
		} catch (RepositoryException e) {
			System.out.println("Virtuoso Repository Connection Error:" +e.getMessage());
			return null;
		}
	}
	
}
