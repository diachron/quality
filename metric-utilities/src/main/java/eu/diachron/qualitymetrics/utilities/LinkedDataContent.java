/**
 * 
 */
package eu.diachron.qualitymetrics.utilities;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.LangBuilder;
import org.apache.jena.riot.RDFLanguages;

import com.hp.hpl.jena.util.FileUtils;


/**
 * @author Jeremy Debattista
 * 
 * This static class contains all information about how Linked Data
 * is served on the Web of Data. These are modified from Jena WebContent
 * Class.
 */
public class LinkedDataContent {
	
	
	//MIME Types registered at http://www.iana.org/assignments/media-types/media-types.xhtml
    public static final String contentTypeN3                = "text/n3" ;
    public static final String contentTypeTurtle            = "text/turtle" ;
    public static final String contentTypeRDFXML            = "application/rdf+xml" ;
    public static final String contentTypeRDFJSON           = "application/rdf+json" ;
    public static final String contentTypeNTriples          = "application/n-triples" ;
    public static final String contentTypeJSONLD         	= "application/ld+json" ;
    public static final String contentTypeNTriplesPlain		= "text/plain";
    public static final String contentTypeRDFTurtle			= "text/rdf+n3";

    //MIME Types not registered at IANA but found in their specific W3C Specs
    public static final String contentTypeNQuads	       	= "application/n-quads" ;
    public static final String contentTypeTriG              = "application/trig" ;
    
    //File Extensions
    public static final String fileExtensionTurtle          = ".ttl" ;
    public static final String fileExtensionNTriples        = ".nt" ;
    public static final String fileExtensionRDFXML        	= ".rdf" ;
    public static final String fileExtensionRDFJSON        	= ".rj" ;
    public static final String fileExtensionN3       		= ".n3" ;
    public static final String fileExtensionJSONLD       	= ".jsonld" ;
    public static final String fileExtensionNQ       		= ".nq" ;
    public static final String fileExtensionTRIG       		= ".trig" ;

    
	//Registering JSONLD to Jena
	public static Lang JSONLD = LangBuilder.create("JSONLD", contentTypeJSONLD)
             .addAltNames("JSONLD")
             .addFileExtensions(".jsonld")
             .build();
	static{
		if(RDFLanguages.nameToLang(contentTypeJSONLD) == null) {
			RDFLanguages.register(JSONLD);
		} else {
			JSONLD = RDFLanguages.nameToLang(contentTypeJSONLD);
		}
	}
    
    private static Map<String, Lang> mapContentTypeToLang = new HashMap<String, Lang>() ;
    static {
        mapContentTypeToLang.put(contentTypeN3,				RDFLanguages.N3) ;
        mapContentTypeToLang.put(contentTypeTurtle,			RDFLanguages.TURTLE);
        mapContentTypeToLang.put(contentTypeRDFXML,			RDFLanguages.RDFXML);
        mapContentTypeToLang.put(contentTypeRDFJSON,		RDFLanguages.RDFJSON);
        mapContentTypeToLang.put(contentTypeNTriples,		RDFLanguages.NTRIPLES);
        mapContentTypeToLang.put(contentTypeJSONLD,			JSONLD);
        mapContentTypeToLang.put(contentTypeNQuads,			RDFLanguages.NQUADS);
        mapContentTypeToLang.put(contentTypeTriG,			RDFLanguages.TRIG);
        mapContentTypeToLang.put(contentTypeNTriplesPlain,	RDFLanguages.NTRIPLES);
        mapContentTypeToLang.put(contentTypeRDFTurtle,		RDFLanguages.TURTLE);
    }
    
