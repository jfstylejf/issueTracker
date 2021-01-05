#!/bin/bash

usage="ERROR\nUsage: issueTracker_scan <commit_id> \nERROR"

# if no args specified, show usage
if [ $# -le 0 ]; then
  echo -e ${usage}
  exit 1
fi

baseDir="$ISSUETRACKER_HOME/bin"
source ${baseDir}/.env

curl -H "Content-Type:application/json" -H "token:$TOKEN" -X POST -d '{"projectId": "'${PROJECT_ID}'", "commitId":"'${1}'" ,"category": "'${TOOL}'"}' http://${IP}:${PORT}/scan -s | grep -Po 'msg[":]+\K[^"]+'