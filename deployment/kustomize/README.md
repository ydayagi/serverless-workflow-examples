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

### Configure DB
1. Redirect the PostgreSQL service to your local host
```bash
kubectl port-forward --namespace postgres svc/postgres-db-service 5432:5432 &
```
2. Download the DDL archive for your version (here it's 1.44): https://repo1.maven.org/maven2/org/kie/kogito/kogito-ddl/
2. Decompress the file 
3. Create the schema for the Kogito runtime:
```bash
PGPASSWORD="sonataflow" psql --host 127.0.0.1 -U sonataflow -d sonataflow -p 5432 -a -f <path to the decompressed archive>/postgresql/V1.35.0__create_runtime_PostgreSQL.sql 
```
Find the credentials and the DB name in the [file postgres.properties](base/postgres.properties), above we are using the default values.

See https://sonataflow.org/serverlessworkflow/latest/persistence/postgresql-flyway-migration.html#manually-executing-scripts for more information about the migration.
## Deploy sample workflow
```shell
kustomize build kustomize/workflows/sonataflow-greeting/ | kubectl apply -f -
```

## Testing the Sample Work Flow
First we need to get the workflow route
### For OpenShift:
* After the deployment above is complete, retrieve the route of the workflow greeting service
```shell
kubectl get route
```

### For k8s:
```shell
kubectl get svc
NAME                   TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)        AGE
...
greeting               ClusterIP   10.105.141.132   <none>        80/TCP         166m
...

kubectl port-forward  svc/greeting 8080:80 &
```

### Execute 
Now that we have the route, execute the following to trigger an execution.

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

