/**
 * 
 */
package de.unibonn.iai.eis.diachron.io;

import de.unibonn.iai.eis.diachron.io.streamprocessor.Consumer;
import de.unibonn.iai.eis.diachron.io.streamprocessor.Producer;
import de.unibonn.iai.eis.diachron.io.streamprocessor.StreamManager;
import de.unibonn.iai.eis.diachron.io.utilities.Menus;


/**
 * This class is the main class, it is in charge to load the data and create the
 * processes and clients to run faster.
 * 
 * @author Carlos Montoya
 */
public class Main {

	/**
	 * Variable to adjust the increment value of the call to the service
	 */
	private static int INCREMENT = 10000;

	private static String serviceUrl = "http://protein.bio2rdf.org/sparql";

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		//int opt1 = Menus.menuMain();

		//String url = serviceUrl;
		
		String url = Menus.menuUrl();
		serviceUrl = url;

		String mail = Menus.menuMail();
		
		StreamManager streamQuads = new StreamManager();
		Producer p1 = new Producer(streamQuads, INCREMENT, serviceUrl);
		Consumer c1 = new Consumer(streamQuads, p1);
		c1.setMail(mail);
		p1.start();
		c1.start();
		
		/*while( c1.isRunning() ){
			System.out.print("");
		}*/
		
		System.out.println("The value of the metrics ");

		//writeFile(streamQuads);
	}
}

