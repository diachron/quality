/**
 * 
 */
package de.unibonn.iai.eis.diachron.qualitymetrics.trust.reputation.util;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * @author Carlos Montoya
 * 
 */
public class ReputationHelper {
	/**
	 * This method serialize into the specify file a class, using XMLEncoder
	 * @param f object to be serialized
	 * @param filename name of the file to created or replace the info
	 * @throws Exception
	 */
	public static void write(ReputationUtil f, String filename) throws Exception {
		XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(
				new FileOutputStream(filename)));
		encoder.writeObject(f);
		encoder.close();

	}

	/**
	 * Read from the filename the object and return the object
	 * @param filename name of the file
	 * @return the object that were store into the file.
	 * @throws Exception
	 */
	public static ReputationUtil read(String filename) throws Exception {
		if (new File(filename).exists()) {

			XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(
					new FileInputStream(filename)));
			ReputationUtil o = (ReputationUtil) decoder.readObject();
			decoder.close();
			return o;
		} else {
			return new ReputationUtil();
		}
	}
}
