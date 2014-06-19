package de.unibonn.iai.eis.diachron.qualitymetrics.representational.understandability;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.ProblemList;
import de.unibonn.iai.eis.diachron.qualitymetrics.AbstractQualityMetric;


public class EmptyAnnotationValue extends AbstractQualityMetric {

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

}
