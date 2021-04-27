#!/bin/bash

echo "start  >>>>>>>>>>>>>>>" >./l.log
detectPath='/home/fdse/codeWisdom/service/dependence-analysis'
neo4jHome=/home/fdse/codeWisdom/service/dependence-analysis/neo4j-community-4.2.3/bin
javaHome=/home/fdse/user/Component/sonar/sonarJava/jdk-11.0.4/bin/
logFile=/home/fdse/codeWisdom/service/dependence-analysis/l.log

res=`${neo4jHome}/neo4j stop`
echo "$res" >> $logFile

cd  deHome0303
res1=`${javaHome}java -jar multi-dependency-1.5.0.jar -a ./application-kingsley.yml && echo first step ok`
echo "first step ok" >> $logFile

res2=`${neo4jHome}/neo4j start `
echo "$res2 " >> $logFile
echo "start neo4j ok "
sleep 25
# delete &
 ${javaHome}java -jar -Dspring.config.location=application-kingsley.yml  multi-dependency-1.5.0.jar -m &
echo $! > pid.txt
echo "step2 ok" >>  $logFile
