#!/bin/sh

export MAVEN_OPTS="$MAVEN_OPTS -noverify -Xms512m -Xmx2048m -XX:MaxPermSize=512m"
mvn clean package
cp chaos-monkey.properties target
cd target
java -jar activiti-chaos-monkey-0.0.1-SNAPSHOT.jar
echo "Running generated test project"
cd output
mvn clean test