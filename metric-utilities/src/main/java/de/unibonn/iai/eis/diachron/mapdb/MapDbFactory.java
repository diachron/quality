package de.unibonn.iai.eis.diachron.mapdb;

import java.io.File;
import java.util.Date;
import java.util.Set;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

/**
 * Creates and properly sets up instances of mapdb databases, stored in the filesystem.
 * @author slondono
 *
 */
public class MapDbFactory {
	
	private static String mapDbDir = "/tmp/";
	
	/**
	 * Sets the directory where all mapdb database files will be stored
	 * @param mapDbDirectory Path of the directory to store all the mapdb files
	 */
	public static void setMapDbDirectory(String mapDbDirectory) {
		mapDbDir = mapDbDirectory + ((mapDbDirectory.endsWith("/"))?(""):("/"));
	}
	
	
	private static DB singletonAsync = null;
	private static DB singleton = null;
	
	public static DB getMapDBAsyncTempFile(){
		if (singletonAsync == null){
			singletonAsync = createAsyncFilesystemDB();
		}
		return singletonAsync;
	}
	
	public static DB getMapDBTempFile(){
		if (singleton == null){
			singleton = createFilesystemDB();
		}
		return singleton;
	}
	
	
	public static <T1,T2> HTreeMap<T1,T2> createHashMap(DB mapDBFile, String name){
		HTreeMap<T1,T2> _htmap = null;
		synchronized (mapDBFile) {
			_htmap = mapDBFile.createHashMap(name).make();
		}
		return _htmap;
	}
	
	public static <T> Set<T> createHashSet(DB mapDBFile, String name){
		Set<T> _hashSet = null;
		synchronized (mapDBFile) {
			_hashSet = mapDBFile.createHashSet(name).make();
		}
		return _hashSet;
	}
	
	/**
	 * Creates a new database, stored in memory (more preciselly, in heap space)
	 */
	public static DB createHeapDB() {
		DB mapDB = 	DBMaker.newHeapDB().make();	
		return mapDB;
	}
		
	
	
	//TODO: Change to private
	/**
	 * Creates a new database, stored as a file
	 */
	public static DB createFilesystemDB() {
		
		String timestamp = null;
		
		synchronized(MapDbFactory.class) {
			timestamp = (new Long((new Date()).getTime())).toString();
		}
		
		DB mapDB = DBMaker.newFileDB(new File(mapDbDir + "mapdb-" + timestamp))
		.closeOnJvmShutdown()
		.deleteFilesAfterClose()
		.transactionDisable()
		.mmapFileEnable()
		.asyncWriteFlushDelay(2000)
		.make();
		
		return mapDB;
	}
	
	
	//TODO: Change to private
	/**
	 * Creates a new database, with async settings
	 */
	public static DB createAsyncFilesystemDB() {
		
		String timestamp = null;
		
		synchronized(MapDbFactory.class) {
			timestamp = (new Long((new Date()).getTime())).toString();
		}
		
		DB mapDB = DBMaker.newFileDB(new File(mapDbDir + "mapasyncdb-" + timestamp))
		.closeOnJvmShutdown()
		.deleteFilesAfterClose()
		.transactionDisable()
		.mmapFileEnable()
		.asyncWriteFlushDelay(2000)
		.asyncWriteEnable()
		.make();
		
		return mapDB;
	}
	

}
