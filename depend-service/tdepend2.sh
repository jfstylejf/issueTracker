#! /bin/bash
cd deHome0303
PID=$(cat pid.txt)
kill -9 $PID