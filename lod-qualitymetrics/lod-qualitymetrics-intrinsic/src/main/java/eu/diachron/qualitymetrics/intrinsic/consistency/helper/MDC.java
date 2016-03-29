/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.consistency.helper;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;


/**
 * @author Jeremy Debattista
 * 
 */
public class MDC implements Serializable {
	private static final long serialVersionUID = 2235605621422072042L;
	public Resource subject;
	public Set<RDFNode> objects;
	
	public MDC(Resource subject, RDFNode object){
		this.subject = subject;
		this.objects = new HashSet<RDFNode>();
		this.objects.add(object);
	}
	
	public MDC(Resource subject){
		this.subject = subject;
		this.objects = new HashSet<RDFNode>();
	}


	@Override
	public int hashCode() {
		if (this.subject == null) return 0;
		return this.subject.hashCode();
	}
	
	@Override
    public boolean equals(Object obj) {
       if (!(obj instanceof MDC))
            return false;
        if (obj == this)
            return true;

        return (this.subject.getURI().equals(((MDC)obj).subject.getURI()));
    }
	
}

