#!/bin/bash

# 先使用项目的编译脚本重新编译所有文件
echo "Compiling all Java files..."
./compile.sh

# 设置classpath，包含所有依赖库
CLASSPATH="./bin:./src:./test"
for jar in ./lib/*.jar; do
  CLASSPATH="$CLASSPATH:$jar"
done

# 创建覆盖率报告目录
mkdir -p coverage-report/utils

echo "Running utils package tests..."

# 使用JaCoCo运行utils包中的所有测试并收集覆盖率信息
java -javaagent:./lib/jacocoagent.jar=destfile=jacoco_utils.exec \
     -cp "$CLASSPATH" \
     org.junit.runner.JUnitCore utils.AutocompleteTest utils.DataLoaderTest utils.MovieCsvParserTest utils.MovieIndexerTest

# 检查测试是否成功
if [ $? -eq 0 ]; then
    echo "All utils tests passed successfully!"
else
    echo "Some utils tests failed. Check the output above for details."
    exit 1
fi

# 生成覆盖率报告
java -jar ./lib/jacococli.jar report jacoco_utils.exec \
     --classfiles ./bin \
     --sourcefiles ./src \
     --html coverage-report/utils

echo "Test execution completed. Coverage report generated in coverage-report/utils directory."
