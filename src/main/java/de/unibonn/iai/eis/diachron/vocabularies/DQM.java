/* CVS $Id: $ */
package de.unibonn.iai.eis.diachron.vocabularies; 
import com.hp.hpl.jena.rdf.model.*;
 
/**
 * Vocabulary definitions from src/main/resources/vocabularies/dqm/dqm.trig 
 * @author Auto-generated by schemagen on 12 May 2014 17:14 
 */
public class DQM {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://www.diachron-fp7.eu/dqm#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    /** <p>Links Intrinsic Category with Accuracy Dimension</p> */
    public static final Property hasAccuracyDimension = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasAccuracyDimension" );
    
    /** <p>Links Contextual Category with Amount of Data Dimension</p> */
    public static final Property hasAmountOfDataDimension = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasAmountOfDataDimension" );
    
    /** <p>Links Amount of Data Dimension with Amount of Triples Metric (Amount of Triples 
     *  Metric belongs to the Amount of Data Dimension)</p>
     */
    public static final Property hasAmountOfTriplesMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasAmountOfTriplesMetric" );
    
    /** <p>Links Accessibility Category with Availability Dimension</p> */
    public static final Property hasAvailabilityDimension = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasAvailabilityDimension" );
    
    /** <p>Links Intrinsic Category with Conciseness Dimension</p> */
    public static final Property hasConcisenessDimension = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasConcisenessDimension" );
    
    /** <p>Links the Dynamicity Category with the Currency Dimension</p> */
    public static final Property hasCurrencyDimension = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasCurrencyDimension" );
    
    /** <p>Links the Currency Dimension with the Currency of Document/Statements Metric 
     *  (Currency of Document/Statements Metric belongs to the Currency Dimension)</p>
     */
    public static final Property hasCurrencyOfDocumentStatementsMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasCurrencyOfDocumentStatementsMetric" );
    
    /** <p>Links Availability Dimension with Dereferenceability of Back-links Metric 
     *  (Dereferenceability of Back-links Metric belongs to the Availability Dimension)</p>
     */
    public static final Property hasDereferenceabilityBackLinksMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasDereferenceabilityBackLinksMetric" );
    
    /** <p>Links Availability Dimension with Dereferenceability of Forward-links Metric 
     *  (Dereferenceability of Forward-links Metric belongs to the Availability Dimension)</p>
     */
    public static final Property hasDereferenceabilityForwardLinksMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasDereferenceabilityForwardLinksMetric" );
    
    /** <p>Links Availability Dimension with Dereferenceability Metric (Dereferenceability 
     *  Metric belongs to the Availability Dimension)</p>
     */
    public static final Property hasDereferenceabilityMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasDereferenceabilityMetric" );
    
    /** <p>Links Conciseness Dimension with Duplicate Instance Metric (Duplicate Instance 
     *  Metric belongs to the Conciseness Dimension)</p>
     */
    public static final Property hasDuplicateInstanceMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasDuplicateInstanceMetric" );
    
    /** <p>Links Availability Dimension with Endpoint Availability Metric (Endpoint Availability 
     *  Metric belongs to the Availability Dimension)</p>
     */
    public static final Property hasEndPointAvailabilityMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasEndPointAvailabilityMetric" );
    
    /** <p>Links the Currency Dimension with the Exclusion of Outdated Data Metric (Exclusion 
     *  of Outdated Data Metric belongs to the Currency Dimension)</p>
     */
    public static final Property hasExclusionOfOutdatedDataMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasExclusionOfOutdatedDataMetric" );
    
    /** <p>Links Conciseness Dimension with Extensional Conciseness Metric (Extensional 
     *  Conciseness Metric belongs to the Conciseness Dimension)</p>
     */
    public static final Property hasExtensionalConcisenessMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasExtensionalConcisenessMetric" );
    
    /** <p>Links Conciseness Dimension with Intensional Conciseness Metric (Intensional 
     *  Conciseness Metric belongs to the Conciseness Dimension)</p>
     */
    public static final Property hasIntensionalConcisenessMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasIntensionalConcisenessMetric" );
    
