/**
 * 
 */
package eu.diachron.qualitymetrics.representational.interpretability;

import java.util.Set;
import java.util.UUID;

import org.mapdb.DB;
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
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import eu.diachron.qualitymetrics.utilities.SerialisableQuad;
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
 */
public class UndefinedClassesAndProperties implements QualityMetric {

	private int undefinedClasses = 0;
	private int undefinedProperties = 0;
	private int totalClasses = 0;
	private int totalProperties = 0;
	
	private static Logger logger = LoggerFactory.getLogger(UndefinedClassesAndProperties.class);
//	private SharedResources shared = SharedResources.getInstance();
	private static DB mapDb = MapDbFactory.getMapDBAsyncTempFile();

	private Set<String> seenSet = MapDbFactory.createHashSet(mapDb, UUID.randomUUID().toString());
	private Set<SerialisableQuad> _problemList = MapDbFactory.createHashSet(mapDb, UUID.randomUUID().toString());

	
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
					
					Boolean defined = VocabularyLoader.checkTerm(object);
					
					if (!defined){
						this.undefinedClasses++;
						Quad q = new Quad(null, object, QPRO.exceptionDescription.asNode(), DQM.UndefinedClass.asNode());
						this._problemList.add(new SerialisableQuad(q));
					}
				}
				this.seenSet.add(object.getURI());
			}
			
		} 
		if (!(this.seenSet.contains(predicate.getURI()))){
			// Checking for properties
			this.totalProperties++;
			logger.info("checking predicate: " + predicate.getURI());
			
			if (!(this.isContainerPredicate(predicate))){
//				Boolean seen = shared.classOrPropertyDefined(predicate.getURI());
//				Boolean defined = null;
//				if (seen == null) {
//					defined = VocabularyLoader.isProperty(predicate);
//					shared.addClassOrProperty(predicate.getURI(), defined);
//				}
//				else defined = seen;
				Boolean defined = VocabularyLoader.isProperty(predicate);

				if (!defined){
					this.undefinedProperties++;
					Quad q = new Quad(null, predicate, QPRO.exceptionDescription.asNode(), DQM.UndefinedProperty.asNode());
					this._problemList.add(new SerialisableQuad(q));
				}
			}
			this.seenSet.add(predicate.getURI());
		}	
	}
	
	private boolean isContainerPredicate(Node predicate){
		if (predicate.getURI().matches(RDF.getURI()+"_[0-9]+")){
			return true;
		}
		return false;
	}

	@Override
	public double metricValue() {
		statsLogger.info("Undefined Classes and Properties. Dataset: {} - Undefined Classes {}, Undefined Properties {}", 
				EnvironmentProperties.getInstance().getDatasetURI(), this.undefinedClasses, this.undefinedProperties);


		return (this.undefinedClasses + this.undefinedProperties == 0) ? 1.0 
				: 1.0 - ((double)(this.undefinedClasses + this.undefinedProperties)/(double)(this.totalClasses + this.totalProperties));
	}

	@Override
	public Resource getMetricURI() {
		return DQM.UndefinedClassesAndPropertiesMetric;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		ProblemList<SerialisableQuad> pl = null;
		try {
			if(this._problemList != null && this._problemList.size() > 0) {
				pl = new ProblemList<SerialisableQuad>(this._problemList);	
			} else {
				pl = new ProblemList<SerialisableQuad>();
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
