#!groovy
pipeline {
    agent any

    environment {
        mvnHome = "tool 'M3'"
    }

    triggers {
        pollSCM ignorePostCommitHooks: true, scmpoll_spec: '''TZ=Asia/Shanghai
         H 3 * * *'''
    }

    stages {
        stage('Preparation') {
            steps {
                echo 'Preparation branch: developer'
                git branch: 'developer', credentialsId: 'ae2cb38a-d554-469f-9051-6624dfb1ce47', url: 'https://github.com/FudanSELab/IssueTracker-Master.git'
            }
        }
        stage('Build') {
            steps {
                echo 'Build'
                // Run the maven build
                sh "'${mvnHome}/bin/mvn' -Dmaven.test.failure.ignore compile "
        }
        stage('Tests') {
            steps {
                echo 'Tests'
                sh "'${mvnHome}/bin/mvn' test"
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying....'
            }
        }
    }
}