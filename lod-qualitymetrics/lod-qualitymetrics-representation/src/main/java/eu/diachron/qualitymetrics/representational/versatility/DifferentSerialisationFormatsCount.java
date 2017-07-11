/**
 * 
 */
package eu.diachron.qualitymetrics.representational.versatility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.semantics.DQMPROB;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.QPRO;
import de.unibonn.iai.eis.luzzu.semantics.vocabularies.VOID;
import eu.diachron.qualitymetrics.utilities.AbstractQualityMetric;
import eu.diachron.qualitymetrics.utilities.SerialisableQuad;

/**
 * @author Jeremy Debattista
 * 
 * Datasets can be represented in different formats, such as RDF/XML, N3,
 * N-Triples etc... The voID vocabulary allows data publishers to define
 * the possible formats in the dataset's metadata using the void:feature 
 * predicate.
 * 
 * In this metric we check if in a dataset has triples descibing
 * the different serialisation formats that a dataset is available in, 
 * using the void:feature. A list of possible serialisation formats 
 * can be found: http://www.w3.org/ns/formats/ 
 * 
 * The metric returns the number of different serialisation formats
 * in a dataset.
 */
public class DifferentSerialisationFormatsCount extends AbstractQualityMetric{
	
	private Set<Quad> _problemList = new HashSet<Quad>();
	
	private static Logger logger = LoggerFactory.getLogger(DifferentSerialisationFormatsCount.class);
	
	
	private int featureCount = 0;
	
	private static List<String> formats = new ArrayList<String>();
	static{
		formats.add("http://www.w3.org/ns/formats/JSON-LD");
		formats.add("http://www.w3.org/ns/formats/N3");
		formats.add("http://www.w3.org/ns/formats/N-Triples");
		formats.add("http://www.w3.org/ns/formats/N-Quads");
		formats.add("http://www.w3.org/ns/formats/LD_Patch");
		formats.add("http://www.w3.org/ns/formats/microdata");
		formats.add("http://www.w3.org/ns/formats/OWL_XML");
		formats.add("http://www.w3.org/ns/formats/OWL_Functional");
		formats.add("http://www.w3.org/ns/formats/OWL_Manchester");
		formats.add("http://www.w3.org/ns/formats/POWDER");
		formats.add("http://www.w3.org/ns/formats/POWDER-S");
		formats.add("http://www.w3.org/ns/formats/PROV-N");
		formats.add("http://www.w3.org/ns/formats/PROV-XML");
		formats.add("http://www.w3.org/ns/formats/RDFa");
		formats.add("http://www.w3.org/ns/formats/RDF_JSON");
		formats.add("http://www.w3.org/ns/formats/RDF_XML");
		formats.add("http://www.w3.org/ns/formats/RIF_XML");
		formats.add("http://www.w3.org/ns/formats/SPARQL_Results_XML");
		formats.add("http://www.w3.org/ns/formats/SPARQL_Results_JSON");
		formats.add("http://www.w3.org/ns/formats/SPARQL_Results_CSV");
		formats.add("http://www.w3.org/ns/formats/SPARQL_Results_TSV");
		formats.add("http://www.w3.org/ns/formats/Turtle");
		formats.add("http://www.w3.org/ns/formats/TriG");
	}

	@Override
	public void compute(Quad quad) {
		logger.debug("Assessing {}",quad.asTriple().toString());
//		this.datasetFeatures.putIfAbsent(EnvironmentProperties.getInstance().getDatasetURI(), new ArrayList<String>());
		
		Node predicate = quad.getPredicate();
		Node object = quad.getObject();
		
		if (predicate.hasURI(VOID.feature.getURI())){
			if (object.isURI()){
				if (formats.contains(object.getURI())) {
					featureCount++;
				}
				else {
					Quad q = new Quad(null, object, QPRO.exceptionDescription.asNode(), DQMPROB.IncorrectFormatDefined.asNode());
					this._problemList.add(new SerialisableQuad(q));
				}
			}
		}
	}

	
	
	@Override
	public double metricValue() {
		statsLogger.info("Different Serialisation Formats. Dataset: {} - Feature Count {}", 
				EnvironmentProperties.getInstance().getDatasetURI(), featureCount );

		return (double)featureCount;
	}
	

	@Override
	public Resource getMetricURI() {
		return DQM.DifferentSerialisationsMetric;
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
