/**
 * 
 */
package de.unibonn.iai.eis.diachron.io.utilities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Carlos
 *
 */
public class ConfigurationLoader {
	/**
	 * This method read from a local file the directory where is saved the Dataset processed
	 * @return The path of the file in the server
	 * @throws IOException
	 */
	public String loadDataBase() throws IOException {

		String result = "";
		Properties prop = new Properties();
		String propFileName = "config.properties";

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
	public String loadMailDefault() throws IOException {

		String result = "";
		Properties prop = new Properties();
		String propFileName = "config.properties";

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
}
