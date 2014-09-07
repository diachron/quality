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
	/**
	 * This method read from a local file the directory where is saved the
	 * Dataset processed
	 * 
	 * @return The path of the file in the server
	 * @throws IOException
	 */
	public List<String> loadAttributes() {
		try {
			List<String> ret = new ArrayList<String>();
			Properties prop = new Properties();
			String propFileName = "coverage.properties";

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
}
