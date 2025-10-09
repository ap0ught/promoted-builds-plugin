// Example Jenkinsfile showing Declarative Pipeline with Promoted Builds Plugin
// This demonstrates the basic syntax for configuring promotion processes

pipeline {
    agent any
    
    properties([
        [$class: 'JobPropertyImpl', 
            activeProcessNames: ['QA-Approved', 'Production-Ready'] as Set
        ]
    ])
    
    stages {
        stage('Build') {
            steps {
                echo 'Building application...'
                // Add your build steps here
            }
        }
        
        stage('Test') {
            steps {
                echo 'Running tests...'
                // Add your test steps here
            }
        }
    }
    
    post {
        success {
            echo 'Build successful. Ready for promotion via Jenkins UI.'
        }
        failure {
            echo 'Build failed. Not eligible for promotion.'
        }
    }
}
