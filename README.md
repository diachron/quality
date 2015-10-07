Diachron Quality Assessment Framework
=======

Instructions on how to Install and Execute
-----

The package is made up of two main modules: The quality assessment framework and the diachron metrics.

The latest development package is found in the master branch. To download from git, type the following in a terminal window: ```git clone https://github.com/diachron/quality.git```.

Please make sure that before you start running the framework, you always do a "git pull" to fetch the latest updates.

In order to facilitate the building and execution of the framework, a script "assessment" is also available in the branch package. First the Luzzu framework has to be downloaded. This can be done by running the script with the dependencies parameter ```./assessment dependencies```. 

After downloading dependencies, the whole package has to be built. This will take some time in its first run, as dependencies have to be downloaded. To build the package the script is run with the build parameter - ```./assessment build```. This will build the diachron metrics, quality framework and set up the required resources. Please make sure that Maven and Java 1.7 are installed on your system.

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

```curl --data "Dataset=$fileName&QualityReportRequired=$boolean$&MetricsConfiguration=$config&BaseURI=$baseURI&IsSparql=$boolean" http://localhost:8080/luzzu/compute_quality```

* `$fileName` - the dataset location, an RDF serialised data dump or SPARQL endpoint (IsSparql has to be set as true if this variable is a SPARQL endpoint);
* `$boolean` - true or false;
* `$baseURI` - the PLD of the dataset/SPARQL endpoint being assessed;
* `$config` - A config file indicating the quality metrics required to run. Ex: `{\"@id\":\"_:f4216607408b1\",\"@type\":[\"http://purl.org/eis/vocab/lmi#MetricConfiguration\"],\"http://purl.org/eis/vocab/lmi #metric\":[{\"@value\":\"eu.diachron.qualitymetrics.intrinsic.accuracy.POBODefinitionUsage\"},{\"@value\":\"eu.diachron.qualitymetrics.intrinsic.consistency.HomogeneousDatatypes\"},{\"@value\":\"eu.diachron.qualitymetrics.intrinsic.consistency.ObsoleteConceptsInOntology\"},{\"@value\":\"eu.diachron.qualitymetrics.representational.understandability.LowBlankNodeUsage\"},{\"@value\":\"eu.diachron.qualitymetrics.representational.understandability.HumanReadableLabelling\"},{\"@value\":\"eu.diachron.qualitymetrics.intrinsic.accuracy.SynonymUsage\"},{\"@value\":\"eu.diachron.qualitymetrics.intrinsic.accuracy.DefinedOntologyAuthor\"},{\"@value\":\"eu.diachron.qualitymetrics.intrinsic.conciseness.OntologyVersioningConciseness\"},{\"@value\":\"eu.diachron.qualitymetrics.accessibility.performance.DataSourceScalability\"},{\"@value\":\"eu.diachron.qualitymetrics.dynamicity.timeliness.TimelinessOfResource\"},{\"@value\":\"eu.diachron.qualitymetrics.intrinsic.consistency.EntitiesAsMembersOfDisjointClasses\"}]}`
