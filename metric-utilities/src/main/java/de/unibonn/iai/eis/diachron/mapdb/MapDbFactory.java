package de.unibonn.iai.eis.diachron.mapdb;

import java.io.File;
import java.util.Date;

import org.mapdb.DB;
import org.mapdb.DBMaker;

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
	
	/**
	 * Creates a new database, stored in memory (more preciselly, in heap space)
	 */
	public static DB createHeapDB() {
		DB mapDB = 	DBMaker.newHeapDB().make();	
		return mapDB;
	}
		
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
		.asyncWriteFlushDelay(500)
		.make();
		
		return mapDB;
	}
	
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
		.asyncWriteFlushDelay(500)
		.asyncWriteEnable()
		.make();
		
		return mapDB;
	}
	
	private static DB singleton = null;
	public static DB getSingletonFileInstance(boolean async){
		if (singleton == null){
			if (async) singleton = MapDbFactory.createAsyncFilesystemDB();
			else singleton = MapDbFactory.createFilesystemDB();
		}
		
		return singleton;
	}

}
