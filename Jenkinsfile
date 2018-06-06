properties([
  [
    $class: 'ThrottleJobProperty',
    categories: ['pipeline'],
    throttleEnabled: true,
    throttleOption: 'category'
  ]
])
pipeline {
    agent any
    options {
        buildDiscarder(logRotator(numToKeepStr: '15'))
        disableConcurrentBuilds()
    }
    environment {
        PATH = "/usr/local/bin/:$PATH"
        COMPOSE_PROJECT_NAME = "${env.JOB_NAME}-${BRANCH_NAME}"
    }
    parameters {
        string(name: 'contractTestsBranch', defaultValue: 'master', description: 'The branch of contract tests to checkout')
    }
    stages {
        stage('Preparation') {
            steps {
                checkout scm

                withCredentials([usernamePassword(
                  credentialsId: "cad2f741-7b1e-4ddd-b5ca-2959d40f62c2",
                  usernameVariable: "USER",
                  passwordVariable: "PASS"
                )]) {
                    sh 'set +x'
                    sh 'docker login -u $USER -p $PASS'
                }
                script {
                    def properties = readProperties file: 'gradle.properties'
                    if (!properties.serviceVersion) {
                        error("serviceVersion property not found")
                    }
                    VERSION = properties.serviceVersion
                    STAGING_VERSION = properties.serviceVersion + "-STAGING"
                    currentBuild.displayName += " - " + VERSION
                }
            }
            post {
                failure {
                    slackSend color: 'danger', message: "${env.JOB_NAME} - #${env.BUILD_NUMBER} ${env.STAGE_NAME} FAILED (<${env.BUILD_URL}|Open>)"
                }
            }
        }
        stage('Build') {
            steps {
                withCredentials([file(credentialsId: '8da5ba56-8ebb-4a6a-bdb5-43c9d0efb120', variable: 'ENV_FILE')]) {
                    sh 'set +x'
                    sh 'sudo rm -f .env'
                    sh 'cp $ENV_FILE .env'
                    sh '''
                        if [ "$GIT_BRANCH" != "master" ]; then
                            sed -i '' -e "s#^TRANSIFEX_PUSH=.*#TRANSIFEX_PUSH=false#" .env  2>/dev/null || true
                        fi
                    '''

                    sh 'docker-compose -f docker-compose.builder.yml run -e BUILD_NUMBER=$BUILD_NUMBER -e GIT_BRANCH=$GIT_BRANCH builder'
                    sh 'docker-compose -f docker-compose.builder.yml build image'
                    sh 'docker-compose -f docker-compose.builder.yml down --volumes'
                    sh "docker tag openlmis/stockmanagement:latest openlmis/stockmanagement:${STAGING_VERSION}"
                    sh "docker push openlmis/stockmanagement:${STAGING_VERSION}"
                }
            }
            post {
                success {
                    archive 'build/libs/*.jar,build/resources/main/api-definition.html, build/resources/main/  version.properties'
                }
                failure {
                    slackSend color: 'danger', message: "${env.JOB_NAME} - #${env.BUILD_NUMBER} ${env.STAGE_NAME} FAILED (<${env.BUILD_URL}|Open>)"
                }
                always {
                    checkstyle pattern: '**/build/reports/checkstyle/*.xml'
                    pmd pattern: '**/build/reports/pmd/*.xml'
                    junit '**/build/test-results/*/*.xml'
                }
            }
        }
        stage('Parallel: Sonar analysis and contract tests') {
            parallel {
                stage('Sonar analysis') {
                    steps {
                        withSonarQubeEnv('Sonar OpenLMIS') {
                            withCredentials([string(credentialsId: 'SONAR_LOGIN', variable: 'SONAR_LOGIN'), string(credentialsId: 'SONAR_PASSWORD', variable: 'SONAR_PASSWORD')]) {
                                sh '''
                                    set +x
                                    sudo rm -f .env

                                    curl -o .env -L https://raw.githubusercontent.com/OpenLMIS/openlmis-ref-distro/master/settings-sample.env

                                    sed -i '' -e "s#spring_profiles_active=.*#spring_profiles_active=#" .env  2>/dev/null || true
                                    sed -i '' -e "s#^BASE_URL=.*#BASE_URL=http://localhost#" .env  2>/dev/null || true
                                    sed -i '' -e "s#^VIRTUAL_HOST=.*#VIRTUAL_HOST=localhost#" .env  2>/dev/null || true

                                    SONAR_LOGIN_TEMP=$(echo $SONAR_LOGIN | cut -f2 -d=)
                                    SONAR_PASSWORD_TEMP=$(echo $SONAR_PASSWORD | cut -f2 -d=)
                                    echo "SONAR_LOGIN=$SONAR_LOGIN_TEMP" >> .env
                                    echo "SONAR_PASSWORD=$SONAR_PASSWORD_TEMP" >> .env

                                    docker-compose -f docker-compose.builder.yml run sonar
                                    docker-compose -f docker-compose.builder.yml down --volumes

                                    sudo rm -vrf .env
                                '''
                                // workaround: Sonar plugin retrieves the path directly from the output
                                sh 'echo "Working dir: ${WORKSPACE}/build/sonar"'
                            }
                        }
                        timeout(time: 1, unit: 'HOURS') {
                            script {
                                def gate = waitForQualityGate()
                                if (gate.status != 'OK') {
                                    error 'Quality Gate FAILED'
                                }
                            }
                        }
                    }
                    post {
                        failure {
                            slackSend color: 'danger', message: "${env.JOB_NAME} - #${env.BUILD_NUMBER} ${env.STAGE_NAME} FAILED (<${env.BUILD_URL}|Open>)"
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
                            slackSend color: 'danger', message: "${env.JOB_NAME} - #${env.BUILD_NUMBER} ${env.STAGE_NAME} FAILED (<${env.BUILD_URL}|Open>)"
                        }
                    }
                }
            }
        }
        stage('ERD generation') {
            steps {
                dir('erd') {
                    sh '''#!/bin/bash -xe
                        # prepare ERD folder on CI server
                        sudo mkdir -p /var/www/html/erd-stockmanagement
                        sudo chown -R $USER:$USER /var/www/html/erd-stockmanagement

                        # General steps:
                        # - Copy env file and remove demo data profiles (errors happen during startup when they are enabled)
                        # - Copy ERD generation docker-compose file and bring up service with db container and wait
                        # - Clean out existing ERD folder
                        # - Create output folder (SchemaSpy uses it to hold ERD files) and make sure it is writable by docker
                        # - Use SchemaSpy docker image to generate ERD files and send to output, wait
                        # - Bring down service and db container
                        # - Make sure output folder and its subfolders is owned by user (docker generated files/folders are owned by docker)
                        # - Move output to web folder
                        # - Clean out old zip file and re-generate it
                        # - Clean up files and folders
                        wget https://raw.githubusercontent.com/OpenLMIS/openlmis-ref-distro/master/settings-sample.env -O .env \
                        && sed -i -e "s/^spring_profiles_active=demo-data,refresh-db/spring_profiles_active=/" .env \
                        && wget https://raw.githubusercontent.com/OpenLMIS/openlmis-stockmanagement/master/docker-compose.erd-generation.yml -O docker-compose.yml \
                        && (/usr/local/bin/docker-compose up &) \
                        && sleep 90 \
                        && sudo rm /var/www/html/erd-stockmanagement/* -rf \
                        && sudo rm -rf output \
                        && mkdir output \
                        && chmod 777 output \
                        && (docker run --rm --network erd_default -v $WORKSPACE/erd/output:/output schemaspy/schemaspy:snapshot -t pgsql -host db -port 5432 -db open_lmis -s stockmanagement -u postgres -p p@ssw0rd -I "(data_loaded)|(schema_version)|(jv_.*)" -norows -hq &) \
                        && sleep 30 \
                        && /usr/local/bin/docker-compose down --volumes \
                        && sudo chown -R $USER:$USER output \
                        && mv output/* /var/www/html/erd-stockmanagement \
                        && rm erd-stockmanagement.zip -f \
                        && pushd /var/www/html/erd-stockmanagement \
                        && zip -r $WORKSPACE/erd/erd-stockmanagement.zip . \
                        && popd \
                        && rmdir output \
                        && rm .env \
                        && rm docker-compose.yml
                    '''
                    archiveArtifacts artifacts: 'erd-stockmanagement.zip'
                }
            }
            post {
                failure {
                    slackSend color: 'danger', message: "${env.JOB_NAME} - #${env.BUILD_NUMBER} ${env.STAGE_NAME} FAILED (<${env.BUILD_URL}|Open>)"
                }
            }
        }
        stage('Push image') {
            when {
                expression {
                    return env.GIT_BRANCH == 'master' || env.GIT_BRANCH =~ /rel-.+/
                }
            }
            steps {
                sh "docker tag openlmis/stockmanagement:${STAGING_VERSION} openlmis/stockmanagement:${VERSION}"
                sh "docker push openlmis/stockmanagement:${VERSION}"
            }
            post {
                failure {
                    slackSend color: 'danger', message: "${env.JOB_NAME} - #${env.BUILD_NUMBER} ${env.STAGE_NAME} FAILED (<${env.BUILD_URL}|Open>)"
                }
            }
        }
    }
    post {
        fixed {
            slackSend color: 'good', message: "${env.JOB_NAME} - #${env.BUILD_NUMBER} Back to normal"
        }
        success {
            build job: 'OpenLMIS-stockmanagement-deploy-to-test', wait: false
        }
    }
}
