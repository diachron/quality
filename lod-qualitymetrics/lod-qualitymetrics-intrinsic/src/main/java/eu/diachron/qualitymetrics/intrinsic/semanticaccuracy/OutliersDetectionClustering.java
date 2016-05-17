/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.semanticaccuracy;

import org.mapdb.HTreeMap;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.semantics.utilities.Commons;
import eu.diachron.qualitymetrics.intrinsic.semanticaccuracy.helper.PredicateClusteringIndexing;
import eu.diachron.qualitymetrics.utilities.AbstractQualityMetric;

/**
 * @author Jeremy Debattista
 * 
 * TODO: TEST
 * 
 */
public class OutliersDetectionClustering extends AbstractQualityMetric {

	protected HTreeMap<String, PredicateClusteringIndexing> propertyClustersClasses = 
			MapDbFactory.createFilesystemDB().createHashMap("predicate_clustering_index").make();

	protected HTreeMap<String,String> typesMap = 
			MapDbFactory.createFilesystemDB().createHashMap("types_map").make();

	protected boolean isCalculated = false;
	
	
	@Override
	public void compute(Quad quad) {
		// TODO Auto-generated method stub
		Resource s = Commons.asRDFNode(quad.asTriple().getSubject()).asResource();
		Resource p = Commons.asRDFNode(quad.asTriple().getPredicate()).asResource();
		Resource o = Commons.asRDFNode(quad.asTriple().getObject()).asResource();
		
		if (o.isLiteral()) return; // we are not treating literals
		
		if (p.equals(RDF.type)) {
			typesMap.put(s.getURI(), o.getURI());
		}
		else {
			PredicateClusteringIndexing predicateClustering;
			
			if (propertyClustersClasses.containsKey(p.getURI())){
				predicateClustering = propertyClustersClasses.get(p.getURI());
			} else {
				predicateClustering = new PredicateClusteringIndexing(p);
				propertyClustersClasses.put(p.getURI(), predicateClustering);
			}
			
			predicateClustering.addTriple(quad.asTriple());
		}
	}

	@Override
	public double metricValue() {
		double avg = 0.0;
		if (!isCalculated){
			for (PredicateClusteringIndexing pci : propertyClustersClasses.values()){
				pci.findOutliers(typesMap); //TODO split in threads
				
				//Model m = pci.evaluationModel(typesMap);
				//TODO: fix evaluation model to output problem report
				avg += pci.getApproximateValue();
			}
		}
		
		return avg / (double) propertyClustersClasses.size();
	}

	@Override
	public Resource getMetricURI() {
		return null;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		return null;
	}

	@Override
	public boolean isEstimate() {
		return true;
	}

	@Override
	public Resource getAgentURI() {
		return null;
	}

}
