package org.lod.qualitymetrics.ontologies;

import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import eu.diachron.qualitymetrics.utilities.TestLoader;


public class Testing {
	
	static TestLoader loader = new TestLoader();
	
	public static void main(String [] args){
		//TestConsitency();
		
		//TestRelationshipRichness();
		
		//TestInheritanceRichness();
		
		TestAttributeRichness();
		
	}
	
	public static void TestConsitency()
	{
        QualityMetric cm = new ConsistencyCheck();
		
		loader.loadDataSet("file:///home/lavdim/Downloads/libraries/simple.rdf");
		
		for(Quad q : loader.getStreamingQuads()){
			cm.compute(q);
			
		}
		//cm.after();
		
		System.out.println(cm.metricValue());
	}
	
	public static void TestRelationshipRichness()
	{
        QualityMetric cm = new RelationshipRichness();
		
		loader.loadDataSet("file:///home/lavdim/Downloads/libraries/simple.rdf");
		
		for(Quad q : loader.getStreamingQuads()){
			//System.out.println(q.asTriple().getSubject().toString());
			cm.compute(q);
			
		}
		//cm.after();
		
		//System.out.println(cm.metricValue());
	}
	
	public static void TestInheritanceRichness()
	{
        QualityMetric cm = new InheritanceRichness();
		
		loader.loadDataSet("file:///home/lavdim/Downloads/libraries/ChargingPoints.rdf");
		
		for(Quad q : loader.getStreamingQuads()){
			//System.out.println(q.asTriple().getSubject().toString());
			cm.compute(q);
			
		}
		//cm.after();
		
		System.out.println(cm.metricValue());
	}
	
	public static void TestAttributeRichness()
	{
        QualityMetric cm = new AttributeRichness();
		
		loader.loadDataSet("file:///home/lavdim/Downloads/libraries/ChargingPoints.rdf");
		
		for(Quad q : loader.getStreamingQuads()){
			//System.out.println(q.asTriple().getSubject().toString());
			cm.compute(q);
			
		}
		//cm.after();
		
		System.out.println(cm.metricValue());
	}


}
