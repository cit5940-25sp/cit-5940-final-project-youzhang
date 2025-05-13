#!/bin/bash

# 先使用项目的编译脚本重新编译所有文件
echo "Compiling all Java files..."
./compile.sh

# set classpath, include all dependency libraries
CLASSPATH="./bin:./src:./test"
for jar in ./lib/*.jar; do
  CLASSPATH="$CLASSPATH:$jar"
done  

# create coverage report directory
mkdir -p coverage-report/utils

echo "Running utils package tests..."

# run utils package tests and collect coverage information
java -javaagent:./lib/jacocoagent.jar=destfile=jacoco_utils.exec \
     -cp "$CLASSPATH" \
     org.junit.runner.JUnitCore utils.AutocompleteTest utils.DataLoaderTest utils.MovieCsvParserTest utils.MovieIndexerTest

# check if tests passed
if [ $? -eq 0 ]; then
    echo "All utils tests passed successfully!"
else
    echo "Some utils tests failed. Check the output above for details."
    exit 1
fi

# generate coverage report
java -jar ./lib/jacococli.jar report jacoco_utils.exec \
     --classfiles ./bin \
     --sourcefiles ./src \
     --html coverage-report/utils

echo "Test execution completed. Coverage report generated in coverage-report/utils directory."
