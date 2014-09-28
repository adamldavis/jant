#!/bin/bash

JAVA_HOME=/usr/lib/jvm/jdk1.8.0
JAVA=$JAVA_HOME/bin/java
JAVAC=$JAVA_HOME/bin/javac

CLP=build/classes
mkdir -p $CLP

$JAVAC -d $CLP Jant.java && $JAVAC -d $CLP Build.java  

$JAVA -cp $CLP Build $1



