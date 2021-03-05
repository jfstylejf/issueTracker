#!/bin/bash

#sourcePath='/home/fdse/codeWisdom/ser'
#/home/fdse/codeWisdom/service/dependence-analysis/scenario-engine $0
#documentPath='C:\\Users\\86189\\Desktop\\clone\\IssueTracker-Master' #$1;#
echo "start of one sh" >./l.log
detectPath='/home/fdse/codeWisdom/service/dependence-analysis'

neo4jHome=/home/fdse/codeWisdom/service/dependence-analysis/
javaHome=/home/fdse/user/Component/sonar/sonarJava/jdk-11.0.4/bin/
repoPath=/home/fdse/codeWisdom/service/dependence-analysis/scenario-engine
logFile=/home/fdse/codeWisdom/service/dependence-analysis/l.log
echo "var 0 =$0" >>$logFile
echo "var 1 =$1" >>$logFile
echo "repoPath=$repoPath" >>$logFile
#cd detect
pwdres=`pwd`
echo "pwd : $pwdres" >>$logFile

res=`./neo4j-community-4.2.3/bin/neo4j stop`
echo " stop neo4j res:  $res" >> $logFile



cd  deHome0303
#res1=`${javaHome}java -jar multi-dependency-1.5.0.jar -a ./application-kingsley.yml &`
res1=`${javaHome}java -jar multi-dependency-1.5.0.jar -a ./application-kingsley.yml && echo first step ok`
echo "first step ok" >> $logFile
echo "res1= $res1"  >> $ logFile
echo "---------------------------------------------------"

cd ..
res2=`./neo4j-community-4.2.3/bin/neo4j start `
echo "start neo4j ok $res2" >> $logFile
echo $res2
echo "start neo4j ok____________________________________________________________________________________"
sleep 5
cd deHome0303
${javaHome}java -jar -Dspring.config.location=application-kingsley.yml  multi-dependency-1.5.0.jar -m &
#echo  "run succsee" >> $logFile
#cd ..
#./neo4j-community-4.2.3/bin/neo4j stop



# judge if one file can run
#if [ -x ./executable_cpu_linux ]; then
#
#  echo " Yes x mode - /etc/profile" >>$logFile
#
#else
#
#  echo " No x mode - /etc/profile" >>$logFile
#  chmod -R 777 executable_cpu_linux
#fi
#
#eval java -jar ./SACloneDetector_v0916.jar ${repoPath}
#eval python calculate_similarity_file_v1029.py
#cd ..
#if [ ! -d "detectResult" ]; then
#  mkdir detectResult
#else
#  echo "have detectResult dir" >>$logFile
#fi
#
#cd detectResult
##  this
#oneresFolder="oneres"
#rm -rf $oneresFolder
#eval mkdir $oneresFolder
#mv ../detect/result ./${oneresFolder}/result
#mv ../detect/tokenData ./${oneresFolder}/tokenData
#mv ../detect/similarity.csv ./${oneresFolder}
#cd ..
#cd ..
# stop neo4j

# eval java -jar multi-dependency-1.3.7.jar -o application-kingsley.yml &

#eval java -jar multi-dependency-1.3.7.jar -s ./application-kingsley.yml &

#/home/fdse/codeWisdom/service/dependence-analysis
#$0 is depend.sh
#repoPath=$1;

#demoFun(){
#    echo "这是我的第一个 shell 函数!"
#}
#echo "-----函数开始执行-----"
#demoFun

#next writ source-project-conf.json

#
#mv ../detect/tokenData ./
#mv ../similarity.csv ./
#cp -avx ./result ../file
#cp -avx ./tokenData ../file
#cp -avx ./similarity.csv ../file

# copy file to detectResult
#
#eval cd detect
#pwdres=`pwd`vim
#echo "pwd : $pwdres" >> ./l.log

#echo '2'
#
#eval python calculate_similarity_file_v1029.py
#echo '3'
#
#cd ../file
#
#rm -rf ./result
#rm -rf ./tokenData
#rm -rf ./similarity.csv
#
#echo '4'
#
#cd ${sourcePath}
#

#
#cd ..
#
#eval java -jar multi-dependency-1.3.7.jar -s ./application-kingsley.yml &
#
#eval java -jar multi-dependency-1.3.7.jar -o application-kingsley.yml &
#
#echo '5'

#---------------------------

#eval java -jar -Dspring.config.location=application-kingsley.yml  multi-dependency-1.3.7.jar -m &
