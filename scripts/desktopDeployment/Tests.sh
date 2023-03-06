#!/bin/bash
jre/bin/java -XX:MaxRAMPercentage=60 -classpath "lib/*" tests.Tests
exit 0
