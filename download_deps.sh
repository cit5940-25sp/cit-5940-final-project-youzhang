#!/bin/bash

# Create lib directory if it doesn't exist
mkdir -p lib

echo "Downloading dependencies to lib directory..."

# Download JSON libraries
echo "Downloading JSON libraries..."
curl -L https://repo1.maven.org/maven2/com/googlecode/json-simple/json-simple/1.1.1/json-simple-1.1.1.jar -o lib/json-simple-1.1.1.jar
curl -L https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar -o lib/gson-2.10.1.jar

# Download JUnit and Hamcrest
echo "Downloading JUnit and Hamcrest..."
curl -L https://repo1.maven.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar -o lib/junit-4.13.2.jar
curl -L https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar -o lib/hamcrest-core-1.3.jar

# Download Mockito and dependencies
echo "Downloading Mockito and dependencies..."
curl -L https://repo1.maven.org/maven2/org/mockito/mockito-core/3.12.4/mockito-core-3.12.4.jar -o lib/mockito-core-3.12.4.jar
curl -L https://repo1.maven.org/maven2/net/bytebuddy/byte-buddy/1.11.13/byte-buddy-1.11.13.jar -o lib/byte-buddy-1.11.13.jar
curl -L https://repo1.maven.org/maven2/net/bytebuddy/byte-buddy-agent/1.11.13/byte-buddy-agent-1.11.13.jar -o lib/byte-buddy-agent-1.11.13.jar
curl -L https://repo1.maven.org/maven2/org/objenesis/objenesis/3.2/objenesis-3.2.jar -o lib/objenesis-3.2.jar

# Download JaCoCo
echo "Downloading JaCoCo..."
curl -L https://repo1.maven.org/maven2/org/jacoco/org.jacoco.agent/0.8.7/org.jacoco.agent-0.8.7-runtime.jar -o lib/jacocoagent.jar
curl -L https://repo1.maven.org/maven2/org/jacoco/org.jacoco.cli/0.8.7/org.jacoco.cli-0.8.7-nodeps.jar -o lib/jacococli.jar

echo "All dependencies downloaded successfully to lib directory."
echo "You can now run './compile.sh' to compile the project."
