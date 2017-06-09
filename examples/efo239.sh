#!/bin/bash
var=$(pwd)
fileName="$var/efo-2.39.rdf"
curl --data "IsSparql=false&BaseUri=http://www.ebi.ac.uk/efo/&Dataset=$fileName&QualityReportRequired=false&MetricsConfiguration={\"@id\":\"_:f4216607408b1\",\"@type\":[\"http://purl.org/eis/vocab/lmi#MetricConfiguration\"],\"http://purl.org/eis/vocab/lmi#metric\":[{\"@value\":\"de.unibonn.iai.eis.diachron.ebi.DefinedOntologyAuthor\"},{\"@value\":\"de.unibonn.iai.eis.diachron.ebi.OBOFoundry\"},{\"@value\":\"de.unibonn.iai.eis.diachron.ebi.ObsoleteConceptsInOntology\"},{\"@value\":\"de.unibonn.iai.eis.diachron.ebi.POBODefinitionUsage\"},{\"@value\":\"de.unibonn.iai.eis.diachron.ebi.SynonymUsage\"},{\"@value\":\"de.unibonn.iai.eis.diachron.ebi.OntologyVersioningConciseness\"}]}" http://localhost:8080/Luzzu/compute_quality ;
