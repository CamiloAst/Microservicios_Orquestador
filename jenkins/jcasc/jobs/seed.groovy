pipelineJob('build-and-scan') {
    definition {
        cps {
            script("""
        pipeline {
          agent any
          stages {
            stage('Checkout'){ steps { checkout scm } }
            stage('Build'){ steps { sh 'mvn -B -DskipTests package' } }
            stage('SonarQube Scan'){
              environment { SONAR_HOST_URL = 'http://sonarqube:9000' }
              steps { sh 'mvn sonar:sonar -Dsonar.login=admin -Dsonar.password=admin' }
            }
          }
        }
      """.stripIndent())
        }
    }
}
