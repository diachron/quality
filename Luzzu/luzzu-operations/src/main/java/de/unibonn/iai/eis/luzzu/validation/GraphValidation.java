package de.unibonn.iai.eis.luzzu.validation;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Jeremy Debattista
 *
 * This class contains methods that perform validation
 * on datasets with regard to their Quality Metadata
 */
public class GraphValidation {
	
	
	
	public static boolean hasQualityMetadata(Model dataset){
		
		return false;
	}
	
	public static boolean hasQualityMetadata(Resource datasetURI, boolean sparqlEndpoint){
		if (!sparqlEndpoint){
		}
		return false;	
	}
	
}
