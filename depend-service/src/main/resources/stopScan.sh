#! /bin/bash
cd deHome0303
PID=$(cat pid.txt)
kill -9 $PID
neo4jHome=/home/fdse/codeWisdom/service/dependence-analysis/neo4j-community-4.2.3/bin

logFile=/home/fdse/codeWisdom/service/dependence-analysis/l.log

res=`${neo4jHome}/neo4j stop`
echo "$res" >> $logFile
