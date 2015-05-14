#!/bin/bash
var=$(pwd)
fileName="$var/efo-2.41.rdf"
curl --data "Dataset=$fileName&QualityReportRequired=false&MetricsConfiguration={\"@id\":\"_:f4216607408b1\",\"@type\":[\"http://github.com/EIS-Bonn/Luzzu#MetricConfiguration\"],\"http://github.com/EIS-Bonn/Luzzu#metric\":[{\"@value\":\"eu.diachron.qualitymetrics.accessibility.availability.SPARQLAccessibility\"}]}" http://localhost:8080/Luzzu/compute_quality ;
