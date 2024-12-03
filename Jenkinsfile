pipeline {
  agent any

  environment {
    DOCKER_IMAGE = "curriculum-app:latest"
    CONTAINER_NAME = "curriculum-app"
    DOCKER_PORT = "8081"
    HOST_PORT = "8081"
    GIT_REPO = "https://github.com/xkydev/pf_curriculum.git"
    GIT_CREDENTIALS_ID = 'github'
  }

  stages {
    stage('Clean Workspace') {
      steps {
        deleteDir()
      }
    }
      
    stage('Checkout') {
      steps {
        checkout([$class: 'GitSCM',
                  branches: [[name: '*/main']],
                  userRemoteConfigs: [[
                      url: env.GIT_REPO,
                      credentialsId: env.GIT_CREDENTIALS_ID
                  ]]
        ])
      }
    }

    stage('Build Docker Image') {
      steps {
        sh 'docker build -t ${DOCKER_IMAGE} .'
      }
    }

    stage('Run Multiple Docker Containers in Parallel') {
          parallel {
              stage('Run Instance 1') {
                  steps {
                      sh '''
                      docker stop ${CONTAINER_NAME}_1 || true
                      docker rm ${CONTAINER_NAME}_1 || true
                      docker run -d --name ${CONTAINER_NAME}_1 -p 8081:8081 -v /var/log/curriculum-app:/logs ${DOCKER_IMAGE}
                      '''
                  }
              }
              stage('Run Instance 2') {
                  steps {
                      sh '''
                      docker stop ${CONTAINER_NAME}_2 || true
                      docker rm ${CONTAINER_NAME}_2 || true
                      docker run -d --name ${CONTAINER_NAME}_2 -p 8082:8081 -v /var/log/curriculum-app:/logs ${DOCKER_IMAGE}
                      '''
                  }
              }
              stage('Run Instance 3') {
                  steps {
                      sh '''
                      docker stop ${CONTAINER_NAME}_3 || true
                      docker rm ${CONTAINER_NAME}_3 || true
                      docker run -d --name ${CONTAINER_NAME}_3 -p 8083:8081 -v /var/log/curriculum-app:/logs ${DOCKER_IMAGE}
                      '''
                  }
              }
          }
      }
  }

  post {
    always {
      echo 'Pipeline Completed!'
    }
  }
}
