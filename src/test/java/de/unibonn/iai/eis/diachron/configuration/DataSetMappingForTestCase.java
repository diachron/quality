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
	public static String MisuseOwlDataTypeOrObjectProperties = "testdumps/SampleInput_MisuseOwlDatatypeObjectProperty.ttl";
	// For...qualitymetric.intrinsic.conciseness
	public static String DuplicateInstance = "testdumps/SampleInput_DuplicateInstance.ttl";
	
}
