#!/bin/bash
cd "$(dirname "$0")"
java -cp "bin:lib/*" GameServer
