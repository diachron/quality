Diachron Quality Assessment Framework
=======

Instructions on how to Install and Execute
-----

The package is made up of two main modules: The quality assessment framework and the diachron metrics.

The latest development package is found in the luzzu-integration branch. To download from git, type the following in a terminal window: ```git clone https://github.com/diachron/quality.git -b luzzu-integration```.

Please make sure that before you start running the framework, you always do a "git pull" to fetch the latest updates.

In order to facilitate the building and execution of the framework, a script "assessment" is also available in the branch package. To build the package the script is run with the build parameter.. ```./assessment build```. This will build the diachron metrics, quality framework and set up the required resources.

To run the assessment framework, the run parameter is required - ```./assessment run```. This will start a local webservice on port 8080.

Assessing Datasets
-----

In this branch we provide an example that can be executed to try out the system. This is found under the folder "examples". In this folder you will find an RDF dataset and a script file. The script contains the relevant cURL command. To run the example, the following is required:

```
$ cd examples
examples$ ./efo234.sh
```

The quality metadata will be stored in "/Luzzu/luzzu-communications/qualityMetadata".

If you want to run your own examples, a cURL command can be done as following:

```curl --data "Dataset=$fileName&QualityReportRequired=$boolean$&MetricsConfiguration=$config" http://localhost:8080/luzzu/compute_quality```

$fileName - can be an http location or a local file
$boolean - true or false
$config - A config file indicating the quality metrics required to run. Ex: {\"@id\":\"_:f4216607408b1\",\"@type\":[\"http://github.com/EIS-Bonn/Luzzu#MetricConfiguration\"],\"http://github.com/EIS-Bonn/Luzzu#metric\":[{\"@value\":\"eu.diachron.qualitymetrics.intrinsic.accuracy.POBODefinitionUsage\"},{\"@value\":\"eu.diachron.qualitymetrics.intrinsic.consistency.HomogeneousDatatypes\"},{\"@value\":\"eu.diachron.qualitymetrics.intrinsic.consistency.ObsoleteConceptsInOntology\"},{\"@value\":\"eu.diachron.qualitymetrics.representational.understandability.LowBlankNodeUsage\"},{\"@value\":\"eu.diachron.qualitymetrics.representational.understandability.HumanReadableLabelling\"},{\"@value\":\"eu.diachron.qualitymetrics.intrinsic.accuracy.SynonymUsage\"},{\"@value\":\"eu.diachron.qualitymetrics.intrinsic.accuracy.DefinedOntologyAuthor\"},{\"@value\":\"eu.diachron.qualitymetrics.intrinsic.conciseness.OntologyVersioningConciseness\"},{\"@value\":\"eu.diachron.qualitymetrics.accessibility.performance.DataSourceScalability\"},{\"@value\":\"eu.diachron.qualitymetrics.dynamicity.timeliness.TimelinessOfResource\"},{\"@value\":\"eu.diachron.qualitymetrics.intrinsic.consistency.EntitiesAsMembersOfDisjointClasses\"}]}
