package de.unibonn.iai.eis.diachron.configuration;

/**
 * This class is responsible for mapping the names of output file
 * for Quality problems
 * 
 * @author Muhammad Ali Qasmi
 * @date 19th May 2014
 */
public class OutputFileMappingForQualityProblems {
     /**
      * For Malformed Datatype Literals Quality Problems   
      */
     public static String MalformedDatatypeLiterals = "C:/MalformedDatatypeLiterals.rdf";
     /**
      * For Incompatible Datatype Range Quality Problems
      */
     public static String IncompatibleDatatypeRange = "C:/IncompatibleDatatypeRange.rdf";
     /**
      * For Homogeneous Datatypes Quality Problems
      */
     public static String HomogeneousDatatypes = "C:/HomogeneousDatatypes.rdf";
     /**
      * For Misplaced Classes Or Properties Quality Problems
      */
     public static String MisplacedClassesOrProperties = "C:/MisplacedClassesOrProperties.rdf";
     /**
      * For Misuse Owl Datatype Or Object Properties Quality Problems
      */
     public static String MisuseOwlDatatypeOrObjectProperties = "C:/MisuseOwlDatatypeOrObjectProperties.rdf";
     /**
      * For Undefined Classes Or Properties Quality Problems 
      */
     public static String UndefinedClassesOrProperties = "C:/UndefinedClassesOrProperties.rdf";
}
