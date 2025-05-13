#!/bin/bash

# 清除并重新创建bin目录用于存放编译后的class文件
echo "Compiling all Java files..."
./compile.sh

# 设置classpath，包含所有依赖库
CLASSPATH="./bin:./src:./test"
for jar in ./lib/*.jar; do
  CLASSPATH="$CLASSPATH:$jar"
done

# 清除旧的覆盖率报告
rm -rf coverage-report
mkdir -p coverage-report

echo "Running all tests and generating unified coverage report..."

# 使用JaCoCo运行所有测试并收集覆盖率信息
java -javaagent:./lib/jacocoagent.jar=destfile=jacoco.exec \
     -cp "$CLASSPATH" \
     org.junit.runner.JUnitCore test.models.ClientTest test.models.MovieTest test.models.TupleTest \
     utils.AutocompleteTest utils.DataLoaderTest utils.MovieCsvParserTest utils.MovieIndexerTest \
     test.GameController.UnitTest

# 检查测试是否成功
if [ $? -eq 0 ]; then
    echo "All tests passed successfully!"
else
    echo "Some tests failed. Check the output above for details."
    exit 1
fi

# 生成统一的覆盖率报告
java -jar ./lib/jacococli.jar report jacoco.exec \
     --classfiles ./bin \
     --sourcefiles ./src \
     --html coverage-report

echo "Test execution completed. Unified coverage report generated in coverage-report directory."
echo "Open coverage-report/index.html to view the report."
