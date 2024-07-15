pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Unit tests') {
            steps {
                script {
                    sh "${GRADLE_HOME}/bin/gradle test"
                }
            }
        }
        stage('Build') {
            steps {
                script {
                    sh "${GRADLE_HOME}/bin/gradle clean build -x test"
                }
            }
        }
        stage('Build Docker Images') {
            steps {
                script {
                    def services = findFiles(glob: '**/Dockerfile')
                    services.each { service ->
                        def serviceName = service.path.tokenize('/')[0]
                        sh "docker build -t ${serviceName}:${BUILD_NUMBER} ${service.path}"
                    }
                }
            }
        }
        stage('Test') {
            steps {
                echo 'Testing...'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying...'
            }
        }
    }
}