#!/bin/bash

usage="\nUsage: issueTracker_commit (all|scanned) size page \n"

# if no args specified, show usage
if [ $# -le 0 ]; then
  echo -e ${usage}
  exit 1
fi

baseDir="$ISSUETRACKER_HOME/bin"
source ${baseDir}/.env

option=$1

case ${option} in

  (all)
	curl -H "token:${TOKEN}" --url  "http://$IP:$PORT/scan/commits?project_id=${PROJECT_ID}&is_whole=true&category=${TOOL}"  -s | jq -r
	exit 0
	;;
  (scanned)
   # need Formatted output display
    curl -H "token:${TOKEN}" --url "http://$IP:$PORT/scan/commits?project_id=${PROJECT_ID}&category=${TOOL}"  -s | jq -r
	exit 0
    ;;
  (*)
   ## /commitDate/{commit_id}   /tillCommitDate/{repo_id}
	echo -e "No option $1"
    echo -e ${usage}
    exit 1
    ;;

esac