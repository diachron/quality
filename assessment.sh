#!/bin/bash
if [ "$1" == dependencies ] ; then
	echo "downloading dependencies";
	git clone https://github.com/EIS-Bonn/Luzzu.git ../Luzzu;
elif [ "$1" == build ] ; then
	echo "Compiling Diachron Quality Metrics";
	mvn clean install -Dmaven.test.skip=true;
	echo "Copying Quality Metrics JAR to Luzzu Quality Framework";
	mkdir ../Luzzu/luzzu-communications/externals/metrics/ebi/;
	cp ebi/target/*-dependencies.jar ../Luzzu/luzzu-communications/externals/metrics/ebi/;
	cp ebi/metrics.trig ../Luzzu/luzzu-communications/externals/metrics/ebi/;
	echo "Copying DQM vocabulary to Luzzu Quality Framework";
	cp quality-vocabulary/src/main/resources/vocabularies/dqm/dqm.ttl ../Luzzu/luzzu-communications/externals/vocabs/;
	echo "Compiling Luzzu Quality Framework";
	cd ../Luzzu/;
	mvn clean install -Dmaven.test.skip=true;
	echo "Creating symbolic links";
	cd ..;
	ln -s ../Luzzu/luzzu-operations/src/main/resources/properties/webservice.properties webservice.properties;
	echo "Build Finished"
elif [ "$1" == run ] ; then
	cd ../Luzzu/luzzu-communications/;
	mvn exec:java -X;
else
	echo "Please first run 'assessment.sh build' then 'assessment.sh run'";
fi
