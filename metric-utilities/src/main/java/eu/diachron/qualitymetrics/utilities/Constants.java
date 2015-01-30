package eu.diachron.qualitymetrics.utilities;

public class Constants {

  public static final String ANNOTATION_PROPERTIES_FILE = "src/main/resources/"
    + "AnnotationPropertiesList.txt";
  public static final String LABEL_PROPERTIES_FILE = "src/main/resources/LabelPropertiesList.txt";
  public final static String UNDEFINED_CLASS_PROPERTIES_FILE = "src/main/resources/UndefinedClassPropertiesList";
  public final static String UNDEFINED_PROPERTIES_FILE = "src/main/resources/UndefinedPropertiesList";

  public static String LOADED_MODELS = "src/main/resources/models";

  public static final String CAMEL_CASE_REGEX = "[A-Z]([A-Z0-9]*[a-z][a-z0-9]*[A-Z]|[a-z0-9]*[A-Z]"
    + "[A-Z0-9]*[a-z])[A-Za-z0-9]*";
}
