/**
 * 
 */
package eu.diachron.qualitymetrics.representational.utils;

import java.util.UUID;

import org.mapdb.HTreeMap;

import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;

/**
 * @author Jeremy Debattista
 * 
 * This is a singleton utiliterian class that holds datastructures 
 * for the Representational Category metrics that can be shared
 * either between metrics or between instances of the same metric
 * (i.e. calculating different datasets) during a single execution 
 * of the Luzzu Framework.
 * 
 */
public class SharedResources {

	private static SharedResources instance = null;
	
	private HTreeMap<String, Boolean> clsPropUndefined = MapDbFactory.getSingletonFileInstance(true).createHashMap(UUID.randomUUID().toString()).make();
	
	protected SharedResources(){}
	
	public static SharedResources getInstance(){
		if (instance == null) instance = new SharedResources();
		return instance;
	}
	
	public Boolean classOrPropertyDefined(String uri){
		if (this.clsPropUndefined.containsKey(uri)) 
			return this.clsPropUndefined.get(uri);
		else 
			return null;
	}
	
	public void addClassOrProperty(String uri, Boolean defined){
		this.clsPropUndefined.putIfAbsent(uri, defined);
	}
}
