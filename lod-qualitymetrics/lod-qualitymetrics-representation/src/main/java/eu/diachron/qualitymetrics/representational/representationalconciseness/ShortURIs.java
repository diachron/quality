package eu.diachron.qualitymetrics.representational.representationalconciseness;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;

/**
 * @author Santiago Londono
 * Detects long URIs or those that contains query parameters, thereby providing a 
 * measure of how compactly is information represented in the dataset
 * 
 * The W3C best practices for URIs say that a URI (including scheme) should be at max 80 characters
 * http://www.w3.org/TR/chips/#uri.
 * Parameterised URIs are considered bad immediately.
 * 
 * Value returns a ratio of the total number of short uris in relation to the
 * number of local URIs of a dataset
 *
 */
public class ShortURIs implements QualityMetric {
	
	private static Logger logger = LoggerFactory.getLogger(ShortURIs.class);
	
	private final Resource METRIC_URI = DQM.ShortURIsMetric;
	
	private Set<String> seenSet = MapDbFactory.createFilesystemDB().createHashSet("seen-set").make();

	
	/**
	 * Sum short uri's
	 */
	private long shortURICount = 0;
	
	/**
	 * Counts the total number of possible dereferenceable URIs defined in the dataset
	 */
	private long countLocalDefURIs = 0;
	
	private List<Quad> _problemList = new ArrayList<Quad>();

	
	public void compute(Quad quad) {
		if (!(quad.getPredicate().hasURI(RDF.type.getURI()))){
			Node subject = quad.getSubject();
			if ((!(subject.isBlank())) && (!(this.seenSet.contains(subject.getURI())))){
				if (subject.isURI()){
					if (possibleDereferenceableURI(subject.getURI())){
						countLocalDefURIs++;
						
						String uri = subject.getURI();
						if (uri.contains("?")){
							Quad q = new Quad(null, subject, QPRO.exceptionDescription.asNode(), DQM.ParametarisedURI.asNode());
							this._problemList.add(q);
						} else if (uri.length() > 80){
							Quad q = new Quad(null, subject, QPRO.exceptionDescription.asNode(), DQM.LongURI.asNode());
							this._problemList.add(q);
						} else {
							shortURICount++;
						}
					}
				}
				this.seenSet.add(subject.getURI());
			}
			
			Node object = quad.getObject();
			if (object.isURI()){
				if ((!(object.isBlank())) &&  (!(this.seenSet.contains(object.getURI())))){
					if (possibleDereferenceableURI(object.getURI())){
						countLocalDefURIs++;
						
						String uri = object.getURI();
						if (uri.contains("?")){
							Quad q = new Quad(null, object, QPRO.exceptionDescription.asNode(), DQM.ParametarisedURI.asNode());
							this._problemList.add(q);
						} else if (uri.length() > 80){
							Quad q = new Quad(null, object, QPRO.exceptionDescription.asNode(), DQM.LongURI.asNode());
							this._problemList.add(q);
						} else {
							shortURICount++;
						}
					}
				}
				this.seenSet.add(object.getURI());
			}
		}
	}

	
	public double metricValue() {
		return (this.shortURICount == 0) ? 1.0 : ((double)shortURICount / (double)countLocalDefURIs);
	}

	
	public Resource getMetricURI() {
		return METRIC_URI;
	}

	
	public ProblemList<?> getQualityProblems() {
		ProblemList<Quad> pl = null;
			try {
				pl = new ProblemList<Quad>(this._problemList);
			} catch (ProblemListInitialisationException e) {
				logger.error(e.getMessage());
			}
		return pl;
	}

	@Override
	public boolean isEstimate() {
		return false;
	}

	@Override
	public Resource getAgentURI() {
		return 	DQM.LuzzuProvenanceAgent;
	}
	
	private boolean possibleDereferenceableURI(String uri){
		if (uri.startsWith("http") || uri.startsWith("https")) return true;
		return false;
	}
	
}
