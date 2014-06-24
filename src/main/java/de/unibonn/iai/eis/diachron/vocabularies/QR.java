/* CVS $Id: $ */
package de.unibonn.iai.eis.diachron.vocabularies; 
import com.hp.hpl.jena.rdf.model.*;
 
/**
 * Vocabulary definitions from src/main/resources/vocabularies/qr/qr.trig 
 * @author Auto-generated by schemagen on 23 Jun 2014 15:47 
 */
public class QR {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://purl.org/eis/vocab/qr#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    /** <p>Represents the dataset URI on which quality metrics where computed</p> */
    public static final Property computedOn = m_model.createProperty( "http://purl.org/eis/vocab/qr#computedOn" );
    
    /** <p>Identifies problem instances in the report</p> */
    public static final Property hasProblem = m_model.createProperty( "http://purl.org/eis/vocab/qr#hasProblem" );
    
    /** <p>Represent the URI of the metric which instantiated this problem</p> */
    public static final Property isDescribedBy = m_model.createProperty( "http://purl.org/eis/vocab/qr#isDescribedBy" );
    
    /** <p>Represent the actual problematic instance from the dataset. This could be 
     *  a list of resources (rdf:Seq) or a list of reified statements.</p>
     */
    public static final Property problematicThing = m_model.createProperty( "http://purl.org/eis/vocab/qr#problematicThing" );
    
    /** <p>Represents a kind of quality problems in which data type are not Homogeneous</p> */
    public static final Resource HomogeneousDatatypes = m_model.createResource( "http://purl.org/eis/vocab/qr#HomogeneousDatatypes" );
    
    /** <p>Represents a kind of quality problems in which range of data type is not consistent 
     *  with object's data type</p>
     */
    public static final Resource IncompatibleDatatypeRange = m_model.createResource( "http://purl.org/eis/vocab/qr#IncompatibleDatatypeRange" );
    
    /** <p>Represents a kind of quality problems in which value is not consistent with 
     *  data type</p>
     */
    public static final Resource MalformedDatatypeLiterals = m_model.createResource( "http://purl.org/eis/vocab/qr#MalformedDatatypeLiterals" );
    
    /** <p>Represents a kind of quality problems in which classes or properties are not 
     *  places properly</p>
     */
    public static final Resource MisplacedClassesOrProperties = m_model.createResource( "http://purl.org/eis/vocab/qr#MisplacedClassesOrProperties" );
    
    /** <p>Represents a kind of quality problems in which owl classes or owl properties 
     *  are not used properly</p>
     */
    public static final Resource MisuseOwlDatatypeOrObjectProperties = m_model.createResource( "http://purl.org/eis/vocab/qr#MisuseOwlDatatypeOrObjectProperties" );
    
    /** <p>Represents a quality problem detected during the assessment of quality metrics 
     *  on triples</p>
     */
    public static final Resource QualityProblem = m_model.createResource( "http://purl.org/eis/vocab/qr#QualityProblem" );
    
    /** <p>Represents a report on the problems detected during the assessment of quality 
     *  on a dataset</p>
     */
    public static final Resource QualityReport = m_model.createResource( "http://purl.org/eis/vocab/qr#QualityReport" );
    
    /** <p>Represents a kind of quality problems in which classes or properties are not 
     *  defined</p>
     */
    public static final Resource UndefinedClassesOrProperties = m_model.createResource( "http://purl.org/eis/vocab/qr#UndefinedClassesOrProperties" );
    
}
