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
mkdir -p coverage-report/models

echo "Running models package tests..."

# 使用JaCoCo运行models包中的所有测试并收集覆盖率信息
java -javaagent:./lib/jacocoagent.jar=destfile=jacoco_models.exec \
     -cp "$CLASSPATH" \
     org.junit.runner.JUnitCore test.models.ClientTest test.models.MovieTest test.models.TupleTest

# 检查测试是否成功
if [ $? -eq 0 ]; then
    echo "All models tests passed successfully!"
else
    echo "Some models tests failed. Check the output above for details."
    exit 1
fi

# 生成覆盖率报告
java -jar ./lib/jacococli.jar report jacoco_models.exec \
     --classfiles ./bin \
     --sourcefiles ./src \
     --html coverage-report/models

echo "Test execution completed. Coverage report generated in coverage-report/models directory."
