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
	
	public int countSC (){
		return countSimpleChanges ("","");
	}
	
	public int countSimpleChanges (String old_version_uri, String new_version_uri){
		int result = 0;
		if (old_version_uri == null || old_version_uri.equals("")){
			old_version_uri = VirtuosoHandler.old_version_uri;
		}
		if (new_version_uri == null || new_version_uri.equals("")){
			new_version_uri = VirtuosoHandler.new_version_uri;
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
		//System.out.println("countDeltas_sparqlQuery:"+sparqlQuery);
		RepositoryConnection con = virhan.getVirtuosoConnection();
		result = seshan.countTupleQueryResult (con,sparqlQuery);
		System.out.println("countDeltas between:"+old_version_uri+" vs. "+new_version_uri +"\nDeltas:"+result);
		return result;
	}
	
}
