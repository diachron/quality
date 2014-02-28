package de.unibonn.iai.eis.diachron.qualitymetrics.utilities;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.unibonn.iai.eis.diachron.configuration.InternalModelConf;

public class DAQHelper {

	private DAQHelper(){}
	
	public static String getClassLabel(Resource uri){
		StmtIterator iter = InternalModelConf.getDAQModel().listStatements(uri, RDFS.label, (RDFNode) null);
		String label = "";
		while (iter.hasNext()){
			label = iter.nextStatement().getObject().toString();
		}
		return label;
	}
	
	
	public static String getDimensionLabel(Resource metricURI){
		return getClassLabel(getDomainResource(metricURI));
	}
	
	public static String getCategoryLabel(Resource metricURI){
		Resource dim = getDomainResource(metricURI);
		Resource cat = getDomainResource(dim);
		
		return getClassLabel(cat);
	}
	
	private static Resource getDomainResource(Resource uri){
		String whereClause = "?prop " + " " + SPARQLHelper.toSPARQL(RDFS.range) + SPARQLHelper.toSPARQL(uri) + " . ";
		whereClause = whereClause + " ?prop " + SPARQLHelper.toSPARQL(RDFS.domain) + " ?domain .";
		
		String query = SPARQLHelper.SELECT_STATEMENT.replace("[variables]", "?domain").replace("[whereClauses]", whereClause);
		Resource r = null;
		Query qry = QueryFactory.create(query);
	    QueryExecution qe = QueryExecutionFactory.create(qry, InternalModelConf.getDAQModel());
	    ResultSet rs = qe.execSelect();
	    
	    while (rs.hasNext()){
	    	r = rs.next().get("domain").asResource();
	    }
	    
	    return r;
	}
	
	
	public static String getClassDescription(Resource uri){
		StmtIterator iter = InternalModelConf.getDAQModel().listStatements(uri, RDFS.comment, (RDFNode) null);
		String label = "";
		while (iter.hasNext()){
			label = iter.nextStatement().getObject().toString();
		}
		return label;
	}
}
