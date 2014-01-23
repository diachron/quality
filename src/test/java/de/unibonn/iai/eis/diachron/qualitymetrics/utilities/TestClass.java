package de.unibonn.iai.eis.diachron.qualitymetrics.utilities;

import com.hp.hpl.jena.graph.Triple;

import de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability.Dereferencibility;
import de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability.RDFAccessibility;
import de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability.SPARQLAccessibility;
import de.unibonn.iai.eis.diachron.qualitymetrics.accessibility.availability.UnstructuredData;

public class TestClass {
    
	//test class for the metrics implemented   
	public static void main(String [] args){
		  TestLoader t = new TestLoader();
		  t.loadDataSet();
		  
		  //Objects for Classed to be tested
		  RDFAccessibility rdfa= new RDFAccessibility();
		  SPARQLAccessibility sparqla= new SPARQLAccessibility();
		  Dereferencibility derefa= new Dereferencibility();
		  UnstructuredData unstra = new UnstructuredData();
		  
		  //Test for RDF accessibility class
		  
		  for(Triple tr : t.getStreamingTriples()){
		   //System.out.println(tr.getSubject());
		   rdfa.compute(tr);
		   System.out.println("The metric RDF accessibility value triple:"+tr.getObject()+" is "+rdfa.metricValue());
		   
		  }
		  
		  //Test for SPARQL accessibility
		  
		  for(Triple tr : t.getStreamingTriples()){
			   //System.out.println(tr.getSubject());
			   sparqla.compute(tr);
			   System.out.println("The metric SPARQL accessibility value triple: "+tr.getObject()+" is "+sparqla.metricValue());
			   
			  }
		  System.out.println("Dereferencebility test begins");
		  //Test for Deferencibility accessibility
		  // Passing all the triple for the calculation of the metric value
		  for(Triple tr : t.getStreamingTriples())
			 {
			  System.out.println(tr.getObject());
			  derefa.compute(tr);
			  
			  unstra.compute(tr);
			 }
		  //Print the metric values calculated
		  System.out.println("Metric value for Derefrencibility is: " + derefa.metricValue());
		  System.out.println("Metric value for Unstructureddata is: " + unstra.metricValue());
		  	  
		  
		 }
}
