pipeline {
    agent any

    options {
        timestamps()
    }

    stages {
        stage('Build') {
            steps {
                sh './gradlew clean classes testClasses'
            }
        }
        stage('Test') {
            steps {
                sh './gradlew test'
            }
            post {
                always {
                    junit 'build/test-results/test/*.xml'
                }
            }
        }
        stage('Binary') {
            steps {
                sh './gradlew fatJar'
                archiveArtifacts artifacts: 'build/libs/sone*-jar-with-dependencies.jar', fingerprint: true
            }
        }
        stage('Reports') {
            steps {
                sh './gradlew jacocoTestReport findbugsMain countLines'
                jacoco classPattern: 'build/classes/*/main', sourcePattern: '**/src/main/'
                findbugs canComputeNew: false, defaultEncoding: '', excludePattern: '', healthy: '', includePattern: '', pattern: '**/findbugs/main.xml', unHealthy: ''
                sloccountPublish encoding: '', pattern: 'build/reports/cloc/*.xml'
            }
        }
    }
}
