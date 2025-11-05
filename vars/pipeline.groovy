import com.lokesh.build.Cal

def call(Map pipelineParams) {
    Cal one = new Cal(this)
}
pipeline {
    agent any

    // Add a parameter for lokeshapp and use params in environment
    environment {
        MYAPP = "${params.lokeshapp}"
    }

    parameters {
        string(name: 'lokeshapp', defaultValue: 'Udathaapp', description: 'Application name')
        string(name: 'USER', defaultValue: 'lokesh udatha', description: 'Enter your full name here:')
        booleanParam(name: 'Run', defaultValue: true, description: 'Run your test cases?')
        choice(name: 'Env', choices: ['build', 'test', 'prod'], description: 'Select one environment here:')
    }

    triggers {
        // runs every minute — keep only if you really want this frequency
        cron('*/1 * * * *')
        pollSCM('*/1 * * * *')
    }

    stages {
        stage('Input Approval') {
            steps {
                // input returns a value if you want, but here we simply pause for approval
                input message: 'Do you want to proceed?', ok: 'Yes, continue'
                echo "Stage 'Input Approval' completed"
            }
        }

        stage('Build and Test') {
            parallel {
                stage('Build') {
                    steps {
                        script {
                            try {
                                echo "If MYAPP (${env.MYAPP}) is correct, print the below command"
                                // example of invoking a shell step if needed:
                                // sh "echo Building ${env.MYAPP}"
                            } catch(Exception e) {
                                error("Stopping this pipeline due to error: ${e.message}")
                            } finally {
                                echo "Build stage is completed"
                            }
                        }
                    }
                }

                stage('Test') {
                    when {
                        expression {
                            return params.Run == true
                        }
                    }
                    steps {
                        echo "Running unit tests..."
                        // sh 'mvn test' or similar
                    }
                }
            }
        }

        stage('Deploy') {
            when {
                // deploy only when Env is build OR test OR prod (adjust logic as needed)
                anyOf {
                    expression { return params.Env == 'build' }
                    expression { return params.Env == 'test' }
                    expression { return params.Env == 'prod' }
                }
            }
            steps {
                script {
                    if (params.Env == 'prod') {
                        echo "Production deployment path running"
                        // steps for prod
                    } else if (params.Env == 'test') {
                        echo "Deploying to TEST environment"
                        // steps for test
                    } else if (params.Env == 'build') {
                        echo "Deploying to BUILD environment"
                        // steps for build
                    } else {
                        echo "Unknown environment: ${params.Env}"
                    }
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline succeeded"
        }
        failure {
            echo "Pipeline failed"
        }
        always {
            echo "Pipeline execution completed"
        }
    }
}
