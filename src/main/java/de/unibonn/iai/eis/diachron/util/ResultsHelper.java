package de.unibonn.iai.eis.diachron.util;

import java.beans.XMLEncoder;
import java.beans.XMLDecoder;
import java.io.*;

public class ResultsHelper {
	public static void write(ResultDataSet f, String filename) throws Exception {
			XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(
					new FileOutputStream(filename)));
			encoder.writeObject(f);
			encoder.close();

	}

	public static ResultDataSet read(String filename) throws Exception {
		if(new File(filename).exists()){

			XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(
					new FileInputStream(filename)));
			ResultDataSet o = (ResultDataSet) decoder.readObject();
			decoder.close();
			return o;
		}
		else{
			return new ResultDataSet();
		}
	}
}