    /** <p>Links Accessibility Dimension with Low Latency Metric (Low Latency Metric 
     *  belongs to the Accessibility Dimension)</p>
     */
    public static final Property hasLowLatencyMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasLowLatencyMetric" );
    
    /** <p>Links Accuracy Dimension with Malformed Data Type Literals Metric (Malformed 
     *  Data Type Literals Metric belongs to the Accuracy Dimension)</p>
     */
    public static final Property hasMalformedDatatypeLiteralsMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasMalformedDatatypeLiteralsMetric" );
    
    /** <p>Links Availability Dimension with Misreported Content Types Metric (Misreported 
     *  Content Types Metric belongs to the Availability Dimension)</p>
     */
    public static final Property hasMisreportedContentTypesMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasMisreportedContentTypesMetric" );
    
    /** <p>Links Conciseness Dimension with Ontology Versioning Conciseness Metric</p> */
    public static final Property hasOntologyVersionConcisenessMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasOntologyVersionConcisenessMetric" );
    
    /** <p>Links Accessibility Category with Performance Dimension</p> */
    public static final Property hasPerformanceDimension = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasPerformanceDimension" );
    
    /** <p>Links Availability Dimension with RDF Availability Metric (RDF Availability 
     *  Metric belongs to the Availability Dimension)</p>
     */
    public static final Property hasRDFAvailabilityMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasRDFAvailabilityMetric" );
    
    /** <p>Links Representational Category with Representational-Conciseness Dimension</p> */
    public static final Property hasRepresentationalConcisenessDimension = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasRepresentationalConcisenessDimension" );
    
    /** <p>Links Representational-Conciseness Dimension with Keeping URIs Short Metric 
     *  (Keeping URIs Short Metric belongs to the Representational-Conciseness Dimension)</p>
     */
    public static final Property hasShortURIsMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasShortURIsMetric" );
    
    /** <p>Links the Currency Dimension with the Time Since Modification Metric (Time 
     *  Since Modification Metric belongs to the Currency Dimension)</p>
     */
    public static final Property hasTimeSinceModificationMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasTimeSinceModificationMetric" );
    
    /** <p>Links the Volatility Dimension with the Time Validity Interval Metric (Time 
     *  Validity Interval Metric belongs to the Volatility Dimension)</p>
     */
    public static final Property hasTimeValidityIntervalMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasTimeValidityIntervalMetric" );
    
    /** <p>Links the Dynamicity Category with the Timeliness Dimension</p> */
    public static final Property hasTimelinessDimension = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasTimelinessDimension" );
    
    /** <p>Links the Timeliness Dimension with the Timeliness of the Resource Metric 
     *  (Timeliness of the Resource Metric belongs to the Timeliness Dimension)</p>
     */
    public static final Property hasTimelinessOfResourceMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasTimelinessOfResourceMetric" );
    
    /** <p>Links Availability Dimension with Unstructured Data Metric (Unstructured Data 
     *  Metric belongs to the Availability Dimension)</p>
     */
    public static final Property hasUnstructuredMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasUnstructuredMetric" );
    
    /** <p>Links the Dynamicity Category with the Volatility Dimension</p> */
    public static final Property hasVolatilityDimension = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasVolatilityDimension" );
    
    /** <p>Encompasses dimensions related to the ability to access to and retrieve the 
     *  dataset required for a particular use case</p>
     */
    public static final Resource Accessibility = m_model.createResource( "http://www.diachron-fp7.eu/dqm#Accessibility" );
    
    /** <p>Extent to which data is correct. The degree to which the dataset correctly 
     *  represents the real world and how much it is free of syntax errors. Classified 
     *  in: Syntactic accuracy (the degree to which data values are close to their 
     *  definition domain) and Semantic accuracy (the degree to which data values 
     *  correctly represent the actual real world values)</p>
     */
    public static final Resource Accuracy = m_model.createResource( "http://www.diachron-fp7.eu/dqm#Accuracy" );
    
