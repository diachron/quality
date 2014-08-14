#!/bin/bash
if [ "$1" == build ] ; then
	echo "Compiling Diachron Quality Metrics";
	mvn clean install -Dmaven.test.skip=true;
	echo "Copying Quality Metrics JAR to Luzzu Quality Framework";
	mkdir Luzzu/luzzu-communications/externals/diachron/;
	cp target/diachron-0.0.1-SNAPSHOT.jar Luzzu/luzzu-communications/externals/diachron/;
	cp metrics.trig Luzzu/luzzu-communications/externals/diachron/;
	echo "Copying DQM vocabulary to Luzzu Quality Framework";
	cp src/main/resources/vocabularies/dqm/dqm.trig Luzzu/luzzu-communications/externalvocab/;
	echo "Compiling Luzzu Quality Framework";
	cd Luzzu/;
	mvn clean compile;
	echo "Build Finished"
elif [ "$1" == run ] ; then
	cd Luzzu/luzzu-communications/;
	mvn exec:java -X;
else
	echo "Please first run 'assessment.sh build' then 'assessment.sh run'";
fi