    private static Map<Lang, String> mapLangToContentType =  new HashMap<Lang, String>() ;
    static {
        mapLangToContentType.put(RDFLanguages.N3,           contentTypeN3) ;
        mapLangToContentType.put(RDFLanguages.TURTLE,       contentTypeTurtle) ;
        mapLangToContentType.put(RDFLanguages.RDFXML,       contentTypeRDFXML) ;
        mapLangToContentType.put(RDFLanguages.RDFJSON,		contentTypeRDFJSON) ;
        mapLangToContentType.put(RDFLanguages.NTRIPLES,     contentTypeNTriples) ;
        mapLangToContentType.put(			  JSONLD,		contentTypeJSONLD);
        mapLangToContentType.put(RDFLanguages.NQUADS,       contentTypeNQuads) ;
        mapLangToContentType.put(RDFLanguages.TRIG,         contentTypeTriG) ;
    }
    
    
    private static Map<String, String> mapContentTypeToFileExt = new HashMap<String, String>() ;
    static {
    	mapContentTypeToFileExt.put(contentTypeN3,			fileExtensionNTriples) ;
    	mapContentTypeToFileExt.put(contentTypeTurtle,		fileExtensionTurtle);
    	mapContentTypeToFileExt.put(contentTypeRDFXML,		fileExtensionRDFXML);
    	mapContentTypeToFileExt.put(contentTypeRDFJSON,		fileExtensionRDFJSON);
    	mapContentTypeToFileExt.put(contentTypeNTriples,	fileExtensionN3);
    	mapContentTypeToFileExt.put(contentTypeJSONLD,		fileExtensionJSONLD);
    	mapContentTypeToFileExt.put(contentTypeNQuads,		fileExtensionNQ);
    	mapContentTypeToFileExt.put(contentTypeTriG,		fileExtensionTRIG);
    	mapContentTypeToFileExt.put(contentTypeNTriplesPlain,	fileExtensionN3);
    	mapContentTypeToFileExt.put(contentTypeRDFTurtle,		fileExtensionTurtle);
    }
    
    private static Map<String, String> mapFileExtToContentType =  new HashMap<String, String>() ;
    static {
    	mapFileExtToContentType.put(fileExtensionNTriples,     	contentTypeN3) ;
    	mapFileExtToContentType.put(fileExtensionTurtle,       	contentTypeTurtle) ;
    	mapFileExtToContentType.put(fileExtensionRDFXML,       	contentTypeRDFXML) ;
    	mapFileExtToContentType.put(fileExtensionRDFJSON,		contentTypeRDFJSON) ;
    	mapFileExtToContentType.put(fileExtensionN3,     		contentTypeNTriples) ;
    	mapFileExtToContentType.put(fileExtensionJSONLD,		contentTypeJSONLD);
    	mapFileExtToContentType.put(fileExtensionNQ,       		contentTypeNQuads) ;
    	mapFileExtToContentType.put(fileExtensionTRIG,         	contentTypeTriG) ;
    }
    
    
    
    public static Lang contentTypeToLang(String ct){
    	return mapContentTypeToLang.get(ct);
    }
    
    public static String contentTypeCanonical(String contentType)
    { 
        Lang lang = contentTypeToLang(contentType) ;
        if ( lang == null ) return null ;
        return mapLangToContentType.get(lang) ;
    }

    public static String langToContentType(Lang lang){
    	return mapLangToContentType.get(lang);
    }
    
    public static String fileExtToContentType(String ext){
    	return mapFileExtToContentType.get(ext);
    }

    public static String contentTypeToFileExt(String ct){
    	return mapContentTypeToFileExt.get(ct);
    }
    
    public static String guessContentType(String fileOrUri){
    	if ( fileOrUri == null ) return null ;
    	if ( fileOrUri.endsWith(".gz") )
        	fileOrUri = fileOrUri.substring(0, fileOrUri.length()-3) ;
    	String ext = FileUtils.getFilenameExt(fileOrUri);
    	return fileExtToContentType(ext);
    }
    
	public static Set<String> contentTypes = new HashSet<String>();
	static{
		contentTypes.add(contentTypeN3);
		contentTypes.add(contentTypeTurtle);
		contentTypes.add(contentTypeRDFXML);
		contentTypes.add(contentTypeRDFJSON);
		contentTypes.add(contentTypeNTriples);
		contentTypes.add(contentTypeJSONLD);
		contentTypes.add(contentTypeNQuads);
		contentTypes.add(contentTypeTriG);
		contentTypes.add(contentTypeNTriplesPlain);
		contentTypes.add(contentTypeRDFTurtle);
	}
	
	public static Set<String> fileExtension = new HashSet<String>();
	static{
		fileExtension.add(fileExtensionN3);
		fileExtension.add(fileExtensionTurtle);
		fileExtension.add(fileExtensionRDFXML);
		fileExtension.add(fileExtensionRDFJSON);
		fileExtension.add(fileExtensionNTriples);
		fileExtension.add(fileExtensionJSONLD);
		fileExtension.add(fileExtensionNQ);
		fileExtension.add(fileExtensionTRIG);
	}
	
}
