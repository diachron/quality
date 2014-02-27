package de.unibonn.iai.eis.diachron.qualitymetrics.report;

import java.util.List;

import de.unibonn.iai.eis.diachron.qualitymetrics.QualityMetric;

public class QualityProblem {

	private final QualityMetric metric;

	public QualityMetric getMetric() {
		return metric;
	}

	List<String> detailedProblems;

	public List<String> getDetailedProblems() {
		return detailedProblems;
	}

	public void setDetailedProblems(List<String> detailedProblems) {
		this.detailedProblems = detailedProblems;
	}

	public QualityProblem(QualityMetric qualityMetric) {
		this.metric = qualityMetric;
	}

}
