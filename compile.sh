#!/bin/bash

# 清除并重新创建bin目录用于存放编译后的class文件
rm -rf bin
mkdir -p bin

# 设置classpath，包含所有依赖库
CLASSPATH="./bin:./src:./test"
for jar in ./lib/*.jar; do
  CLASSPATH="$CLASSPATH:$jar"
done

# 编译src目录下的所有Java文件
javac -source 1.8 -target 1.8 -d bin -cp "$CLASSPATH" $(find src -name "*.java")

# 编译test目录下的所有Java文件
javac -source 1.8 -target 1.8 -d bin -cp "$CLASSPATH" $(find test -name "*.java")

echo "Compilation completed."
