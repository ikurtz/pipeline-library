// vars/cloudRunDeploy.groovy
def call(Map config) {
  def podYaml = libraryResource 'podtemplates/cloud-run.yml'
  def label = "cloudrun-${UUID.randomUUID().toString()}"
  def CLOUD_RUN_URL
  podTemplate(name: 'cloud-run-pod', label: label, yaml: podYaml, nodeSelector: 'workload=general') {
    node(label) {
      container(name: 'gcp-sdk') {
        if (config.deployType == "gke") {
          sh "gcloud beta run deploy ${config.serviceName} --image ${config.image} --platform gke --cluster ${config.clusterName} --cluster-location ${config.region} --namespace ${config.namespace} --format=json"
        }
        else if (config.deployType == "vmware") {
          sh "gcloud beta run deploy ${config.serviceName} --image ${config.image} --platform kubernetes --namespace ${config.namespace} --kubeconfig ${config.kubeconfig} --format=json"
        }
        else {
          sh "gcloud beta run deploy ${config.serviceName} --image ${config.image} --allow-unauthenticated --region ${config.region} --platform managed --format=json"
          CLOUD_RUN_URL = sh (script: "gcloud beta run services describe ${config.serviceName} --region ${config.region} --platform managed --format=json | jq -r '.url' | tr -d '\n'", 
                returnStdout: true)
        } 
      }
      withCredentials([usernamePassword(credentialsId: 'cbdays-github-token', usernameVariable: 'USERNAME', passwordVariable: 'TOKEN')]) {
        sh """
          curl -s -H "Authorization: token ${TOKEN}" \
            -X POST -d '{"body": "Preview Environment URL: ${CLOUD_RUN_URL}"}' \
            "https://api.github.com/repos/cloudbees-days/blog/issues/10/comments"
        """
      }
    }
  }
}