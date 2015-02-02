package eu.diachron.qualitymetrics.utilities;

import java.io.File;
import java.io.FileInputStream;
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
 * Responsible for reading vocabulary (Models) from Web. Stores vocabulary in
 * memory and file cache.
 * 
 * To enable file cache create a directory with name 'models' from
 * quality-extension/resources/models. To disable file cache delete
 * directory with name 'models' from quality-extension/resources/models.
 */
@Deprecated
public class VocabularyReader {
  private static final Logger LOG = Logger.getLogger(VocabularyReader.class);
  private static Hashtable<String, Model> vocabularies = new Hashtable<String, Model>();

  /**
   * Reads vocabulary from given URL or cache.
   * @param url AN URL to read from.
   */
  public static Model read(String url) {
    Model model = ModelFactory.createDefaultModel();
    if (url == null) {
      return model;
    }
    try {
      if (vocabularies.containsKey(url)) {
        model = vocabularies.get(url);
      } else if (modelFileExists(url)) {
        model = readModelFromFile(url);
        vocabularies.put(url, model);
      } else {
        model.read(url);
        vocabularies.put(url, model);
        writeModelToFile(model, url);
      }
    } catch (RiotException e) {
      LOG.error(e.getLocalizedMessage());
    } catch (HttpException e) {
      LOG.error(e.getLocalizedMessage());
    } catch (MalformedURIException e) {
      LOG.error(e.getLocalizedMessage());
    } catch (IOException e) {
      LOG.error(e.getLocalizedMessage());
    }
    return model;
  }

  private static boolean modelFileExists(String url) throws MalformedURIException {
    return new File(getAbsPath(url)).exists() ? true : false;
  }

  /**
   * Reads a model from a file.
   * @throws IOException if the file is not found.
   */
  private static Model readModelFromFile(String url) throws IOException {
    Model model = ModelFactory.createDefaultModel();
    FileInputStream file = new FileInputStream(getAbsPath(url));
    model.read(file, url);
    file.close();
    return model;
  }

  private static void writeModelToFile(Model model, String url) throws IOException {
    if ((new File(Constants.LOADED_MODELS)).exists()) {
      createDirs(url);
      FileOutputStream file = new FileOutputStream(getAbsPath(url));
      model.write(file);
      file.close();
    }
  }

  private static String getAbsPath(String url) throws MalformedURIException {
    return Constants.LOADED_MODELS + File.separator + getFileNameFromUrl(url) + ".rdf";
  }

  /**
   * Creates directories for given url.
   */
  private static void createDirs(String url) throws MalformedURIException {
    if (url != null) {
      URI tmpUrl = new URI(url);
      if (tmpUrl.getHost() != null) {
        new File(Constants.LOADED_MODELS + File.separator + tmpUrl.getHost()).mkdirs();
      }
    }
  }

  private static String getFileNameFromUrl(String url) throws MalformedURIException {
    String fileName = "";
    if (url != null) {
      URI uri = new URI(url);
      if (uri.getHost() != null) {
        fileName += uri.getHost();
      }
      if (uri.getPath() != null) {
        fileName += "/" + uri.getPath(false, false).replace('/', '_');
      }
    }
    return fileName;
  }

  public static void clear() {
    vocabularies.clear();
  }
}
