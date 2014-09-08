/**
 * 
 */
package eu.diachron.qualitymetrics.cache;

import de.unibonn.iai.eis.luzzu.cache.CacheManager;

/**
 * @author Jeremy Debattista
 * 
 * This class communicates with Luzzu's Cache Manager,
 * storing resources which might be used in the future.
 * 
 */
public class DiachronCacheManager {

	public static final String HTTP_RESOURCE_CACHE = "http_resource_cache";
	
	private static DiachronCacheManager instance = null;
	private CacheManager luzzuCM = CacheManager.getInstance();
	
	protected DiachronCacheManager(){
		luzzuCM.createNewCache(HTTP_RESOURCE_CACHE, 5000);
	};
	
	public static DiachronCacheManager getInstance(){
		if (instance == null) {
			instance = new DiachronCacheManager();
		}
		return instance;
	}
	
	public void addToCache(String cacheName, String key, Object value){
		luzzuCM.addToCache(cacheName, key, value);
	}
	
	public boolean existsInCache(String cacheName, String key){
		return luzzuCM.existsInCache(cacheName, key);
	}
	
	public Object getFromCache(String cacheName, String key){
		return luzzuCM.getFromCache(cacheName, key);
	}
}
