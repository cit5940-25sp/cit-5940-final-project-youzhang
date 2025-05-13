#!/bin/bash

# compile all Java files
echo "Compiling all Java files..."
./compile.sh

# set classpath, include all dependency libraries
CLASSPATH="./bin:./src:./test"
for jar in ./lib/*.jar; do
  CLASSPATH="$CLASSPATH:$jar"
done

# remove old coverage report
rm -rf coverage-report
mkdir -p coverage-report

echo "Running all tests and generating unified coverage report..."

# run all tests and collect coverage information
java -javaagent:./lib/jacocoagent.jar=destfile=jacoco.exec \
     -cp "$CLASSPATH" \
     org.junit.runner.JUnitCore test.models.ClientTest test.models.MovieTest test.models.TupleTest \
     utils.AutocompleteTest utils.DataLoaderTest utils.MovieCsvParserTest utils.MovieIndexerTest \
     test.GameController.UnitTest

# check if tests passed
if [ $? -eq 0 ]; then
    echo "All tests passed successfully!"
else
    echo "Some tests failed. Check the output above for details."
    exit 1
fi

# generate unified coverage report
java -jar ./lib/jacococli.jar report jacoco.exec \
     --classfiles ./bin \
     --sourcefiles ./src \
     --html coverage-report

echo "Test execution completed. Unified coverage report generated in coverage-report directory."
echo "Open coverage-report/index.html to view the report."
