def call(String gitHubOrg, String gitHubRepo, String deployUrl, String environment = 'staging', String credentialId = 'cloudbees-ci-workshop-github-app') {        
  withCredentials([usernamePassword(credentialsId: "${credentialId}", usernameVariable: 'GITHUB_APP', passwordVariable: 'GITHUB_ACCESS_TOKEN')]) {
   def deploymentId =  sh(script: """
      curl \
        -X POST \
        -H 'authorization: Bearer ${GITHUB_ACCESS_TOKEN}' \
        -H 'Accept: application/vnd.github.antiope-preview+json' \
        -H 'Accept: application/vnd.github.v3+json' \
        https://api.github.com/repos/${gitHubOrg}/${gitHubRepo}/deployments \
        --data '{"ref":"${BRANCH_NAME}","environment":"${environment}","required_contexts":[],"description":"CloudBees CI Deployment"}' \
        | jq -r '.id' | tr -d '\n' 
    """, returnStdout: true)
    env.GITHUB_DEPLOYMENT_ID = deploymentId
    gitHubDeployStatus(gitHubOrg, gitHubRepo, deployUrl)
   
  }
} 