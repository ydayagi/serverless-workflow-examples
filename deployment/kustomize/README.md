Deploy SonataFlow services and workflows

## Prerequisites
* An Openshift, Minikube or Openshift Local cluster
* You must be logged in to the cluster from command line

## Minikube

```shell
minikube start --cpus 4 --memory 10240 --addons registry --addons metrics-server --insecure-registry "10.0.0.0/24" --insecure-registry "localhost:5000"
```

## Deploy PostgreSQL, Data Index, Jobs Service

```shell
kustomize build kustomize/base/ | kubectl apply -f -
```
You may have the following error:
```shell
error: unable to recognize "STDIN": no matches for kind "SonataFlowPlatform" in version "sonataflow.org/v1alpha08"
```
In that case, simply re-run 
```shell
kustomize build kustomize/base/ | kubectl apply -f -
```

## Deploy sample workflow
```shell
kustomize build kustomize/workflows/sonataflow-greeting/ | kubectl apply -f -
```

## Testing the Sample Work Flow

For OpenShift:
* After the deployment above is complete, retrieve the route of the workflow greeting service
```shell
kubectl get route
```

For k8s:
```shell

```

* Check if you get a response from the greeting workflow 
```shell
curl -X POST -H 'Content-Type:application/json' -H 'Accept:application/json' -d '{"name": "SonataFlow", "language": "English"}'    http://<Your greeting workflow route>/greeting
```
* A sample response
```json
{"id":"bf05e03f-a996-4482-aff7-89aa4a173be9","workflowdata":{"name":"SonataFlow","language":"English","greeting":"Hello from JSON Workflow, "}}
```

## Delete the deployment
```shell
kustomize build base/ | kubectl apply -f -
```
If not all resources were delete, repeat the last action one more time.