#!/bin/bash

# compile all Java files
echo "Compiling all Java files..."
./compile.sh

# set classpath, include all dependency libraries
CLASSPATH="./bin:./src:./test"
for jar in ./lib/*.jar; do
  CLASSPATH="$CLASSPATH:$jar"
done

# create coverage report directory
mkdir -p coverage-report/models

echo "Running models package tests..."

# run models package tests and collect coverage information
java -javaagent:./lib/jacocoagent.jar=destfile=jacoco_models.exec \
     -cp "$CLASSPATH" \
     org.junit.runner.JUnitCore test.models.ClientTest test.models.MovieTest test.models.TupleTest

# check if tests passed
if [ $? -eq 0 ]; then
    echo "All models tests passed successfully!"
else
    echo "Some models tests failed. Check the output above for details."
    exit 1
fi

# generate coverage report
java -jar ./lib/jacococli.jar report jacoco_models.exec \
     --classfiles ./bin \
     --sourcefiles ./src \
     --html coverage-report/models

echo "Test execution completed. Coverage report generated in coverage-report/models directory."
