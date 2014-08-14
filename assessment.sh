#!/bin/bash
if [ "$1" == build ] ; then
	cd Luzzu/;
	mvn clean compile;
elif [ "$1" == run ] ; then
	cd Luzzu/luzzu-communications/;
	mvn exec:java -X;
fi