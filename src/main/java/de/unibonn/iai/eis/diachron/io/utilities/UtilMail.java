/**
 * 
 */
package de.unibonn.iai.eis.diachron.io.utilities;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.ConfigurationLoader;

/**
 * @author Carlos
 * 
 */
public class UtilMail {

	static String username, contrasena;
	static Properties p;
	static Session sesion;

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

	private static void setup() throws MessagingException, IOException {
			
		ConfigurationLoader conf = new ConfigurationLoader();
		// datos de conexion
		username = conf.loadByKey("userResponsibleMail",ConfigurationLoader.CONFIGURATION_FILE);
		contrasena = conf.loadByKey("passResponsibleMail",ConfigurationLoader.CONFIGURATION_FILE);
		// propiedades de la conexion
		p = new Properties();
		p.put("mail.smtp.auth", "true");
		p.put("mail.smtp.starttls.enable", "true");
		p.put("mail.smtp.host", "smtp.gmail.com");
		p.put("mail.smtp.port", "587");

		// creamos la sesion
		sesion = crearSesion();
	}

	private static Session crearSesion() {
		
		Session session = Session.getInstance(p,
				new javax.mail.Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, contrasena);
					}
				});
		return session;
	}

	public static void sendMail(String to, String subject, String content) throws MessagingException, IOException {
		setup();
		// Construimos el Mensaje
		Message mensaje = new MimeMessage(sesion);
		try {
			mensaje.setRecipient(Message.RecipientType.TO, new InternetAddress(
					to));
			mensaje.setSubject(subject);
			mensaje.setText(content);
			// Enviamos el Mensaje
			Transport.send(mensaje);
			System.out.println("Mail sent it to: " + to);
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}
	}
}
