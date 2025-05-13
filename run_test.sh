#!/bin/bash

# set classpath, include all dependency libraries
CLASSPATH="./bin:./src:./test"
for jar in ./lib/*.jar; do
  CLASSPATH="$CLASSPATH:$jar"
done

# create coverage report directory
mkdir -p coverage-report

# run tests and collect coverage information
java -javaagent:./lib/jacocoagent.jar=destfile=jacoco.exec \
     -cp "$CLASSPATH" \
     org.junit.runner.JUnitCore test.GameController.UnitTest

# generate coverage report
java -jar ./lib/jacococli.jar report jacoco.exec \
     --classfiles ./bin \
     --sourcefiles ./src \
     --html coverage-report

echo "Test execution completed. Coverage report generated in coverage-report directory."
