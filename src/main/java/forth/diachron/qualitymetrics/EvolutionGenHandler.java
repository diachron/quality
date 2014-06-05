package forth.diachron.qualitymetrics;

import org.openrdf.repository.RepositoryConnection;

import forth.diachron.connectivity.VirtuosoHandler;

/**
 * @author Ioannis Chrysakis
 * 
 */
//This class contains generic functions applied for evolution metrics
public class EvolutionGenHandler {
private VirtuosoHandler virhan = new VirtuosoHandler();
private SesameQueryHandler seshan = new SesameQueryHandler();
	
	public int countDeltas (){
		return countDeltas ("","");
	}
	
	public int countDeltas (String old_version_uri, String new_version_uri){
		if (old_version_uri == null || old_version_uri.equals("")){
			old_version_uri = VirtuosoHandler.old_version_uri;
		}
		if (new_version_uri == null || new_version_uri.equals("")){
			new_version_uri = VirtuosoHandler.old_version_uri;
		}
		
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
		
		RepositoryConnection con = virhan.getVirtuosoConnection();
		return seshan.countTupleQueryResult (con,sparqlQuery);
	}
	
}
