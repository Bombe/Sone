pipeline {
    agent any

    options {
        timestamps()
    }

    stages {
        stage('Build') {
            tools {
                jdk 'OpenJDK 8'
            }
            steps {
                sh './gradlew clean classes testClasses'
            }
        }
        stage('Test') {
            tools {
                jdk 'OpenJDK 8'
            }
            steps {
                sh './gradlew test'
            }
            post {
                always {
                    junit 'build/test-results/*/*.xml'
                }
            }
        }
        stage('Binary') {
            tools {
                jdk 'OpenJDK 8'
            }
            steps {
                sh './gradlew fatJar'
                archiveArtifacts artifacts: 'build/libs/sone*-jar-with-dependencies.jar', fingerprint: true
            }
        }
        stage('Reports') {
            tools {
                jdk 'OpenJDK 8'
            }
            steps {
                sh './gradlew jacocoTestReport'
                jacoco classPattern: 'build/classes/*/main', sourcePattern: '**/src/main/'
            }
        }
    }
}
