// Example Jenkinsfile showing how to use promoted build parameters
// This demonstrates selecting a specific promoted build from an upstream job

pipeline {
    agent any
    
    parameters {
        promotedBuild(
            name: 'PROMOTED_BUILD',
            project: 'upstream-job',
            promotion: 'Production-Ready',
            description: 'Select a production-ready build to deploy'
        )
    }
    
    stages {
        stage('Deploy') {
            steps {
                echo "Deploying build: ${params.PROMOTED_BUILD}"
                echo "Build URL: ${params.PROMOTED_BUILD_URL}"
                echo "Build number: ${params.PROMOTED_BUILD_NUMBER}"
                
                // Example: Copy artifacts from the promoted build
                // copyArtifacts(
                //     projectName: 'upstream-job',
                //     selector: specific("${params.PROMOTED_BUILD_NUMBER}")
                // )
                
                // Add your deployment steps here
            }
        }
    }
    
    post {
        success {
            echo 'Deployment successful!'
        }
    }
}
