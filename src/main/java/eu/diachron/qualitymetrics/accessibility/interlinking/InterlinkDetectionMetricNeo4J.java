/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.interlinking;


import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.tooling.GlobalGraphOperations;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.assessment.ComplexQualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DirectedGraph;
import eu.diachron.qualitymetrics.accessibility.interlinking.helper.CentralityMeasureN4J;
import eu.diachron.qualitymetrics.accessibility.interlinking.helper.ClusteringCoefficientMeasureN4J;
import eu.diachron.qualitymetrics.accessibility.interlinking.helper.DegreeMeasure;
import eu.diachron.qualitymetrics.accessibility.interlinking.helper.DegreeMeasureN4J;
import eu.diachron.qualitymetrics.accessibility.interlinking.helper.DescriptiveRichnessMeasure;
import eu.diachron.qualitymetrics.accessibility.interlinking.helper.DescriptiveRichnessMeasureN4J;
import eu.diachron.qualitymetrics.accessibility.interlinking.helper.RDFEdge;
import eu.diachron.qualitymetrics.accessibility.interlinking.helper.SameAsMeasure;
import eu.diachron.qualitymetrics.accessibility.interlinking.helper.SameAsMeasureN4J;
import eu.diachron.semantics.vocabulary.DQM;

/**
 * @author Jeremy Debattista
 * 
 */
public class InterlinkDetectionMetricNeo4J implements ComplexQualityMetric{
	
	private static GraphDatabaseService graphDb = new GraphDatabaseFactory()
    	.newEmbeddedDatabaseBuilder("target/graph")
    	.setConfig( GraphDatabaseSettings.nodestore_mapped_memory_size, "10M")
    	.setConfig( GraphDatabaseSettings.string_block_size, "60")
    	.setConfig( GraphDatabaseSettings.array_block_size, "300")
    .newGraphDatabase();
	
	private static IndexDefinition indexDefinition;
	private static Label value_Label = DynamicLabel.label("Value");
	static{
		registerShutdownHook(graphDb);
	
		try ( Transaction tx = graphDb.beginTx() )
		{
		    Schema schema = graphDb.schema();
		    indexDefinition = schema.indexFor(value_Label).on("value").create();
		    tx.success();
		}
		
        try ( Transaction tx = graphDb.beginTx() )
        {
            Schema schema = graphDb.schema();
            schema.awaitIndexOnline( indexDefinition, 10, TimeUnit.SECONDS );
        }
	}
	
	private DirectedGraph<String, RDFEdge> _graph = new DelegateForest<String, RDFEdge>();
	private boolean afterExecuted = false;
	
	private double metricValue = 0.0; //In order to calculate the metric value, we get the IDEAL value of all other sub-metrics and multiply it by a 0.2 weight
	
	private final Resource METRIC_URI = DQM.InterlinkDetectionMetric;
	
	public void compute(Quad quad) {
		try ( Transaction tx = graphDb.beginTx() )
		{
		    // Database operations go here
			String subject;
			Node subjectNode = null;
			subject = (quad.getSubject().isBlank()) ? quad.getSubject().getBlankNodeId().getLabelString() : quad.getSubject().getURI();
			ResourceIterable<Node> res = graphDb.findNodesByLabelAndProperty(value_Label, "value", subject);
			subjectNode = (res.iterator().hasNext()) ? res.iterator().next() : null;
			if (subjectNode == null){
				subjectNode = graphDb.createNode(value_Label);
				subjectNode.setProperty("value", subject);
				subjectNode.addLabel(LabelType.SUBJECT);
			}
			
			String object;
			Node objectNode = null;
			object = (quad.getObject().isBlank()) ? quad.getObject().getBlankNodeId().getLabelString() : 
				((quad.getObject().isURI()) ? quad.getObject().getURI() : quad.getObject().getLiteralValue().toString());
			res = graphDb.findNodesByLabelAndProperty(value_Label, "value", object);
			objectNode = (res.iterator().hasNext()) ? res.iterator().next() : null;
			if (objectNode == null){
				objectNode = graphDb.createNode(value_Label);
				objectNode.setProperty("value", object);
				subjectNode.addLabel(LabelType.OBJECT);
			}
			
			Relationship predicate = subjectNode.createRelationshipTo(objectNode, PropertyType.PROPERTY);
			predicate.setProperty("value", quad.getPredicate().getURI());
			
			tx.success();
		}
	}

	public Resource getMetricURI() {
		return this.METRIC_URI;
	}

	public ProblemList<?> getQualityProblems() {
		return null;
	}

	public double metricValue() {
		if (!this.afterExecuted) 
			this.after();

		return this.metricValue;
	}

	public void before(Object... arg0) {
		//do all initialisation
	}
	
	// Post-Processing
	public void after(Object... arg0) {
		this.afterExecuted = true;
		
		
		try ( Transaction ignore = graphDb.beginTx() )
		{
			//1. DegreeMeasure
			DegreeMeasureN4J dm = new DegreeMeasureN4J(graphDb);
			metricValue += dm.getIdealMeasure() * 0.2;
			
			//2. Clustering Coefficient
			ClusteringCoefficientMeasureN4J ccm = new ClusteringCoefficientMeasureN4J(graphDb);
			metricValue += ccm.getIdealMeasure() * 0.2;
			
			//3. Centrality
			CentralityMeasureN4J cm = new CentralityMeasureN4J(graphDb);
			metricValue += cm.getIdealMeasure() * 0.2;
			
			//4. OpenSameAs
			//	for this we do a ratio of the number of same as triples against the number of open sameas - ideally we have 0..
			SameAsMeasureN4J sam = new SameAsMeasureN4J(graphDb);
			metricValue += (1.0 - sam.getIdealMeasure()) * 0.2;
			
			//5. Description Richness
			DescriptiveRichnessMeasureN4J drm = new DescriptiveRichnessMeasureN4J(graphDb);
			metricValue += drm.getIdealMeasure() * 0.2;
		} finally {
			graphDb.shutdown();
            try {
				FileUtils.deleteDirectory(new File("target/graph"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static void registerShutdownHook(final GraphDatabaseService graphDb)
	{
	    // Registers a shutdown hook for the Neo4j instance so that it
	    // shuts down nicely when the VM exits (even if you "Ctrl-C" the
	    // running application).
	    Runtime.getRuntime().addShutdownHook( new Thread()
	    {
	        @Override
	        public void run()
	        {
	            graphDb.shutdown();
	            try {
					FileUtils.deleteDirectory(new File("target/graph"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	    } );
	}
	
	private static enum PropertyType implements RelationshipType
	{
	    PROPERTY
	}
	
	private static enum LabelType implements Label
	{
	    SUBJECT, PREDICATE, OBJECT
	}
}
