/**
 * 
 */
package de.unibonn.iai.eis.diachron.webinterface.beans;

import java.io.Serializable;

import de.unibonn.iai.eis.diachron.io.streamprocessor.Consumer;
import de.unibonn.iai.eis.diachron.io.streamprocessor.Producer;
import de.unibonn.iai.eis.diachron.io.streamprocessor.StreamManager;
/**
 * @author Carlos
 * 
 */
public class ProcessDataSetBean implements Serializable {

	// ////////////////////////////////////////////////////////////////////////
	// Serial version UID
	// ////////////////////////////////////////////////////////////////////////
	/** */
	private static final long serialVersionUID = 5040929916098311761L;
	/** */
	private static int INCREMENT = 10000;
	/** */
	private String mail;
	/** */
	private String url;
	
	public String runProcess(){
		StreamManager streamQuads = new StreamManager();
		Producer p1 = new Producer(streamQuads, INCREMENT, this.url);
		Consumer c1 = new Consumer(streamQuads, p1);
		c1.setMail(this.mail);
		p1.start();
		c1.start();
		
		/*while( c1.isRunning() ){
			System.out.print("");
		}*/
		
		System.out.println("The value of the metrics ");
		return "";
	}
	
	/**
	 * @return the mail
	 */
	public String getMail() {
		return mail;
	}

	/**
	 * @param mail the mail to set
	 */
	public void setMail(String mail) {
		this.mail = mail;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}
}
