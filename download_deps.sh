#!/bin/bash

# Create lib directory if it doesn't exist
mkdir -p lib

# Download JSON library (json-simple)
curl -L https://repo1.maven.org/maven2/com/googlecode/json-simple/json-simple/1.1.1/json-simple-1.1.1.jar -o lib/json-simple-1.1.1.jar

# Download Gson library
curl -L https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar -o lib/gson-2.10.1.jar

echo "Dependencies downloaded to lib directory."
