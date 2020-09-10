library "pipeline-library@$BRANCH_NAME"
def gitHubCredId = 'field-workshops-github-app'
pipeline {
  agent none
  options {
    timeout(time: 20, unit: 'MINUTES') 
    disableConcurrentBuilds()
  }
  stages {
    stage('GitHub Tests') {
      agent {label 'default-jnlp' }
      stages {
        stage('GitHub Setup') {
          steps {
            withCredentials([usernamePassword(credentialsId: "${gitHubCredId}",
                usernameVariable: 'GITHUB_APP',
                passwordVariable: 'GITHUB_ACCESS_TOKEN')]) {
              sh(script: """
                curl -H 'Accept: application/vnd.github.antiope-preview+json' \
                     -H 'authorization: Bearer ${GITHUB_ACCESS_TOKEN}' \
                     -H "Accept: application/vnd.github.baptiste-preview+json" \
                     https://api.github.com/repos/cloudbees-days/simple-java-maven-app/generate \
                     --data '{"owner":"cloudbees-days","name":"pipeline-library-test"}'
                
                mkdir -p pipeline-library-test
                cd pipeline-library-test
                git init
                git config user.email "cbci.bot@workshop.cb-sa.io"
                git config user.name "CloudBees CI Bot"
                git remote add origin https://x-access-token:${GITHUB_ACCESS_TOKEN}@github.com/cloudbees-days/pipeline-library-test.git
                git pull origin master
                git fetch
                git checkout -B test-branch
                cp example.cloudbees-ci.yml cloudbees-ci.yml
                git add cloudbees-ci.yml
                git commit -a -m 'adding marker file'
                git push -u origin test-branch
                
                echo "create pull request"
                curl -H 'Accept: application/vnd.github.antiope-preview+json' \
                     -H 'authorization: Bearer ${GITHUB_ACCESS_TOKEN}' \
                     --data '{"title":"add marker file","head":"test-branch","base":"master"}' \
                     https://api.github.com/repos/cloudbees-days/pipeline-library-test/pulls
              """)
            }
            script {
              Map config
              config.message = "test pr comment"
              config.credId = gitHubCredId
              config.issueId = 1
              config.repoOwner = 'cloudbees-days'
              config.repo = 'pipeline-library-test'
              gitHubComment(config)
              
              withCredentials([usernamePassword(credentialsId: "${gitHubCredId}",
                  usernameVariable: 'GITHUB_APP',
                  passwordVariable: 'GITHUB_ACCESS_TOKEN')]) {
                def actualCommentBody =  sh(script: """
                  curl \
                    -H "Accept: application/vnd.github.v3+json" \
                    -H 'authorization: Bearer ${GITHUB_ACCESS_TOKEN}' \
                    https://api.github.com/repos/cloudbees-days/pipeline-library-test/issues/comments/1
                    | jq -r '.body' | tr -d '\n' 
                """, returnStdout: true)
                if(actualCommentBody != "test pr comment") {
                  error "Failed PR Comment Test"
                }
              }
            }
          }
        }
      }
      post {
        always {
          node('default-jnlp') {
            withCredentials([usernamePassword(credentialsId: "field-workshops-github-app",
                usernameVariable: 'GITHUB_APP',
                passwordVariable: 'GITHUB_ACCESS_TOKEN')]) {
              sh(script: """
                curl \
                  -X DELETE \
                  -H "Accept: application/vnd.github.v3+json" \
                  -H 'authorization: Bearer ${GITHUB_ACCESS_TOKEN}' \
                  https://api.github.com/repos/cloudbees-days/pipeline-library-test
              """)
            }
          }
        }
      }
    }
  }
}
    