    /** <p>Refers to how appropriate the quantity and volume of data is for the task 
     *  at hand. The amount of data should be enough to approximate the true scenario 
     *  precisely</p>
     */
    public static final Resource AmountOfData = m_model.createResource( "http://www.diachron-fp7.eu/dqm#AmountOfData" );
    
    /** <p>Counts the total number of triples present in the dataset</p> */
    public static final Resource AmountOfTriplesMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#AmountOfTriplesMetric" );
    
    /** <p>Metrics in this dimension measure the extent to which information (or some 
     *  portion of it) is present, obtainable and ready for use</p>
     */
    public static final Resource Availability = m_model.createResource( "http://www.diachron-fp7.eu/dqm#Availability" );
    
    /** <p>Refers to the redundancy of entities, be it at the schema or the data level</p> */
    public static final Resource Conciseness = m_model.createResource( "http://www.diachron-fp7.eu/dqm#Conciseness" );
    
    /** <p>Comprises dimensions that highly depend on the context of the task to be performed</p> */
    public static final Resource Contextual = m_model.createResource( "http://www.diachron-fp7.eu/dqm#Contextual" );
    
    /** <p>Provides a measure of the degree to which information is up to date and how 
     *  promptly the data is updated</p>
     */
    public static final Resource Currency = m_model.createResource( "http://www.diachron-fp7.eu/dqm#Currency" );
    
    /** <p>Compares the time when the data was observed with the time when the data (document 
     *  as a whole and individual triples) were last modified, thereby providing a 
     *  measure of how current the dataset is</p>
     */
    public static final Resource CurrencyOfDocumentStatementsMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#CurrencyOfDocumentStatementsMetric" );
    
    /** <p>Detects all local in-links or back-links (locally available triples with the 
     *  resource URI appearing as object, in the dereferenced document returned for 
     *  the resource)</p>
     */
    public static final Resource DereferenceabilityBackLinksMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#DereferenceabilityBackLinksMetric" );
    
    /** <p>Detects all forward links (locally known triples where the local URI is mentioned 
     *  in the subject)</p>
     */
    public static final Resource DereferenceabilityForwardLinksMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#DereferenceabilityForwardLinksMetric" );
    
    /** <p>Determines whether requesting a resource, identified by a URI, results in 
     *  an error code being returned (e.g. 4xx: client error or 5xx: server error), 
     *  or in a broken link</p>
     */
    public static final Resource DereferenceabilityMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#DereferenceabilityMetric" );
    
    /** <p>Provides a measure of the redundancy of entities at the data level (ratio 
     *  of entities that violate the uniqueness rule)</p>
     */
    public static final Resource DuplicateInstanceMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#DuplicateInstanceMetric" );
    
    /** <p>Dimensions in this category provide information about how current is the data 
     *  (i.e. its freshness over time) and how frequently it changes</p>
     */
    public static final Resource Dynamicity = m_model.createResource( "http://www.diachron-fp7.eu/dqm#Dynamicity" );
    
    /** <p>Checks whether the server (endpoint) is reachable and responds to a SPARQL 
     *  query</p>
     */
    public static final Resource EndPointAvailabilityMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#EndPointAvailabilityMetric" );
    
    /** <p>Provides a measure of the degree to which information is up to date, by comparing 
     *  the total number of resources, described by the dataset, versus the number 
     *  of those that are recognized to be outdated</p>
     */
    public static final Resource ExclusionOfOutdatedDataMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#ExclusionOfOutdatedDataMetric" );
    
    /** <p>Provides a measure of the redundancy of the dataset at the data level, computed 
     *  as the ratio of the Number of Unique Subjects to the Total Number of Subjects</p>
     */
    public static final Resource ExtensionalConcisenessMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#ExtensionalConcisenessMetric" );
    
    /** <p>Provides a measure of the redundancy of the dataset at the schema level, computed 
     *  as the ratio of the Number of Unique Attributes (Properties) to the Total 
     *  Number of Attributes in the target schema</p>
     */
    public static final Resource IntensionalConcisenessMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#IntensionalConcisenessMetric" );
    
