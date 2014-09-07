/**
 * 
 */
package de.unibonn.iai.eis.diachron.io.utilities;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Carlos
 * 
 */
public class UtilMail {
	/**
	 * 
	 * @param to
	 */
	public static void sendMail(String to) {
		try {

			// Establish a network connection for sending mail
			URL u = new URL("mailto:" + to); // Create a mailto: URL
			URLConnection c = u.openConnection(); // Create a URLConnection for
													// it
			c.setDoInput(false); // Specify no input from this URL
			c.setDoOutput(true); // Specify we'll do output
			System.out.println("Connecting..."); // Tell the user what's
													// happening
			System.out.flush(); // Tell them right now
			c.connect(); // Connect to mail host
			PrintWriter out = // Get output stream to mail host
			new PrintWriter(new OutputStreamWriter(c.getOutputStream()));

			// Write out mail headers. Don't let users fake the From address
			out.println("From: \" eisLab@uni-bonn.de  \" <"
					+ System.getProperty("user.name") + "@"
					+ InetAddress.getLocalHost().getHostName() + ">");
			out.println("To: " + to);
			out.println("Subject: Proccess Finish");
			out.println(); // blank line to end the list of headers

			// Now ask the user to enter the body of the message
			System.out.println("Enter the message. "
					+ "End with a '.' on a line by itself.");
			// Read message line by line and send it out.
			String line;

			line = "The process is already finish, you can check now in the web site of the QUENTLOD lab";

			out.println(line);

			// Close the stream to terminate the message
			out.close();
			// Tell the user it was successfully sent.
			System.out.println("Message sent.");
			System.out.flush();
		} catch (Exception e) { // Handle any exceptions, print error message.
			System.err.println(e);
			System.err.println("Usage: java SendMail [<mailhost>]");
		}
	}
}
