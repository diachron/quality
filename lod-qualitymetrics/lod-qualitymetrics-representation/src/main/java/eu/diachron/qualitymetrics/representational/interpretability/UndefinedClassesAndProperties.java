/**
 * 
 */
package eu.diachron.qualitymetrics.representational.interpretability;

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
import eu.diachron.qualitymetrics.representational.utils.SharedResources;
import eu.diachron.qualitymetrics.utilities.VocabularyLoader;

/**
 * @author Jeremy Debattista
 * 
 * This metric measures the number of undefined classes and
 * properties used by a data publisher in the assessed dataset.
 * By undefined classes and properties we mean that such resources
 * are used without any formal definition (e.g. using foaf:image 
 * instead of foaf:img).
 * 
 * 
 */
public class UndefinedClassesAndProperties implements QualityMetric {

	private int undefinedClasses = 0;
	private int undefinedProperties = 0;
	private int totalClasses = 0;
	private int totalProperties = 0;
	
	private static Logger logger = LoggerFactory.getLogger(UndefinedClassesAndProperties.class);
	private SharedResources shared = SharedResources.getInstance();
	private List<Quad> _problemList = new ArrayList<Quad>();

	private Set<String> seenSet = MapDbFactory.createFilesystemDB().createHashSet("seen-set").make();
	
	@Override
	public void compute(Quad quad) {
		logger.debug("Assessing quad: " + quad.asTriple().toString());

		Node predicate = quad.getPredicate();
		
		if (predicate.hasURI(RDF.type.getURI())){
		
			// Checking for classes
			Node object = quad.getObject();
			if ((!(object.isBlank())) &&  (!(this.seenSet.contains(object.getURI())))){
				logger.info("checking class: " + object.getURI());
	
				if (!(object.isBlank())){
					this.totalClasses++;
					
					Boolean seen = shared.classOrPropertyDefined(object.getURI());
					Boolean defined = null;
					if (seen == null) {
						defined = VocabularyLoader.checkTerm(object);
						shared.addClassOrProperty(object.getURI(), defined);
					}
					else defined = seen;
					
					if (!defined){
						this.undefinedClasses++;
						Quad q = new Quad(null, object, QPRO.exceptionDescription.asNode(), DQM.UndefinedClass.asNode());
						this._problemList.add(q);
					}
				}
				this.seenSet.add(object.getURI());
			}
			
		} 
		if (!(this.seenSet.contains(predicate.getURI()))){
				// Checking for properties
				this.totalProperties++;
				logger.info("checking predicate: " + predicate.getURI());
	
				Boolean seen = shared.classOrPropertyDefined(predicate.getURI());
				Boolean defined = null;
				if (seen == null) {
					defined = VocabularyLoader.checkTerm(predicate);
					shared.addClassOrProperty(predicate.getURI(), defined);
				}
				else defined = seen;
				
				if (!defined){
					this.undefinedProperties++;
					Quad q = new Quad(null, predicate, QPRO.exceptionDescription.asNode(), DQM.UndefinedProperty.asNode());
					this._problemList.add(q);
				}
				
				this.seenSet.add(predicate.getURI());
		}	
	}

	@Override
	public double metricValue() {
		logger.debug("Values: Undefined Classes {}, Undefined Properties {}", this.undefinedClasses, this.undefinedProperties );

		return (this.undefinedClasses + this.undefinedProperties == 0) ? 1.0 
				: 1.0 - ((double)(this.undefinedClasses + this.undefinedProperties)/(double)(this.totalClasses + this.totalProperties));
	}

	@Override
	public Resource getMetricURI() {
		return DQM.UndefinedClassesAndPropertiesMetric;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		ProblemList<Quad> pl = null;
		try {
			if(this._problemList != null && this._problemList.size() > 0) {
				pl = new ProblemList<Quad>(this._problemList);
			} else {
				pl = new ProblemList<Quad>();
			}
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
	
}