    /** <p>Refers to dimensions that are independent of the user's context and that measure 
     *  the correctness, succinctness and consistency of the dataset</p>
     */
    public static final Resource Intrinsic = m_model.createResource( "http://www.diachron-fp7.eu/dqm#Intrinsic" );
    
    /** <p>Measures the delay between the submission of a request to the data source 
     *  and the reception of the respective response (or the first part of it)</p>
     */
    public static final Resource LowLatencyMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#LowLatencyMetric" );
    
    /** <p>Detects ill-typed literals, which do not abide by the lexical syntax of their 
     *  respective datatypes</p>
     */
    public static final Resource MalformedDatatypeLiteralsMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#MalformedDatatypeLiteralsMetric" );
    
    /** <p>Checks whether the content is suitable for consumption and if such content 
     *  should be accessed</p>
     */
    public static final Resource MisreportedContentTypesMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#MisreportedContentTypesMetric" );
    
    /** <p>Provides a measure for checking singleton instances of the use-case specific 
     *  defined owl:ontologyVersion property</p>
     */
    public static final Resource OntologyVersionConcisenessMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#OntologyVersionConcisenessMetric" );
    
    /** <p>Assesses the efficiency with which a system can bind to the dataset and get 
     *  access to the information contained into it</p>
     */
    public static final Resource Performance = m_model.createResource( "http://www.diachron-fp7.eu/dqm#Performance" );
    
    /** <p>Upon request of an RDF dump, checks whether it is provided as result</p> */
    public static final Resource RDFAvailabilityMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#RDFAvailabilityMetric" );
    
    /** <p>Groups dimensions related to the design of the data, which provide information 
     *  about how the chosen representation of the data affect its quality</p>
     */
    public static final Resource Representational = m_model.createResource( "http://www.diachron-fp7.eu/dqm#Representational" );
    
    /** <p>Measures the extent to which the representation of data is compact, clear 
     *  and well formatted</p>
     */
    public static final Resource RepresentationalConciseness = m_model.createResource( "http://www.diachron-fp7.eu/dqm#RepresentationalConciseness" );
    
    /** <p>Detects the use of short URIs (and the avoidance of query parameters), which 
     *  suggests that information is compactly represented and favors readability</p>
     */
    public static final Resource ShortURIsMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#ShortURIsMetric" );
    
    /** <p>Provides a measure of the degree to which information is up to date, by comparing 
     *  the current time with the time when the data was last modified</p>
     */
    public static final Resource TimeSinceModificationMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#TimeSinceModificationMetric" );
    
    /** <p>Measures the frequency with which data varies over time, by calculating the 
     *  length of the time interval during which data remains valid.</p>
     */
    public static final Resource TimeValidityIntervalMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#TimeValidityIntervalMetric" );
    
    /** <p>Indicates how up-to-date the data is, relative to the specific task at hand</p> */
    public static final Resource Timeliness = m_model.createResource( "http://www.diachron-fp7.eu/dqm#Timeliness" );
    
    /** <p>Assesses the difference between the last modified time of the original data 
     *  source and the last modified time of the semantic web source, thereby indicating 
     *  if the resource is outdated</p>
     */
    public static final Resource TimelinessOfResourceMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#TimelinessOfResourceMetric" );
    
    /** <p>Dimensions belonging to this group focus on the perceived trustworthiness 
     *  of the dataset and its sources</p>
     */
    public static final Resource Trust = m_model.createResource( "http://www.diachron-fp7.eu/dqm#Trust" );
    
    /** <p>Detects dead links or URIs without any supporting RDF metadata or no redirection 
     *  using the status code 303</p>
     */
    public static final Resource UnstructuredMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#UnstructuredMetric" );
    
    /** <p>Refers to the frequency with which data varies in time</p> */
    public static final Resource Volatility = m_model.createResource( "http://www.diachron-fp7.eu/dqm#Volatility" );
    
}
