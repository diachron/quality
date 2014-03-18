package de.unibonn.iai.eis.diachron.qualitymetrics.report;

import java.util.Hashtable;

public class QualityReport {

	private final Hashtable<String, QualityProblem> qProblems = new Hashtable<String, QualityProblem>();

	public void addProblem(QualityProblem problem) {
		qProblems.put(problem.getMetric().getMetricURI().getLocalName(),
				problem);
	}
}
