/**
 * 
 */
package eu.diachron.qualitymetrics.representational.versatility;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.mapdb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.semantics.DQMPROB;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ResourceBaseURIOracle;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
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
 * In this metric we check if in a dataset has 1 or more triples descibing
 * the different serialisation formats that a dataset is available in, 
 * using the void:feature. A list of possible serialisation formats 
 * can be found: http://www.w3.org/ns/formats/ 
 * 
 * The metric returns 1 if the data published is represented in 2 or more
 * formats.
 */
public class DifferentSerialisationFormats extends AbstractQualityMetric{
	
	private static DB mapDb = MapDbFactory.getMapDBAsyncTempFile();
	private Set<SerialisableQuad> _problemList = MapDbFactory.createHashSet(mapDb, UUID.randomUUID().toString());
	
	private static Logger logger = LoggerFactory.getLogger(DifferentSerialisationFormats.class);
	
	private String datasetURI = null;
	
	ResourceBaseURIOracle oracle = new ResourceBaseURIOracle();
	
	private ConcurrentHashMap<String, List<String>> datasetFeatures = new ConcurrentHashMap<String, List<String>>();
	
	boolean flag = false;
	
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
			datasetURI = quad.getSubject().getURI();
			
			if (object.isURI()){
				if (formats.contains(object.getURI())) {
					List<String> features = getOrCreate(datasetURI);
					features.add(object.getURI());
					this.datasetFeatures.put(datasetURI, features);
				}
				else {
					Quad q = new Quad(null, object, QPRO.exceptionDescription.asNode(), DQMPROB.IncorrectFormatDefined.asNode());
					this._problemList.add(new SerialisableQuad(q));
				}
			}
		}
	}

	private List<String> getOrCreate(String datasetURI){
		List<String> lst = new ArrayList<String>();
		if (this.datasetFeatures.containsKey(datasetURI)) lst = this.datasetFeatures.get(datasetURI);
		else this.datasetFeatures.put(datasetURI, lst);
		return lst;
	}
	
	
	@Override
	public double metricValue() {
		if (!flag){
			this.setQualityProblems();
			flag = true;
		}
		
		int totalNumberDatasets = this.datasetFeatures.size();
		int datasetsWithMoreThanOneFeature = 0;
		for(String dataset : this.datasetFeatures.keySet()){
			List<String> features = this.datasetFeatures.get(dataset);
			if (features.size() > 1){
				datasetsWithMoreThanOneFeature++;
			}
		}
		statsLogger.info("Different Serialisation Formats. Dataset: {} - Datasets With More than One Feature {}, Total Number Dataset {}", 
				EnvironmentProperties.getInstance().getDatasetURI(), datasetsWithMoreThanOneFeature,totalNumberDatasets );

		return (double)datasetsWithMoreThanOneFeature / (double) totalNumberDatasets;
	}
	
	private void setQualityProblems(){
		for(String dataset : this.datasetFeatures.keySet()){
			List<String> features = this.datasetFeatures.get(dataset);
			if (features.size() <= 1){
				Quad q = new Quad(null, ModelFactory.createDefaultModel().createResource(dataset).asNode(), QPRO.exceptionDescription.asNode(), DQMPROB.NoMultipleFormatDefined.asNode());
				this._problemList.add(new SerialisableQuad(q));
			}
		}
	}

	@Override
	public Resource getMetricURI() {
		return DQM.DifferentSerialisationsMetric;
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
