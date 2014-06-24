/* CVS $Id: $ */
package de.unibonn.iai.eis.diachron.vocabularies; 
import com.hp.hpl.jena.rdf.model.*;
 
/**
 * Vocabulary definitions from src/main/resources/vocabularies/void/void.ttl 
 * @author Auto-generated by schemagen on 24 Jun 2014 21:33 
 */
public class VOID {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://rdfs.org/ns/void#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    /** <p>The rdfs:Class that is the rdf:type of all entities in a class-based partition.</p> */
    public static final Property class_ = m_model.createProperty( "http://rdfs.org/ns/void#class" );
    
    /** <p>A subset of a void:Dataset that contains only the entities of a certain rdfs:Class.</p> */
    public static final Property classPartition = m_model.createProperty( "http://rdfs.org/ns/void#classPartition" );
    
    /** <p>The total number of distinct classes in a void:Dataset. In other words, the 
     *  number of distinct resources occuring as objects of rdf:type triples in the 
     *  dataset.</p>
     */
    public static final Property classes = m_model.createProperty( "http://rdfs.org/ns/void#classes" );
    
    /** <p>An RDF dump, partial or complete, of a void:Dataset.</p> */
    public static final Property dataDump = m_model.createProperty( "http://rdfs.org/ns/void#dataDump" );
    
    /** <p>The total number of distinct objects in a void:Dataset. In other words, the 
     *  number of distinct resources that occur in the object position of triples 
     *  in the dataset. Literals are included in this count.</p>
     */
    public static final Property distinctObjects = m_model.createProperty( "http://rdfs.org/ns/void#distinctObjects" );
    
    /** <p>The total number of distinct subjects in a void:Dataset. In other words, the 
     *  number of distinct resources that occur in the subject position of triples 
     *  in the dataset.</p>
     */
    public static final Property distinctSubjects = m_model.createProperty( "http://rdfs.org/ns/void#distinctSubjects" );
    
    /** <p>The total number of documents, for datasets that are published as a set of 
     *  individual documents, such as RDF/XML documents or RDFa-annotated web pages. 
     *  Non-RDF documents, such as web pages in HTML or images, are usually not included 
     *  in this count. This property is intended for datasets where the total number 
     *  of triples or entities is hard to determine. void:triples or void:entities 
     *  should be preferred where practical.</p>
     */
    public static final Property documents = m_model.createProperty( "http://rdfs.org/ns/void#documents" );
    
    /** <p>The total number of entities that are described in a void:Dataset.</p> */
    public static final Property entities = m_model.createProperty( "http://rdfs.org/ns/void#entities" );
    
    public static final Property exampleResource = m_model.createProperty( "http://rdfs.org/ns/void#exampleResource" );
    
    public static final Property feature = m_model.createProperty( "http://rdfs.org/ns/void#feature" );
    
    /** <p>Points to the void:Dataset that a document is a part of.</p> */
    public static final Property inDataset = m_model.createProperty( "http://rdfs.org/ns/void#inDataset" );
    
    public static final Property linkPredicate = m_model.createProperty( "http://rdfs.org/ns/void#linkPredicate" );
    
    /** <p>The dataset describing the objects of the triples contained in the Linkset.</p> */
    public static final Property objectsTarget = m_model.createProperty( "http://rdfs.org/ns/void#objectsTarget" );
    
    /** <p>An OpenSearch description document for a free-text search service over a void:Dataset.</p> */
    public static final Property openSearchDescription = m_model.createProperty( "http://rdfs.org/ns/void#openSearchDescription" );
    
    /** <p>The total number of distinct properties in a void:Dataset. In other words, 
     *  the number of distinct resources that occur in the predicate position of triples 
     *  in the dataset.</p>
     */
    public static final Property properties = m_model.createProperty( "http://rdfs.org/ns/void#properties" );
    
    /** <p>The rdf:Property that is the predicate of all triples in a property-based 
     *  partition.</p>
     */
    public static final Property property = m_model.createProperty( "http://rdfs.org/ns/void#property" );
    
    /** <p>A subset of a void:Dataset that contains only the triples of a certain rdf:Property.</p> */
    public static final Property propertyPartition = m_model.createProperty( "http://rdfs.org/ns/void#propertyPartition" );
    
    /** <p>A top concept or entry point for a void:Dataset that is structured in a tree-like 
     *  fashion. All resources in a dataset can be reached by following links from 
     *  its root resources in a small number of steps.</p>
     */
    public static final Property rootResource = m_model.createProperty( "http://rdfs.org/ns/void#rootResource" );
    
    public static final Property sparqlEndpoint = m_model.createProperty( "http://rdfs.org/ns/void#sparqlEndpoint" );
    
    /** <p>The dataset describing the subjects of triples contained in the Linkset.</p> */
    public static final Property subjectsTarget = m_model.createProperty( "http://rdfs.org/ns/void#subjectsTarget" );
    
    public static final Property subset = m_model.createProperty( "http://rdfs.org/ns/void#subset" );
    
    /** <p>One of the two datasets linked by the Linkset.</p> */
    public static final Property target = m_model.createProperty( "http://rdfs.org/ns/void#target" );
    
    /** <p>The total number of triples contained in a void:Dataset.</p> */
    public static final Property triples = m_model.createProperty( "http://rdfs.org/ns/void#triples" );
    
    /** <p>Defines a simple URI look-up protocol for accessing a dataset.</p> */
    public static final Property uriLookupEndpoint = m_model.createProperty( "http://rdfs.org/ns/void#uriLookupEndpoint" );
    
    /** <p>Defines a regular expression pattern matching URIs in the dataset.</p> */
    public static final Property uriRegexPattern = m_model.createProperty( "http://rdfs.org/ns/void#uriRegexPattern" );
    
    /** <p>A URI that is a common string prefix of all the entity URIs in a void:Dataset.</p> */
    public static final Property uriSpace = m_model.createProperty( "http://rdfs.org/ns/void#uriSpace" );
    
    /** <p>A vocabulary that is used in the dataset.</p> */
    public static final Property vocabulary = m_model.createProperty( "http://rdfs.org/ns/void#vocabulary" );
    
    /** <p>A set of RDF triples that are published, maintained or aggregated by a single 
     *  provider.</p>
     */
    public static final Resource Dataset = m_model.createResource( "http://rdfs.org/ns/void#Dataset" );
    
    /** <p>A web resource whose foaf:primaryTopic or foaf:topics include void:Datasets.</p> */
    public static final Resource DatasetDescription = m_model.createResource( "http://rdfs.org/ns/void#DatasetDescription" );
    
    /** <p>A collection of RDF links between two void:Datasets.</p> */
    public static final Resource Linkset = m_model.createResource( "http://rdfs.org/ns/void#Linkset" );
    
    /** <p>A technical feature of a void:Dataset, such as a supported RDF serialization 
     *  format.</p>
     */
    public static final Resource TechnicalFeature = m_model.createResource( "http://rdfs.org/ns/void#TechnicalFeature" );
    
}
