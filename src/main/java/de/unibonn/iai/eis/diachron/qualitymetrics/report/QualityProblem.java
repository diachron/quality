package de.unibonn.iai.eis.diachron.qualitymetrics.report;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.qualitymetrics.QualityMetric;

public class QualityProblem {

	private final QualityMetric metric;
	private final Set<Quad> affectedInstances = new HashSet<Quad>();

	public QualityMetric getMetric() {
		return metric;
	}

	public QualityProblem(QualityMetric qualityMetric) {
		this.metric = qualityMetric;
	}

	public void addInstance(Quad instance) {
		affectedInstances.add(instance);
	}

}
