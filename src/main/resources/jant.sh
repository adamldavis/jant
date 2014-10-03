#!/bin/bash
# usage: ./jant.sh <task> 

CLP=jant.jar

java -cp $CLP Build.java
java -cp $CLP:. jant.Jant $1
