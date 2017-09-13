pipeline {
    agent {
        docker {
            image 'maven:3.3.9'
            label 'docker'
            args '-v ${HOME}/.m2:${WORKSPACE}/.m2 -v ${HOME}/.docker:${WORKSPACE}/.docker -v /usr/bin/docker:/usr/bin/docker -v /var/run/docker.sock:/var/run/docker.sock'
        }
    }

    environment {
        DOCKER_REPOSITORY = 'registry.inferno-project.ru/v0rt3x'
        DOCKER_CONFIG = "${env.WORKSPACE}/.docker"
        MAVEN_CONFIG = "${env.WORKSPACE}/.m2"
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '15', daysToKeepStr: '30'))
    }

    stages {
        stage ('Build :: Set Version') {
            steps {
                script {
                    env.POM_VERSION = readMavenPom(file: "${WORKSPACE}/pom.xml").version
                    env.RELEASE_VERSION = env.POM_VERSION.replace("-SNAPSHOT", "")
                    env.BUILD_VERSION = "${env.RELEASE_VERSION}-${env.BUILD_TIMESTAMP}-${env.BUILD_NUMBER}"

                    currentBuild.displayName = "${env.BRANCH_NAME} - ${env.BUILD_VERSION}"
                }

                sh 'mvn versions:set -gs ${WORKSPACE}/.m2/settings.xml -DnewVersion=${BUILD_VERSION}'
            }
        }


        stage ('Build :: Build Artifacts') {
            steps {
                sh 'mvn clean install -gs ${WORKSPACE}/.m2/settings.xml -C -B -DskipTests -Ddocker.repository=${DOCKER_REPOSITORY}'
            }
        }

        stage ('Promote') {
            when {
                branch 'master'
            }

            steps {
                sh 'mvn deploy -gs ${WORKSPACE}/.m2/settings.xml -C -B -DskipTests -Ddocker.skip.build=true -Ddocker.repository=${DOCKER_REPOSITORY}'
            }
        }
    }

    post {
        always {
            sh 'docker rmi $(docker images | grep ${BUILD_VERSION} | awk \'{ print $3 }\')'

            deleteDir()
        }
    }
}