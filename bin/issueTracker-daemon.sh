#!/bin/bash

usage="\nUsage: issueTracker-daemon.sh （stop|start|restart) (default|develop|external) \n"

basePath=$(pwd)
option=$1


# if no args specified, show usage
if [ $# -le 0 ]; then
    echo -e ${usage}
    exit 1
fi


stop(){
   # paging or not ；size is the number of listing in the page
    if [ "$(cat .pid)" == "NULL" ] ; then
		echo "no progress need stop"
	exit 1
    fi

    for pid in $(cat .pid)
    do
		kill -9 ${pid}
    done
    echo "NULL" > .pid
}

start(){
    for serviceName in account project issue cn.edu.fudan.common.scan tag event
    do
        # if no args specified, show usage
        if [ $# -le 0 ]; then
            echo -e ${usage}
            exit 1
        fi

        if [ "$1" == "default" ]
        then
           nohup java -jar    ${serviceName}-service-0.0.1-SNAPSHOT.jar   > ${serviceName}.log 2>&1 &
        elif [ "$1" == "develop" ]
        then
            config="--spring.profiles.active=develop"
#            echo " nohup java -jar    ${serviceName}-service-0.0.1-SNAPSHOT.jar  ${config}  > ${serviceName}-service.log 2>&1 &"
            nohup java -jar    ${serviceName}-service-0.0.1-SNAPSHOT.jar  ${config}  > ${serviceName}-service.log 2>&1 &
        elif [ "$1" == "external" ]
        then
            config="-Dspring.config.location=${basePath}/config/application-${serviceName}.properties"
#            echo "nohup java -jar   ${config} ${serviceName}-service-0.0.1-SNAPSHOT.jar   > ${serviceName}-service.log 2>&1 &"
            nohup java -jar   ${config} ${serviceName}-service-0.0.1-SNAPSHOT.jar   > ${serviceName}-service.log 2>&1 &
        else
            echo -e "No option $1"
            echo -e ${usage}
            exit 1
        fi
    done
     ps -ef | grep /home/fdse/user/issueTracker/config/application- | awk '{if($3==1)printf $2 "\n" }' > .pid
}

case ${option} in

  (stop)
	stop
    ;;
  (start)
    start ${2}
    ;;
  (restart)
	stop
    start ${2}
   ;;
  (*)
    echo -e "No option $1"
    echo -e ${usage}
    exit 1
    ;;

esac
