package de.unibonn.iai.eis.diachron.commons.bigdata;

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
			.compressionEnable()
			.mmapFileEnable()
			.asyncWriteEnable()
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
		
		DB mapDB = DBMaker.newFileDB(new File(mapDbDir + "mapasyncdb-" + timestamp)).
				asyncWriteEnable().
				asyncWriteFlushDelay(500).
				transactionDisable().
				closeOnJvmShutdown().
				deleteFilesAfterClose().
				make();
		
		return mapDB;
	}

}
