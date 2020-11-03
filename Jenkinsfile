#!/usr/bin/env groovy
import hudson.tasks.test.AbstractTestResultAction

properties([
  [
    $class: 'ThrottleJobProperty',
    categories: ['pipeline'],
    throttleEnabled: true,
    throttleOption: 'category'
  ]
])
pipeline {
    agent none
    options {
        buildDiscarder(logRotator(
            numToKeepStr: env.BRANCH_NAME.equals("master") ? '15' : '3',
            daysToKeepStr: env.BRANCH_NAME.equals("master") || env.BRANCH_NAME.startsWith("rel-") ? '' : '7',
            artifactDaysToKeepStr: env.BRANCH_NAME.equals("master") || env.BRANCH_NAME.startsWith("rel-") ? '' : '3',
            artifactNumToKeepStr: env.BRANCH_NAME.equals("master") || env.BRANCH_NAME.startsWith("rel-") ? '' : '1'
        ))
        disableConcurrentBuilds()
        skipStagesAfterUnstable()
    }
    environment {
        COMPOSE_PROJECT_NAME = "stockmanagement${BRANCH_NAME}"
    }
    parameters {
        string(name: 'contractTestsBranch', defaultValue: 'master', description: 'The branch of contract tests to checkout')
    }
    stages {
        stage('Preparation') {
            agent any
            steps {
                withCredentials([usernamePassword(
                  credentialsId: "cad2f741-7b1e-4ddd-b5ca-2959d40f62c2",
                  usernameVariable: "USER",
                  passwordVariable: "PASS"
                )]) {
                    sh 'set +x'
                    sh 'docker login -u $USER -p $PASS'
                }
                script {
                    CURRENT_BRANCH = env.GIT_BRANCH // needed for agent-less stages
                    def properties = readProperties file: 'gradle.properties'
                    if (!properties.serviceVersion) {
                        error("serviceVersion property not found")
                    }
                    VERSION = properties.serviceVersion
                    STAGING_VERSION = properties.serviceVersion
                    if (CURRENT_BRANCH != 'master' || (CURRENT_BRANCH == 'master' && !VERSION.endsWith("SNAPSHOT"))) {
                        STAGING_VERSION += "-STAGING"
                    }
                    currentBuild.displayName += " - " + VERSION
                }
            }
            post {
                failure {
                    script {
                        notifyAfterFailure()
                    }
                }
            }
        }
        stage('Build') {
            agent any
            environment {
                PATH = "/usr/local/bin/:$PATH"
                STAGING_VERSION = "${STAGING_VERSION}"
            }
            steps {
                withCredentials([file(credentialsId: '8da5ba56-8ebb-4a6a-bdb5-43c9d0efb120', variable: 'ENV_FILE'),
                                 file(credentialsId: 'b35ad1bd-ccca-437f-a2cd-3578c10da7bf', variable: 'SECRING_FILE'),
                                 usernamePassword(
                                         credentialsId: "70dd29d6-7990-4598-a2f8-aa3e1f038ac1",
                                         usernameVariable: "SIGNING_KEYID",
                                         passwordVariable: "SIGNING_PASSWORD"),
                                 usernamePassword(
                                         credentialsId: "79aa4a36-2c52-486f-bbca-1ed06b314a96",
                                         usernameVariable: "OSSRH_USERNAME",
                                         passwordVariable: "OSSRH_PASSWORD"
                                 )]) {
                    script {
                        try {
                            sh(script: "./ci-buildImage.sh")
                            currentBuild.result = processTestResults('SUCCESS')
                        }
                        catch (exc) {
                            currentBuild.result = processTestResults('FAILURE')
                            if (currentBuild.result == 'FAILURE') {
                                error(exc.toString())
                            }
                        }
                    }
                }
            }
            post {
                success {
                    archive 'build/libs/*.jar,build/resources/main/api-definition.html, build/resources/main/  version.properties'
                }
                unstable {
                    script {
                        notifyAfterFailure()
                    }
                }
                failure {
                    script {
                        notifyAfterFailure()
                    }
                }
                cleanup {
                    script {
                        sh "sudo rm -rf ${WORKSPACE}/{*,.*} || true"
                    }
                }
            }
        }
        stage('Build demo-data') {
            when {
                expression {
                    return CURRENT_BRANCH == 'master'
                }
            }
            steps {
                build job: "OpenLMIS-3.x-build-demo-data-pipeline"
            }
            post {
                failure {
                    script {
                        notifyAfterFailure()
                    }
                }
            }
        }
        stage('Deploy to test') {
            when {
                expression {
                    return CURRENT_BRANCH == 'master' && VERSION.endsWith("SNAPSHOT")
                }
            }
            steps {
                build job: 'OpenLMIS-stockmanagement-deploy-to-test', wait: false
            }
            post {
                failure {
                    script {
                        notifyAfterFailure()
                    }
                }
            }
        }
        /*stage('Parallel: Sonar analysis and contract tests') {
            parallel {
                stage('Sonar analysis') {
                    agent any
                    environment {
                        PATH = "/usr/local/bin/:$PATH"
                    }
                    steps {
                        withSonarQubeEnv('Sonar OpenLMIS') {
                            withCredentials([string(credentialsId: 'SONAR_LOGIN', variable: 'SONAR_LOGIN'), string(credentialsId: 'SONAR_PASSWORD', variable: 'SONAR_PASSWORD')]) {
                                script {
                                    sh(script: "./ci-sonarAnalysis.sh")

                                    // workaround: Sonar plugin retrieves the path directly from the output
                                    sh 'echo "Working dir: ${WORKSPACE}/build/sonar"'
                                }
                            }
                        }
                        timeout(time: 1, unit: 'HOURS') {
                            script {
                                def gate = waitForQualityGate()
                                if (gate.status != 'OK') {
                                    echo 'Quality Gate FAILED'
                                    currentBuild.result = 'UNSTABLE'
                                }
                            }
                        }
                    }
                    post {
                        unstable {
                            script {
                                notifyAfterFailure()
                            }
                        }
                        failure {
                            script {
                                notifyAfterFailure()
                            }
                        }
                        cleanup {
                            script {
                                sh "sudo rm -rf ${WORKSPACE}/{*,.*} || true"
                            }
                        }
                    }
                }
                stage('Contract tests') {
                    steps {
                        build job: "OpenLMIS-contract-tests-pipeline/${params.contractTestsBranch}", propagate: true, wait: true,
                        parameters: [
                            string(name: 'serviceName', value: 'stockmanagement'),
                            text(name: 'customEnv', value: "OL_STOCKMANAGEMENT_VERSION=${STAGING_VERSION}")
                        ]
                        build job: "OpenLMIS-contract-tests-pipeline/${params.contractTestsBranch}", propagate: true, wait: true,
                        parameters: [
                            string(name: 'serviceName', value: 'requisition'),
                            text(name: 'customEnv', value: "OL_STOCKMANAGEMENT_VERSION=${STAGING_VERSION}")
                        ]
                        build job: "OpenLMIS-contract-tests-pipeline/${params.contractTestsBranch}", propagate: true, wait: true,
                        parameters: [
                            string(name: 'serviceName', value: 'fulfillment'),
                            text(name: 'customEnv', value: "OL_STOCKMANAGEMENT_VERSION=${STAGING_VERSION}")
                        ]
                    }
                    post {
                        failure {
                            script {
                                notifyAfterFailure()
                            }
                        }
                    }
                }
            }
        }*/
        stage('ERD generation') {
            agent {
                node {
                    label 'master'
                }
            }
            environment {
                PATH = "/usr/local/bin/:$PATH"
            }
            steps {
                dir('erd') {
                    sh(script: "../ci-erdGeneration.sh")
                    archiveArtifacts artifacts: 'erd-stockmanagement.zip'
                }
            }
            post {
                failure {
                    script {
                        notifyAfterFailure()
                    }
                }
            }
        }
        stage('Push image') {
            agent any
            when {
                expression {
                    env.GIT_BRANCH =~ /rel-.+/ || (env.GIT_BRANCH == 'master' && !VERSION.endsWith("SNAPSHOT"))
                }
            }
            steps {
                sh "docker pull openlmis/stockmanagement:${STAGING_VERSION}"
                sh "docker tag openlmis/stockmanagement:${STAGING_VERSION} openlmis/stockmanagement:${VERSION}"
                sh "docker push openlmis/stockmanagement:${VERSION}"
            }
            post {
                success {
                    script {
                        if (!VERSION.endsWith("SNAPSHOT")) {
                            currentBuild.setKeepLog(true)
                        }
                    }
                }
                failure {
                    script {
                        notifyAfterFailure()
                    }
                }
            }
        }
    }
    post {
        fixed {
            script {
                BRANCH = "${BRANCH_NAME}"
                if (BRANCH.equals("master") || BRANCH.startsWith("rel-")) {
                    slackSend color: 'good', message: "${env.JOB_NAME} - #${env.BUILD_NUMBER} Back to normal"
                }
            }
        }
    }
}

