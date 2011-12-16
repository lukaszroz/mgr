#!/bin/bash --posix

JAVA=`cygpath 'C:\Program Files\Java\jdk1.7.0_01\bin\java'`

"$JAVA" \
	-server \
	-Dfile.encoding=UTF-8 \
	-jar target/simulation.jar "$@" SS