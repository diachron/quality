#!/bin/bash
if [ "$1" == dependencies ] ; then
	echo "downloading dependencies";
	wget https://github.com/EIS-Bonn/Luzzu/archive/1.2.tar.gz -O ../Luzzu.tar.gz
	tar -xvf ../Luzzu.tar.gz
	mv Luzzu-1.2/ ../Luzzu/
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
	echo "Please first run 'assessment.sh build' then 'assessment.sh run'. If Luzzu is not on your system, please run 'assessment.sh dependencies' prior to the build and run.";
fi
