#!/bin/bash
if [ "$1" == build ] ; then
	echo "Compiling Diachron Quality Metrics";
	sudo mvn clean install -Dmaven.test.skip=true;
	echo "Copying Quality Metrics JAR to Luzzu Quality Framework";
	sudo mkdir Luzzu/luzzu-communications/externals/diachron/;
	sudo cp target/diachron-0.0.1-SNAPSHOT.jar Luzzu/luzzu-communications/externals/diachron/;
	sudo cp metrics.trig Luzzu/luzzu-communications/externals/diachron/;
	echo "Copying DQM vocabulary to Luzzu Quality Framework";
	sudo cp src/main/resources/vocabularies/dqm/dqm.trig Luzzu/luzzu-communications/externalvocab/;
	echo "Compiling Luzzu Quality Framework";
	cd Luzzu/;
	sudo mvn clean install;
	echo "Build Finished"
elif [ "$1" == run ] ; then
	cd Luzzu/luzzu-communications/;
	sudo mvn exec:java -X;
else
	echo "Please first run 'assessment.sh build' then 'assessment.sh run'";
fi