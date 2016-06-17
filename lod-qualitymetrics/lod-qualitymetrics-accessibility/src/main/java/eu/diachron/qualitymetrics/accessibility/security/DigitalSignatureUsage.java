/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.security;

import java.io.Serializable;
import java.util.UUID;

import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;

import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import eu.diachron.qualitymetrics.utilities.AbstractQualityMetric;

/**
 * @author Jeremy Debattista
 * 
 */
public class DigitalSignatureUsage extends AbstractQualityMetric {

	private static final Resource ENDORSEMENT = ModelFactory.createDefaultModel().createResource("http://xmlns.com/wot/0.1/Endorsement");
	private static final Property ASSURANCE = ModelFactory.createDefaultModel().createProperty("http://xmlns.com/wot/0.1/assurance");
	private static final Property ENDORSER = ModelFactory.createDefaultModel().createProperty("http://xmlns.com/wot/0.1/endorser");

	private static DB mapDb = MapDbFactory.getMapDBAsyncTempFile();

	private HTreeMap<String,DigitalSignature> docs = MapDbFactory.createHashMap(mapDb, UUID.randomUUID().toString());
	private HTreeMap<String,DigitalSignature> endorsements = MapDbFactory.createHashMap(mapDb, UUID.randomUUID().toString());
	
	private static Logger logger = LoggerFactory.getLogger(DigitalSignatureUsage.class);
	
	@Override
	public void compute(Quad quad) {
		logger.debug("Computing : {} ", quad.asTriple().toString());
	
		Node subject = quad.getSubject();
		Node object = quad.getObject();
		
		if (quad.getObject().getURI().equals(FOAF.Document.getURI())){
			docs.putIfAbsent(subject.toString(), new DigitalSignature());
		}
		
		if (quad.getPredicate().getURI().equals(ASSURANCE.getURI())){
			DigitalSignature ds ;
			if (endorsements.containsKey(object.getURI())){
				ds = endorsements.get(object.getURI());
			} else {
				ds = new DigitalSignature();
				ds.endorcement = object.getURI();
			}
			
			ds.assurance = subject.getURI();
			
			docs.put(subject.toString(), ds);
			endorsements.put(object.getURI(), ds);

		}
		
		if (quad.getPredicate().getURI().equals(ENDORSER.getURI())){
			DigitalSignature ds ;
			if (endorsements.containsKey(object.getURI())){
				ds = endorsements.get(object.getURI());
			} else {
				ds = new DigitalSignature();
				ds.endorcement = subject.getURI();
			}
			
			ds.endorcer = object.getURI();
		}
		
		if (quad.getObject().getURI().equals(ENDORSEMENT.getURI())){
			DigitalSignature ds = new DigitalSignature();
			ds.endorcement = subject.getURI();
			
			endorsements.putIfAbsent(subject.getURI(), ds);
		}
	}

	@Override
	public double metricValue() {
		double noDocs = this.docs.size();
		double noDocsWithoutEndorcement = 0.0;
		
		for(DigitalSignature ds : this.docs.values()) noDocsWithoutEndorcement += (ds.fullEndorcement()) ? 1 : 0;

		statsLogger.info("DigitalSignatureUsage. Dataset: {} - Total # Documents : {}; # Documents without Endorcement : {};", 
				EnvironmentProperties.getInstance().getDatasetURI(), noDocs, noDocsWithoutEndorcement);
		
		return (noDocsWithoutEndorcement / noDocs);
	}

	@Override
	public Resource getMetricURI() {
		return DQM.DigitalSignatureUsage;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		return null;
	}

	@Override
	public boolean isEstimate() {
		return false;
	}

	@Override
	public Resource getAgentURI() {
		return DQM.LuzzuProvenanceAgent;
	}

	private class DigitalSignature implements Serializable {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -2185956592313988605L;
		private String endorcement = null;
		private String endorcer = null;
		private String assurance = null;
		
		
		public boolean fullEndorcement(){
			if ((endorcement == null) || (endorcer == null) || (assurance == null)) return false;
			return true;
		}
		
		
		@Override
		public int hashCode() {
			return endorcement.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof DigitalSignature){
				DigitalSignature other = (DigitalSignature) obj;
				return this.endorcement.equals(other.endorcement);
			}
			return false;
		}



	}
}
