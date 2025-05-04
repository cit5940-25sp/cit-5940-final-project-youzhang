#!/bin/bash

# Create bin directory if it doesn't exist
mkdir -p bin

# Function to compile Java files in a directory if it exists
compile_directory() {
    local dir=$1
    if [ -d "$dir" ] && [ "$(ls -A $dir/*.java 2>/dev/null)" ]; then
        echo "Compiling Java files in $dir..."
        javac -d bin -cp bin:src $dir/*.java
        return $?
    fi
    return 0
}

# Clean bin directory
echo "Cleaning bin directory..."
rm -rf bin/*

# Compile Java files in order
echo "Compiling Java files..."

# First compile the model classes
echo "Compiling model classes..."
javac -d bin -cp src src/models/*.java

# Then compile utility classes
echo "Compiling utility classes..."
javac -d bin -cp bin:src src/utils/*.java

# Then compile factories
echo "Compiling factories..."
compile_directory "src/factories"

# Then compile services
echo "Compiling services..."
compile_directory "src/services"

# Then compile controllers
echo "Compiling controllers..."
compile_directory "src/controllers"

# Finally compile the main server class
echo "Compiling main server class..."
javac -d bin -cp bin:src src/GameServer.java

# Check if compilation was successful
if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    
    # Run the server
    echo "Starting server on port 8080..."
    java -cp bin:src GameServer 8080
else
    echo "Compilation failed!"
    exit 1
fi 