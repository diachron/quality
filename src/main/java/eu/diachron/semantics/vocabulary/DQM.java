/* CVS $Id: $ */
package eu.diachron.semantics.vocabulary; 
import com.hp.hpl.jena.rdf.model.*;
 
/**
 * Vocabulary definitions from src/main/resources/vocabularies/dqm/dqm.trig 
 * @author Auto-generated by schemagen on 02 Oct 2014 20:31 
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
    
    /** <p>Links Intrinsic Category with Consistency Dimension</p> */
    public static final Property hasConsistencyDimension = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasConsistencyDimension" );
    
    /** <p>Links the Dynamicity Category with the Currency Dimension</p> */
    public static final Property hasCurrencyDimension = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasCurrencyDimension" );
    
    /** <p>Links the Currency Dimension with the Currency of Document/Statements Metric 
     *  (Currency of Document/Statements Metric belongs to the Currency Dimension)</p>
     */
    public static final Property hasCurrencyOfDocumentStatementsMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasCurrencyOfDocumentStatementsMetric" );
    
    /** <p>Links Accuracy Dimension with POBO Definition Usage Metric</p> */
    public static final Property hasDefinedOntologyAuthorMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasDefinedOntologyAuthorMetric" );
    
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
    
    /** <p>Links Understandability Dimension with Empty annotation value metric</p> */
    public static final Property hasEmptyAnnotationValueMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasEmptyAnnotationValueMetric" );
    
    /** <p>Links Availability Dimension with Endpoint Availability Metric (Endpoint Availability 
     *  Metric belongs to the Availability Dimension)</p>
     */
    public static final Property hasEndPointAvailabilityMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasEndPointAvailabilityMetric" );
    
    /** <p>Links Consistency Dimension with ‘entities as members of disjoint classes’ 
     *  Metric (Entities as Members of Disjoint Classes Metric belongs to the Consistency 
     *  Dimension)</p>
     */
    public static final Property hasEntitiesAsMembersOfDisjointClassesMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasEntitiesAsMembersOfDisjointClassesMetric" );
    
    /** <p>Links the Currency Dimension with the Exclusion of Outdated Data Metric (Exclusion 
     *  of Outdated Data Metric belongs to the Currency Dimension)</p>
     */
    public static final Property hasExclusionOfOutdatedDataMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasExclusionOfOutdatedDataMetric" );
    
    /** <p>Links Conciseness Dimension with Extensional Conciseness Metric (Extensional 
     *  Conciseness Metric belongs to the Conciseness Dimension)</p>
     */
    public static final Property hasExtensionalConcisenessMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasExtensionalConcisenessMetric" );
    
    /** <p>Links security dimension with access to data through HTTPS metric</p> */
    public static final Property hasHTTPSDataAccessMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasHTTPSDataAccessMetric" );
    
    /** <p>Links Performance Dimension with High Throughput Metric (High Throughput Metric 
     *  belongs to the Performance Dimension)</p>
     */
    public static final Property hasHighThroughputMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasHighThroughputMetric" );
    
    /** <p>Links Consistency Dimension with Homogeneous data types Metric</p> */
    public static final Property hasHomogeneousDatatypesMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasHomogeneousDatatypesMetric" );
    
    /** <p>Links Understandability Dimension with Human Readable Labelling Metric (Human 
     *  Readable Labelling belongs to the Understandability Dimension)</p>
     */
    public static final Property hasHumanReadableLabellingMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasHumanReadableLabellingMetric" );
    
    /** <p>Links Accuracy Dimension with Incompatible Datatype Range Metric</p> */
    public static final Property hasIncompatibleDatatypeRangeMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasIncompatibleDatatypeRangeMetric" );
    
    /** <p>Links Conciseness Dimension with Intensional Conciseness Metric (Intensional 
     *  Conciseness Metric belongs to the Conciseness Dimension)</p>
     */
    public static final Property hasIntensionalConcisenessMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasIntensionalConcisenessMetric" );
    
    /** <p></p> */
    public static final Property hasInterlinkDetectionMetricMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasInterlinkDetectionMetricMetric" );
    
    /** <p>Links Accessibility Category with Interlink Dimension</p> */
    public static final Property hasInterlinkDimension = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasInterlinkDimension" );
    
    /** <p>Links Understandability Dimension with labels using capitals metric</p> */
    public static final Property hasLabelsUsingCapitalsMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasLabelsUsingCapitalsMetric" );
    
    /** <p>Links Accessibility Category with Licensing Dimension</p> */
    public static final Property hasLicensingDimension = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasLicensingDimension" );
    
    /** <p>Links Understandability Dimension with Low Blank Nodes Usage Metric</p> */
    public static final Property hasLowBlankNodesUsageMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasLowBlankNodesUsageMetric" );
    
    /** <p>Links the Performance Dimension with Low Latency Metric (Low Latency Metric 
     *  belongs to the Performance Dimension)</p>
     */
    public static final Property hasLowLatencyMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasLowLatencyMetric" );
    
    /** <p>Links Licensing Dimension with Machine-readable Indication of a License Metric 
     *  (Machine-readable Indication of a License Metric belongs to the Licensing 
     *  Dimension)</p>
     */
    public static final Property hasMachineReadableLicenseMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasMachineReadableLicenseMetric" );
    
    /** <p>Links Accuracy Dimension with Malformed Data Type Literals Metric (Malformed 
     *  Data Type Literals Metric belongs to the Accuracy Dimension)</p>
     */
    public static final Property hasMalformedDatatypeLiteralsMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasMalformedDatatypeLiteralsMetric" );
    
    /** <p>Links Consistency Dimension with misplaced classes or properties metric</p> */
    public static final Property hasMisplacedClassesOrPropertiesMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasMisplacedClassesOrPropertiesMetric" );
    
    /** <p>Links Availability Dimension with Misreported Content Types Metric (Misreported 
     *  Content Types Metric belongs to the Availability Dimension)</p>
     */
    public static final Property hasMisreportedContentTypesMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasMisreportedContentTypesMetric" );
    
    /** <p>Links Consistency Dimension with Misuse Owl- datatype or object properties 
     *  metric</p>
     */
    public static final Property hasMisuseOwlDatatypeOrObjectPropertiesMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasMisuseOwlDatatypeOrObjectPropertiesMetric" );
    
    /** <p>Links Reputation category with OBO foundry metric</p> */
    public static final Property hasOBOFoundryMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasOBOFoundryMetric" );
    
    /** <p>Links Consistency Dimension with Obsolete Concepts In Ontology Metric</p> */
    public static final Property hasObsoleteConceptsInOntologyMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasObsoleteConceptsInOntologyMetric" );
    
    /** <p>Links Conciseness Dimension with Ontology Versioning Conciseness Metric</p> */
    public static final Property hasOntologyVersionConcisenessMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasOntologyVersionConcisenessMetric" );
    
    /** <p>Links Accuracy Dimension with POBO Definition Usage Metric</p> */
    public static final Property hasPOBODefinitionUsageMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasPOBODefinitionUsageMetric" );
    
    /** <p>Links Accessibility Category with Performance Dimension</p> */
    public static final Property hasPerformanceDimension = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasPerformanceDimension" );
    
    /** <p>Links Availability Dimension with RDF Availability Metric (RDF Availability 
     *  Metric belongs to the Availability Dimension)</p>
     */
    public static final Property hasRDFAvailabilityMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasRDFAvailabilityMetric" );
    
    /** <p>Links Representational Category with Understandability DimensionLinks Representational 
     *  Category with Representational-Conciseness Dimension</p>
     */
    public static final Property hasRepresentationalConcisenessDimension = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasRepresentationalConcisenessDimension" );
    
    /** <p>Links Reputation category with Reputation of dataset metric</p> */
    public static final Property hasReputationOfDatasetMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasReputationOfDatasetMetric" );
    
    /** <p>Links Performance Dimension with Scalability of a Data Source Metric (Scalability 
     *  of a Data Source Metric belongs to the Performance Dimension)</p>
     */
    public static final Property hasScalabilityOfDataSourceMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasScalabilityOfDataSourceMetric" );
    
    /** <p>Links Accessibility Category with Security Dimension</p> */
    public static final Property hasSecurityDimension = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasSecurityDimension" );
    
    /** <p>Links Representational-Conciseness Dimension with Keeping URIs Short Metric 
     *  (Keeping URIs Short Metric belongs to the Representational-Conciseness Dimension)</p>
     */
    public static final Property hasShortURIsMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasShortURIsMetric" );
    
    /** <p>Links Accuracy Dimension with Synonym Usage Metric</p> */
    public static final Property hasSynonymUsageMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasSynonymUsageMetric" );
    
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
    
    /** <p>Links Consistency Dimension with Undefined classes metric</p> */
    public static final Property hasUndefinedClassesMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasUndefinedClassesMetric" );
    
    /** <p>Links Consistency Dimension with Undefined properties metric</p> */
    public static final Property hasUndefinedPropertiesMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasUndefinedPropertiesMetric" );
    
    /** <p>Links Availability Dimension with Unstructured Data Metric (Unstructured Data 
     *  Metric belongs to the Availability Dimension)</p>
     */
    public static final Property hasUnstructuredMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasUnstructuredMetric" );
    
    /** <p>Links the Dynamicity Category with the Volatility Dimension</p> */
    public static final Property hasVolatilityDimension = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasVolatilityDimension" );
    
    /** <p>Links Understandability Dimension with Whitespace in annotation</p> */
    public static final Property hasWhitespaceInAnnotationMetric = m_model.createProperty( "http://www.diachron-fp7.eu/dqm#hasWhitespaceInAnnotationMetric" );
    
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
    
    /** <p>Consistency means that a knowledge base is free of (logical/formal) contradictions 
     *  with respect to particular knowledge representation and inference mechanisms</p>
     */
    public static final Resource Consistency = m_model.createResource( "http://www.diachron-fp7.eu/dqm#Consistency" );
    
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
    
    /** <p>Checks whether the creator &lt;efo:creator&gt; is defined in the ontology.</p> */
    public static final Resource DefinedOntologyAuthorMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#DefinedOntologyAuthorMetric" );
    
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
    
    /** <p>EmptyAnnotationValue consider the following widely used annotation properties 
     *  (labels, comments, notes, etc.) and identifies triples whose property is from 
     *  a pre-configured list of annotation properties, and whose object is an empty 
     *  string</p>
     */
    public static final Resource EmptyAnnotationValueMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#EmptyAnnotationValueMetric" );
    
    /** <p>Checks whether the server (endpoint) is reachable and responds to a SPARQL 
     *  query</p>
     */
    public static final Resource EndPointAvailabilityMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#EndPointAvailabilityMetric" );
    
    /** <p>the ratio of entities described as members of disjoint classes (here: classes 
     *  explicitly known as disjoint) to the total number of entities described in 
     *  the dataset</p>
     */
    public static final Resource EntitiesAsMembersOfDisjointClassesMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#EntitiesAsMembersOfDisjointClassesMetric" );
    
    /** <p>Provides a measure of the degree to which information is up to date, by comparing 
     *  the total number of resources, described by the dataset, versus the number 
     *  of those that are recognized to be outdated</p>
     */
    public static final Resource ExclusionOfOutdatedDataMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#ExclusionOfOutdatedDataMetric" );
    
    /** <p>Provides a measure of the redundancy of the dataset at the data level, computed 
     *  as the ratio of the Number of Unique Subjects to the Total Number of Subjects</p>
     */
    public static final Resource ExtensionalConcisenessMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#ExtensionalConcisenessMetric" );
    
    /** <p>Verifies if access to the dataset is performed through a sound, HTTPS/SSL 
     *  connection and thus, connection is confidential</p>
     */
    public static final Resource HTTPSDataAccessMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#HTTPSDataAccessMetric" );
    
    /** <p>Measures the number of answered HTTP requests responsed by the source of the 
     *  dataset, per second</p>
     */
    public static final Resource HighThroughputMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#HighThroughputMetric" );
    
    /** <p>Detects outliers or conflicts of literal datatypes. Since it is meant to measure 
     *  the homogeneity and not the validity possible rdfs:range restrictions are 
     *  not evaluated</p>
     */
    public static final Resource HomogeneousDatatypesMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#HomogeneousDatatypesMetric" );
    
    /** <p>Assesses the percentage of entities having an rdfs:label or rdfs:comment</p> */
    public static final Resource HumanReadableLabellingMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#HumanReadableLabellingMetric" );
    
    /** <p>Detects literals incompatible with range data type</p> */
    public static final Resource IncompatibleDatatypeRangeMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#IncompatibleDatatypeRangeMetric" );
    
    /** <p>Provides a measure of the redundancy of the dataset at the schema level, computed 
     *  as the ratio of the Number of Unique Attributes (Properties) to the Total 
     *  Number of Attributes in the target schema</p>
     */
    public static final Resource IntensionalConcisenessMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#IntensionalConcisenessMetric" );
    
    /** <p>This is a complex metric that detects good quality interlinks by measuring 
     *  the (i) interlink degree; (ii) clustering coefficient; (iii) centrality; (iv) 
     *  open sameAs chains; (v) sameAs description richness</p>
     */
    public static final Resource InterlinkDetectionMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#InterlinkDetectionMetric" );
    
    /** <p>Refers to the degree to which entities that represent the same concept are 
     *  linked to each other, be it within or between two or more data sources.</p>
     */
    public static final Resource Interlinking = m_model.createResource( "http://www.diachron-fp7.eu/dqm#Interlinking" );
    
    /** <p>Refers to dimensions that are independent of the user's context and that measure 
     *  the correctness, succinctness and consistency of the dataset</p>
     */
    public static final Resource Intrinsic = m_model.createResource( "http://www.diachron-fp7.eu/dqm#Intrinsic" );
    
    /** <p>Labels using capitals identifies triples whose property is from a pre-configured 
     *  list of label properties, and whose object uses a bad style of capitalization</p>
     */
    public static final Resource LabelsUsingCapitalsMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#LabelsUsingCapitalsMetric" );
    
    /** <p>Indicates whether consumers of the dataset are explicitly granted permissions 
     *  to use it, under defined conditions (if any)</p>
     */
    public static final Resource Licensing = m_model.createResource( "http://www.diachron-fp7.eu/dqm#Licensing" );
    
    /** <p>Provides a measure for the usage of blank nodes in a dataset</p> */
    public static final Resource LowBlankNodesUsageMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#LowBlankNodesUsageMetric" );
    
    /** <p>Measures the delay between the submission of a request to the data source 
     *  and the reception of the respective response (or the first part of it)</p>
     */
    public static final Resource LowLatencyMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#LowLatencyMetric" );
    
    /** <p>Verifies that the resource is annotated with a machine-readable indication 
     *  of the license (e.g. a VoID description or dcterms:license property)</p>
     */
    public static final Resource MachineReadableLicenseMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#MachineReadableLicenseMetric" );
    
    /** <p>Detects ill-typed literals, which do not abide by the lexical syntax of their 
     *  respective datatypes</p>
     */
    public static final Resource MalformedDatatypeLiteralsMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#MalformedDatatypeLiteralsMetric" );
    
    /** <p>Find resources that are - defined as a property but also appear on subject 
     *  or object positions in other triples (except cases like ex:prop rdf:type rdfs:Property, 
     *  ex:prop rds:subPropetyOf) - defined as a class but also appear on predicate 
     *  position in other triples. The metric is computed as a ratio of misplaced 
     *  classes and properties</p>
     */
    public static final Resource MisplacedClassesOrPropertiesMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#MisplacedClassesOrPropertiesMetric" );
    
    /** <p>Checks whether the content is suitable for consumption and if such content 
     *  should be accessed</p>
     */
    public static final Resource MisreportedContentTypesMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#MisreportedContentTypesMetric" );
    
    public static final Resource MisuseOwlDatatypeOrObjectPropertiesMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#MisuseOwlDatatypeOrObjectPropertiesMetric" );
    
    /** <p>Detect properties that are defined as a owl:datatype property but is used 
     *  as object property and properties defined as a owl:object property and used 
     *  as datatype property The metric is computed as a ratio of misused properties</p>
     */
    public static final Resource MisusedOwlDatatypeOrObjectPropertiesMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#MisusedOwlDatatypeOrObjectPropertiesMetric" );
    
    /** <p>Detects non reputable resources by retrieving URI of resources from data sets 
     *  and comparing them with URI found in reputable resources.</p>
     */
    public static final Resource OBOFoundryMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#OBOFoundryMetric" );
    
    /** <p>Provides a measure for the number of classes and properties in an ontology 
     *  which are marked as depricated. If an ontology is making lots of obsolete 
     *  concepts between different versions, then this is an indicator that the ontology 
     *  is going through a lot of changes, and is potentially in a state of poor quality</p>
     */
    public static final Resource ObsoleteConceptsInOntologyMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#ObsoleteConceptsInOntologyMetric" );
    
    /** <p>Detects the redefinition by third parties of external classes/properties such 
     *  that reasoning over data using those external terms is affected</p>
     */
    public static final Resource OntologyHijackingMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#OntologyHijackingMetric" );
    
    /** <p>Provides a measure for checking singleton instances of the use-case specific 
     *  defined owl:ontologyVersion property</p>
     */
    public static final Resource OntologyVersionConcisenessMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#OntologyVersionConcisenessMetric" );
    
    /** <p>Provides a measure for an Ontology checking the usage of &lt;pobo:def&gt; 
     *  in defined classes.</p>
     */
    public static final Resource POBODefinitionUsageMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#POBODefinitionUsageMetric" );
    
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
    
    /** <p>Reputation dimensions related to the reputation of the data, which provide 
     *  information about how the reputable the data is based on its providers and 
     *  host</p>
     */
    public static final Resource Reputation = m_model.createResource( "http://www.diachron-fp7.eu/dqm#Reputation" );
    
    /** <p>Detects non reputable resources by retrieving URI of resources from data sets 
     *  and comparing them with URI found in reputable resources.</p>
     */
    public static final Resource ReputationOfDatasetMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#ReputationOfDatasetMetric" );
    
    /** <p>Determines whether the time required to answer a set of N requests divided 
     *  by N, is not longer than the time it takes to answer a single request</p>
     */
    public static final Resource ScalabilityOfDataSourceMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#ScalabilityOfDataSourceMetric" );
    
    /** <p>Refers to the extent to which data is protected against illegal alteration 
     *  and it's authenticity can be guaranteed and to the confidentiality of the 
     *  communication between a source and its consumers</p>
     */
    public static final Resource Security = m_model.createResource( "http://www.diachron-fp7.eu/dqm#Security" );
    
    /** <p>Detects the use of short URIs (and the avoidance of query parameters), which 
     *  suggests that information is compactly represented and favors readability</p>
     */
    public static final Resource ShortURIsMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#ShortURIsMetric" );
    
    /** <p>Measures the number of classes which has a synonym &lt;efo:alternative_term&gt; 
     *  described.</p>
     */
    public static final Resource SynonymUsageMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#SynonymUsageMetric" );
    
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
    
    /** <p>Detects undefined classes</p> */
    public static final Resource UndefinedClassesMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#UndefinedClassesMetric" );
    
    /** <p>Detects undefined properties</p> */
    public static final Resource UndefinedPropertiesMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#UndefinedPropertiesMetric" );
    
    /** <p>Refers to the easy with which data can be compreshended without ambiguity 
     *  and used by a human data consumer</p>
     */
    public static final Resource Understandability = m_model.createResource( "http://www.diachron-fp7.eu/dqm#Understandability" );
    
    /** <p>Detects dead links or URIs without any supporting RDF metadata or no redirection 
     *  using the status code 303</p>
     */
    public static final Resource UnstructuredMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#UnstructuredMetric" );
    
    /** <p>Refers to the frequency with which data varies in time</p> */
    public static final Resource Volatility = m_model.createResource( "http://www.diachron-fp7.eu/dqm#Volatility" );
    
    /** <p>Whitespace in annotation consider the following widely used annotation properties 
     *  (labels, comments, notes, etc.) and identifies triples whose property is from 
     *  a pre-configured list of annotation properties, and whose object value has 
     *  leading or ending white space in string.</p>
     */
    public static final Resource WhitespaceInAnnotationMetric = m_model.createResource( "http://www.diachron-fp7.eu/dqm#WhitespaceInAnnotationMetric" );
    
}
