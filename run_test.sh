#!/bin/bash

# 设置classpath，包含所有依赖库
CLASSPATH="./bin:./src:./test"
for jar in ./lib/*.jar; do
  CLASSPATH="$CLASSPATH:$jar"
done

# 创建覆盖率报告目录
mkdir -p coverage-report

# 使用JaCoCo运行测试并收集覆盖率信息
java -javaagent:./lib/jacocoagent.jar=destfile=jacoco.exec \
     -cp "$CLASSPATH" \
     org.junit.runner.JUnitCore test.GameController.UnitTest

# 生成覆盖率报告
java -jar ./lib/jacococli.jar report jacoco.exec \
     --classfiles ./bin \
     --sourcefiles ./src \
     --html coverage-report

echo "Test execution completed. Coverage report generated in coverage-report directory."
