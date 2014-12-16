package eswc15.evaluation.settings;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;

import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;

public class EvaluationCase implements Serializable {

	/**
	 * Random Serial Version
	 */
	private static final long serialVersionUID = -3157138489172108209L;
	
	transient private Model metricConfiguration;
	private long totalTriples;
	private List<Long> tIterations = new ArrayList<Long>();
	private long tAvg;
	private long tMin = Long.MAX_VALUE;
	private long tMax = Long.MIN_VALUE;
	private String caseName;
	private String caseDescription;
	private String metricDump = "";
	private QualityMetric metric;
	private QualityMetric _metric;
	private List<Double> metricValues = new ArrayList<Double>();
	
	public EvaluationCase(String caseName, QualityMetric metric){
		this.caseName = caseName;
		this.setMetric(metric);
		this._metric = metric;
	}
	
	
	public void resetMetric(){
		QualityMetric _ = _metric;
		this.setMetric(_metric);
		_metric = _;
	}
	
	public Model getMetricConfiguration() {
		return metricConfiguration;
	}

	public long getTotalTriples() {
		return totalTriples;
	}

	public void setTotalTriples(long totalTriples) {
		this.totalTriples = totalTriples;
	}

	public List<Long> gettIterations() {
		return tIterations;
	}

	public void setDifference(long difference) {
		tAvg += difference;
		tMax = (tMax < difference) ? difference : tMax;
		tMin = (tMin > difference) ? difference : tMin;
		tIterations.add(difference);
	}

	public long gettAvg() {
		return (tAvg / tIterations.size());
	}

	public long gettMin() {
		return tMin;
	}

	public long gettMax() {
		return tMax;
	}


	public void setCaseName(String caseName) {
		this.caseName = caseName;
	}
	
	public String getCaseName() {
		return caseName;
	}

	public String getCaseDescription() {
		return caseDescription;
	}

	public void setCaseDescription(String caseDescription) {
		this.caseDescription = caseDescription;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(this.metric.getClass().getEnclosingClass().getName().toString() + ",");
		sb.append(this.getTotalTriples() + ",");
		sb.append((this.gettAvg()/ 1000.0) + ",");
		sb.append((this.gettMin()/ 1000.0) + ",");
		sb.append((this.gettMax()/ 1000.0));
		return sb.toString();
	}
	
	public String getMetricDump() {
		return metricDump;
	}


	public QualityMetric getMetric() {
		return metric;
	}


	public void setMetric(QualityMetric metric) {
		this.metric = metric;
	}


	public List<Double> getMetricValues() {
		return metricValues;
	}


	public void addMetricValue(Double value) {
		this.metricValues.add(value);
	}
}
