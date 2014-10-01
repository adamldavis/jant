#!/bin/bash

JAVA_HOME=/usr/lib/jvm/jdk1.8.0
JAVA=$JAVA_HOME/bin/java
JAVAC=$JAVA_HOME/bin/javac

CLP=build/classes
mkdir -p $CLP

$JAVAC -d $CLP Jant.java Build.java JavaBuilder.java  

$JAVA -cp $CLP Jant $1



