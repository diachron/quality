package de.unibonn.iai.eis.diachron.configuration;

/**
 * Configure data set file name for specific test case
 * 
 * @author Muhammad Ali Qasmi
 * @date 18th March 2014
 */
public class DataSetMappingForTestCase {
	
	// For ...qualitymetric.intrinsic.accuracy
	public static String MalformedDatatypeLiterals = "testdumps/chembl-rdf-void.ttl";
	public static String IncompatibleDatatypeRange = "testdumps/chembl-rdf-void_2.ttl";
	// For...qualitymetric.intrinsic.consistency
	public static String UndefinedClassesOrProperties = "testdumps/SampleInput_UndefinedClassesOrProperties.ttl";
	public static String MisplacedClassesOrProperties = "testdumps/SampleInput_MisplacedClassesOrProperties.ttl";
	public static String MisuseOwlDataTypeOrObjectProperties = "testdumps/SampleInput_MisusedOwlDatatypeObjectProperty.ttl";
	public static String HomogeneousDatatypes = "testdumps/SampleInput_HomogeneousDatatypes.ttl";
	public static String OntologyHijacking = "testdumps/SampleInput_OntologyHijacking.ttl";
	// For...qualitymetric.intrinsic.conciseness
	public static String DuplicateInstance = "testdumps/SampleInput_DuplicateInstance.ttl";
	// For...qualitymetrics.dynamicity.currency.CurrencyDocumentStatements
	public static String CurrencyDocumentStatements = "testdumps/SampleInput_Currency.ttl";
	// For...qualitymetrics.accessibility.security.HTTPSDataAccessTest
	public static String SecureDataAccess = "testdumps/SampleInput_HttpsAccess.ttl";
	// For..qualitymetric.reputation
	public static String ReputationOfDataset = "testdumps/SampleInput_ReputationOfDataset.ttl";
	public static String OBOFoundry = "testdumps/SampleInput_OBOFoundry.owl";
	// For ..qualitymetrics.representational.understandability
	public static String EmptyAnnotationValue = "testdumps/SampleInput_EmptyAnnotationValue.ttl";
	public static String WhitespaceInAnnotation = "testdumps/SampleInput_WhitespaceInAnnotation.ttl";
	public static String LabelsUsingCapitals = "testdumps/SampleInput_LabelsUsingCapitals.ttl";
	// For ..qualitymetrics.accessibility.availability
	public static String Dereferenceability = "testdumps/SampleInput_PleiadesShort.ttl";
	public static String MisreportedContentType = "testdumps/SampleInput_PleiadesShort.ttl";
	// For ..qualitymetrics.accessibility.performance
	public static String DataSourceScalability = "testdumps/SampleInput_DrugBank.ttl";
	public static String HighThroughput = "testdumps/SampleInput_DrugBank.ttl";
	public static String LowLatencyTest = "testdumps/SampleInput_DrugBank.ttl";
	public static String CorrectURIUsage = "testdumps/SampleInput_DrugBank.ttl";
}
