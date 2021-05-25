#!/bin/bash

repoPath=${1}

mkdir ${repoPath}
cd ${repoPath}

git clone http://fdse.gitlab.com/platform/forTest.git
