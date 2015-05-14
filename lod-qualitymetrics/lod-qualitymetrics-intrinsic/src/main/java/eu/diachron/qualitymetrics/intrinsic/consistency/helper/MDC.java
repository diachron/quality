/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.consistency.helper;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Jeremy Debattista
 * 
 */
public class MDC implements Serializable {
	private static final long serialVersionUID = 2235605621422072042L;
	public String subject;
	public Set<String> objects;
	
	public MDC(String subject, String object){
		this.subject = subject;
		this.objects = new HashSet<String>();
		this.objects.add(object);
	}
}
