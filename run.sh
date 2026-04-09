#!/bin/bash

# Compile everything
echo "Compiling..."
javac -cp "lib/*" -d class src/*.java

if [ $? -eq 0 ]; then
    echo "Compilation successful."
    echo "-----------------------------------"
    echo "To run the Master: java -cp \"class:lib/*\" MasterAntennaNode"
    echo "To run an Antenna: java -cp \"class:lib/*\" Antenna <id> <x> <y> <neighborId>"
    echo "To run a User:     java -cp \"class:lib/*\" User <id> <x> <y>"
    echo "-----------------------------------"
else
    echo "Compilation failed."
fi