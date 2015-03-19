/**
 * 
 */
package eu.diachron.qualitymetrics.cache;

import de.unibonn.iai.eis.luzzu.cache.CacheManager;
import de.unibonn.iai.eis.luzzu.cache.CacheObject;

/**
 * @author Jeremy Debattista
 * 
 * This class communicates with Luzzu's Cache Manager,
 * storing resources which might be used in the future.
 * 
 */
public class DiachronCacheManager {

	public static final String HTTP_RESOURCE_CACHE = "http_resource_cache";
	public static final String VOCABULARY_CACHE = "vocabulary_cache";
//	public static final String DATASET_CACHE = "dataset_cache";
	
	private static DiachronCacheManager instance = null;
	private CacheManager luzzuCM = CacheManager.getInstance();
	
	protected DiachronCacheManager(){
		luzzuCM.createNewCache(HTTP_RESOURCE_CACHE, 5000);
		luzzuCM.createNewCache(VOCABULARY_CACHE, 5000);
//		luzzuCM.createNewCache(DATASET_CACHE, 5000);
	};
	
	public static DiachronCacheManager getInstance(){
		if (instance == null) {
			instance = new DiachronCacheManager();
		}
		return instance;
	}
	
	public void addToCache(String cacheName, String key, CacheObject value){
		luzzuCM.addToCache(cacheName, key, value);
	}
	
	public boolean existsInCache(String cacheName, String key){
		return luzzuCM.existsInCache(cacheName, key);
	}
	
	public Object getFromCache(String cacheName, String key){
		return luzzuCM.getFromCache(cacheName, key);
	}
}
