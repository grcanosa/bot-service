pipeline {
  agent {
    docker {
      image 'maven'
    }

  }
  stages {
    stage('build') {
      parallel {
        stage('build') {
          steps {
            sh 'maven --version'
          }
        }

        stage('test') {
          steps {
            echo 'Hello'
          }
        }

      }
    }

  }
}