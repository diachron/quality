/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.interlinking;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.mapdb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;
import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ResourceBaseURIOracle;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.ProblemListInitialisationException;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import eu.diachron.qualitymetrics.accessibility.availability.helper.ModelParser;

/**
 * @author Jeremy Debattista
 * 
 * In this metric we identify the total number of external linked used in the dataset. An external link
 * is identified if the subject URI is from one data source and an object URI from ￼another data source.
 * The data source should return RDF data to be considered as 'linked'.
 * In this metric rdf:type triples are skipped since these are not normally considered as part of the
 * Data Level Constant (or Data Level Position).
 * The value returned by this metric is the number of valid external links a dataset has (i.e. the number
 * of resource links not the number of links to datasets)
 * 
 * Based on: [1] Hogan Aidan, Umbrich Jürgen. An empirical survey of Linked Data conformance. Section 5.2, 
 * Linking, Issue VI: Use External URIs (page 20).
 */
public class LinkExternalDataProviders implements QualityMetric {
	
	/**
	 * MapDB database, used to persist the Map containing the instances found to be declared in the dataset
	 */
	private DB mapDB = MapDbFactory.createAsyncFilesystemDB();
	
	/**
	 * A set that holds all unique resources
	 */
	private Set<String> setResources = mapDB.createHashSet("link-external-data-providers").make();

	
	/**
	 * A set that holds all unique PLDs that return RDF data
	 */
	private Set<String> setPLDsRDF = mapDB.createHashSet("link-external-data-providers-rdf").make();
	
	final static Logger logger = LoggerFactory.getLogger(LinkExternalDataProviders.class);


	private final Resource METRIC_URI = DQM.LinksToExternalDataProvidersMetric;
	
	private List<Quad> _problemList = new ArrayList<Quad>();

	private boolean computed = false;
	
	@Override
	public void compute(Quad quad) {
		logger.debug("Computing : {} ", quad.asTriple().toString());
		
		String subject = ResourceBaseURIOracle.extractPayLevelDomainURI(quad.getSubject().toString());
		setResources.add(subject);

		if (quad.getObject().isURI()){
			String object = ResourceBaseURIOracle.extractPayLevelDomainURI(quad.getObject().toString());
			setResources.add(object);
		}
	}

	@Override
	public double metricValue() {
		if (!computed){
			//remove the base uri from the set because that will not be an "external link"
			String baseURI = EnvironmentProperties.getInstance().getDatasetURI();
			
			Iterator<String> iterator = setResources.iterator();
			while (iterator.hasNext()) {
			    String element = iterator.next();
			    if (element.contains(baseURI)) iterator.remove();
			}
			
			this.checkForRDFLinks();
			computed = true;
		}
		
		statsLogger.info("LinkExternalDataProviders. Dataset: {} - # Top Level Domains : {};", 
				EnvironmentProperties.getInstance().getDatasetURI(), setPLDsRDF.size());
		
		return setPLDsRDF.size();
	}

	@Override
	public Resource getMetricURI() {
		return METRIC_URI;
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
		return DQM.LuzzuProvenanceAgent;
	}
	
	private void checkForRDFLinks() {
		for (String s : setResources){
			if (ModelParser.snapshotParser(s))
				setPLDsRDF.add(ResourceBaseURIOracle.extractPayLevelDomainURI(s));
		}
	}
}
