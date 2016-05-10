/**
 * 
 */
package eu.diachron.qualitymetrics.contextual.provenance;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;

/**
 * @author Jeremy Debattista
 * 
 * According the the Data on the Web BP W3C WG, "consumers
 * need to know the origin or history of the published data... 
 * data published should include or link to provenance information".
 * 
 * In the Extended Provenance Metric, the "origin" problem is tackled.
 * This metric will identify discrepancies between the creation date
 * and the modified date (this alone would also be a good indicator),
 * and then checking if there are prov:Revision instances leading up
 * to the last modified date. 
 */
public class HistoryProvenanceTrackerMetric implements QualityMetric {

	@Override
	public void compute(Quad quad) {
		// TODO Auto-generated method stub

	}

	@Override
	public double metricValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Resource getMetricURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEstimate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Resource getAgentURI() {
		// TODO Auto-generated method stub
		return null;
	}

}
