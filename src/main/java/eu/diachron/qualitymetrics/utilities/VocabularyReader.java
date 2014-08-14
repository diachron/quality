package eu.diachron.qualitymetrics.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.riot.RiotException;
import org.apache.log4j.Logger;
import org.apache.xerces.util.URI;
import org.apache.xerces.util.URI.MalformedURIException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
/**
 * Responsible for reading vocabulary (Models) from Web.
 * Stores vocabulary in memory and file cache.
 * 
 * Note : file cache path ..src/main/resources/models
 * 
 * to enable file cache create a directory with name 'models' from ../src/main/resources.
 * to disable file cache delete directory with name 'models' from ../src/main/resources.
 * 
 * @author Muhammad Ali Qasmi
 * @date 13th March 2014
 */
public final class VocabularyReader {
	
	private static String modelsFolderPath = "src/main/resources/models";
	
    private static Logger logger = Logger.getLogger(VocabularyReader.class);
	private static Hashtable<String, Model>vocabularies = new Hashtable<String, Model>();
	
	/**
	 * Creates directory for given url
	 * 
	 * @param url
	 * @return
	 */
	private static void createDir(String url){
	        try {
	                if (url != null) {
                        URI tmpUrl = new URI(url);
                        if (tmpUrl.getHost() != null) {
                                new File(modelsFolderPath + "/" +tmpUrl.getHost())
                                .mkdirs();
                        }
                    }
            } catch (MalformedURIException e) {
                    logger.debug(e.getStackTrace());
                    logger.debug(e.getMessage());
            }
	}
	
	/**
	 * Converts url into a format that can be used as file name.
	 * 
	 * @param url
	 * @return url as fileName
	 */
	private static String urlAsFileName(String url) {
	        String fileName = "";
	        try {
	                if (url != null) {
                        URI tmpUrl = new URI(url);
                        if (tmpUrl.getHost() != null) {
                                fileName += tmpUrl.getHost(); 
                        }
                        if (tmpUrl.getPath() != null ){
                                fileName += "/" + tmpUrl.getPath(false,false).replace('/', '_');
                        }
	                }
	        } catch (MalformedURIException e) {
	                fileName = "";
	                logger.debug(e.getStackTrace());
                    logger.debug(e.getMessage());
            }
	        return fileName;
	}
	/**
	 * Writes contents of model to file
	 */
	private static void writeModelToFile(Model model, String url){
	        try {
	                if ((new File(modelsFolderPath)).exists()){
    	                VocabularyReader.createDir(url);
    	                String tmpFullPath = VocabularyReader.modelsFolderPath + "/" + VocabularyReader.urlAsFileName(url) + ".rdf";
    	                FileOutputStream fileOut = new FileOutputStream(tmpFullPath);
    	                model.write(fileOut);
    	                fileOut.close();
	                }
                } catch (FileNotFoundException e) {
                        logger.debug(e.getStackTrace());
                        logger.debug(e.getMessage());
                } catch (IOException e) {
                        logger.debug(e.getStackTrace());
                        logger.debug(e.getMessage());
                }
    }
	
	
	/**
	 * Reads contents model from file
	 */
	private static Model readModelFromFile(String url) {
	        Model tmpModel = null;
	        try {
	                tmpModel = ModelFactory.createDefaultModel();
	                String tmpFullPath = VocabularyReader.modelsFolderPath + "/" + VocabularyReader.urlAsFileName(url) + ".rdf";     
                    FileInputStream fileIn = new FileInputStream(tmpFullPath);
                    tmpModel.read(fileIn, url);
                    fileIn.close();
                } catch (FileNotFoundException e) {
                        tmpModel = null;
                        logger.debug(e.getStackTrace());
                        logger.error(e.getMessage());
                } catch (IOException e) {
                        tmpModel = null;
                        logger.debug(e.getStackTrace());
                        logger.error(e.getMessage());
                        
                }
            return tmpModel;
	}
	
	/**
	 * Checks if content of given url exists in file cache
	 * @param url
	 * @return true - if file exists
	 */
	private static boolean hasModelInFile(String url) {
	        String tmpFullPath = VocabularyReader.modelsFolderPath + "/" + VocabularyReader.urlAsFileName(url) + ".rdf";  
	        File file = new File(tmpFullPath);
	        return (file.exists()) ? true : false;
	}
	
	/**
	 * Clears all content from the vocabulary hash table
	 */
	public static void clear(){
		VocabularyReader.vocabularies.clear();
	}
	
	/**
	 * Reads vocabulary from given URL or cache
	 * 
	 * @param url
	 * @return model
	 */
	public static Model read(String url){
		Model model = null;
        try {
	        if (VocabularyReader.vocabularies.containsKey(url)){
	            // load vocabularies from memory    
	        	logger.debug(url + " :: vocabulary loaded from memory cache.");
	        	model = VocabularyReader.vocabularies.get(url);
	        } else if (VocabularyReader.hasModelInFile(url)){
	            // load vocabularies from file    
	            logger.debug(url + " :: vocabulary loaded from file cache.");
                model = VocabularyReader.readModelFromFile(url);
                if (model != null) {
                        VocabularyReader.vocabularies.put(url, model);
                }
	        }
	        else {
	            if (VocabularyReader.vocabularies.containsKey(url)){
                    return VocabularyReader.vocabularies.get(url);        
	            }
	            // load vocabularies from web    
	        	logger.debug(url + " :: vocabulary loaded from web.");
	        	model = ModelFactory.createDefaultModel();
	        	model.read(url);
	        	// store model in file
	        	VocabularyReader.vocabularies.put(url, model);
	        	// write to file
	        	VocabularyReader.writeModelToFile(model, url);
	        }
        }
        catch (RiotException roitException){
        	logger.debug(url + " :: unable to loaded vocabulary from web.");
        	logger.debug(roitException);
        	logger.error(roitException.getMessage());
        	return null;
        }
        catch (HttpException httpException){
        	logger.debug(url + " :: unable to loaded vocabulary from web.");
        	logger.debug(httpException);
        	logger.error(httpException.getMessage());
        	return null;
        }
        catch (NullPointerException nullPointerException){
        	logger.debug(url + " :: unable to loaded vocabulary from web.");
        	logger.debug(nullPointerException);
        	logger.error(nullPointerException.getMessage());
        	return null;
        }
        return model;
	}
}