def notifyAfterFailure() {
    messageColor = 'danger'
    if (currentBuild.result == 'UNSTABLE') {
        messageColor = 'warning'
    }
    BRANCH = "${BRANCH_NAME}"
    if (BRANCH.equals("master") || BRANCH.startsWith("rel-")) {
        slackSend color: "${messageColor}", message: "${env.JOB_NAME} - #${env.BUILD_NUMBER} ${env.STAGE_NAME} ${currentBuild.result} (<${env.BUILD_URL}|Open>)"
    }
    emailext subject: "${env.JOB_NAME} - #${env.BUILD_NUMBER} ${env.STAGE_NAME} ${currentBuild.result}",
        body: """<p>${env.JOB_NAME} - #${env.BUILD_NUMBER} ${env.STAGE_NAME} ${currentBuild.result}</p><p>Check console <a href="${env.BUILD_URL}">output</a> to view the results.</p>""",
        recipientProviders: [[$class: 'CulpritsRecipientProvider'], [$class: 'DevelopersRecipientProvider']]
}

def processTestResults(status) {
    checkstyle pattern: '**/build/reports/checkstyle/*.xml'
    pmd pattern: '**/build/reports/pmd/*.xml'
    junit '**/build/test-results/*/*.xml'

    AbstractTestResultAction testResultAction = currentBuild.rawBuild.getAction(AbstractTestResultAction.class)
    if (testResultAction != null) {
        failuresCount = testResultAction.failCount
        echo "Failed tests count: ${failuresCount}"
        if (failuresCount > 0) {
            echo "Setting build unstable due to test failures"
            status = 'UNSTABLE'
        }
    }

    return status
}
