/**
 * 
 */
package de.unibonn.iai.eis.diachron.qualitymetrics.utilities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * @author Carlos
 * 
 */
public class ConfigurationLoader {
	
	public static final String COVERAGE_FILE = "coverage.properties";
	public static final String CONFIGURATION_FILE = "config.properties";
	
	/**
	 * This method read from a local file the directory where is saved the
	 * Dataset processed
	 * 
	 * @return The path of the file in the server
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	public List<String> loadAttributes(String fileName) {
		try {
			List<String> ret = new ArrayList<String>();
			Properties prop = new Properties();
			String propFileName = fileName;

			InputStream inputStream = getClass().getClassLoader()
					.getResourceAsStream(propFileName);

			prop.load(inputStream);

			if (inputStream == null) {
				throw new FileNotFoundException("property file '"
						+ propFileName + "' not found in the classpath");
			}
			// get;

			Enumeration em = prop.keys();
			while (em.hasMoreElements()) {
				String str = (String) em.nextElement();
				System.out.println(str + ": " + prop.get(str));
				ret.add(prop.get(str).toString());
			}
			return ret;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * This method read from a local file the directory where is saved the Dataset processed
	 * @return The path of the file in the server
	 * @throws IOException
	 */
	public String loadDataBase(String fileName) throws IOException {

		String result = "";
		Properties prop = new Properties();
		String propFileName = fileName;

		InputStream inputStream = getClass().getClassLoader()
				.getResourceAsStream(propFileName);
		prop.load(inputStream);
		if (inputStream == null) {
			throw new FileNotFoundException("property file '" + propFileName
					+ "' not found in the classpath");
		}

		// get the property value and print it out
		String dataBase = prop.getProperty("dataBase");

		result = dataBase;
		return result;
	}
	
	/**
	 * This method read from a local file the directory where is saved the Dataset processed
	 * @return The path of the file in the server
	 * @throws IOException
	 */
	public String loadMailDefault(String fileName) throws IOException {

		String result = "";
		Properties prop = new Properties();
		String propFileName = fileName;

		InputStream inputStream = getClass().getClassLoader()
				.getResourceAsStream(propFileName);
		prop.load(inputStream);
		if (inputStream == null) {
			throw new FileNotFoundException("property file '" + propFileName
					+ "' not found in the classpath");
		}

		// get the property value and print it out
		String dataBase = prop.getProperty("defaultMail");

		result = dataBase;
		return result;
	}
	

	public String loadByKey(String key, String fileName) throws IOException{
		String result = "";
		Properties prop = new Properties();
		String propFileName = fileName;

		InputStream inputStream = getClass().getClassLoader()
				.getResourceAsStream(propFileName);
		prop.load(inputStream);
		if (inputStream == null) {
			throw new FileNotFoundException("property file '" + propFileName
					+ "' not found in the classpath");
		}

		// get the property value and print it out
		String dataBase = prop.getProperty(key);

		result = dataBase;
		return result;
	}
}
