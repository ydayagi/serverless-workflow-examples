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
## Deploy sample workflows
### Greeting workflow
```shell
kustomize build kustomize/workflows/sonataflow-greeting/ | kubectl apply -f -
```
### Event with timeout
This sample is waiting for at maximum 30 seconds for `event1` then for `event2` to be received.
```shell
kustomize build kustomize/workflows/sonataflow-event-timeout/ | kubectl apply -f -
```

## Testing the Sample Work Flows
### Greeting workflow
Once the deployment above is complete, we need to get the workflow route of the workflow greeting service:
* For OpenShift:
```shell
kubectl get route
```

*For k8s:
```shell
kubectl get svc
NAME                   TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)        AGE
...
greeting               ClusterIP   10.105.141.132   <none>        80/TCP         166m
...

kubectl port-forward  svc/greeting 8080:80 &
```

Now that we have the route, execute the following to trigger an execution.

* Check if you get a response from the greeting workflow 
```shell
curl -X POST -H 'Content-Type:application/json' -H 'Accept:application/json' -d '{"name": "SonataFlow", "language": "English"}'    http://<Your greeting workflow route>/greeting
```
* A sample response
```json
{"id":"bf05e03f-a996-4482-aff7-89aa4a173be9","workflowdata":{"name":"SonataFlow","language":"English","greeting":"Hello from JSON Workflow, "}}
```
### Event with timeout
Once the deployment above is complete, we need to get the workflow route of the workflow `event-timeout` service:
* For OpenShift:
```shell
kubectl get route
```

* For k8s:
```shell
kubectl get svc
NAME                   TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)        AGE
...
event-timeout          ClusterIP   10.110.66.233    <none>        80/TCP         62m
...

kubectl port-forward  svc/event-timeout 8081:80 &
```

Now that we have the route, execute the following to trigger an execution.

* Check if you get a response from the greeting workflow
```shell
curl -X POST -H 'Content-Type:application/json' -H 'Accept:application/json' -d '{}'    http://<Your event timeout workflow route>/event-timeout
```
* A sample response
```json
{"id":"dda51c0b-01ec-4630-a494-8f25682be7d6","workflowdata":{}}
```

* You can check the running workflows instances by sending
```shell
curl -i -X GET -H 'Content-Type:application/json' -H 'Accept:application/json' -d '{}' 'http://localhost:8081/event-timeout'
```
* A sample response
```json
[{"id":"dda51c0b-01ec-4630-a494-8f25682be7d6","workflowdata":{}}]
```

Then we need to send the `event1` cloud event: take the `id` from the previous response and replace it in the following command
```shell
curl -i -X POST -H 'Content-Type: application/cloudevents+json' -d '{"datacontenttype": "application/json", "specversion":"1.0","id":"<id>","source":"/local/curl","type":"event1_event_type","data": "{\"eventData\":\"Event1 sent from UI\"}", "kogitoprocrefid": "<id>" }' http://localhost:8081/
HTTP/1.1 202 Accepted
content-length: 0
```
In the above request, `id` can be any UUID and `kogitoprocrefid` shall be the `id` of the previously started workflow.

Then `event2` shall be sent:
```shell
curl -i -X POST -H 'Content-Type: application/cloudevents+json' -d '{"datacontenttype": "application/json", "specversion":"1.0","id":"<id>","source":"/local/curl","type":"event2_event_type","data": "{\"eventData\":\"Event2 sent from UI\"}", "kogitoprocrefid": "<id>" }' http://localhost:8081/
HTTP/1.1 202 Accepted
content-length: 0
```

In both request, it is the field `type` that specify the event sent.

If both request are send within 30 seconds each, in the logs of the `event-timeout` pod, we should see a message:
```shell
kubectl logs -f event-timeout-76dc79855f-hj57m
...
INFO  [org.kie.kog.ser.wor.act.SysoutAction] (kogito-event-executor-1) event-state-timeouts: dda51c0b-01ec-4630-a494-8f25682be7d6 has finalized. The event1 was received. -- The event2 was received.
...
```

If not, the log message would indicate which event(s) was(were) not received.

Either after the 2 events have been received or after 1 minutes (timeouts expired), you should not see the `id` of the workflow when listing the running instances.

You can see the timeout expiration notification(s) being sent by the `JobService` in the logs of its pod:
```shell
kubectl logs -f jobs-service-bddc7ff9d-hpsrq
...
# Trigger timeout for event1
2023-09-25 14:48:50,642 jobs-service-bddc7ff9d-hpsrq INFO  [org.kie.kogito.jobs.service.job.DelegateJob:-1] (vert.x-eventloop-thread-0) Executing for context JobDetails[id='0f6a5b02-d46e-45da-9008-f02173d76cad', correlationId='0f6a5b02-d46e-45da-9008-f02173d76cad', status=SCHEDULED, lastUpdate=null, retries=0, executionCounter=0, scheduledId='null', recipient=RecipientInstance{recipient=HttpRecipient{url='http://10.110.66.233:80/management/jobs/event-timeout/instances/<id of the workflow>/timers/-1', method='POST', headers={processInstanceId=<id of the workflow>, nodeInstanceId=43effaa9-1ab5-4b61-9dd9-ba52c3d07221, processId=event-timeout, rootProcessId=null, rootProcessInstanceId=null, Content-Type=application/json}, queryParams={}, payload=org.kie.kogito.jobs.service.api.recipient.http.HttpRecipientJsonPayloadData@f06caf04} org.kie.kogito.jobs.service.api.recipient.http.HttpRecipient@6a6342fe}, trigger=org.kie.kogito.timer.impl.SimpleTimerTrigger@352c2edd, executionTimeout=null, executionTimeoutUnit=null]
2023-09-25 14:48:50,660 jobs-service-bddc7ff9d-hpsrq INFO  [org.kie.kogito.jobs.service.job.DelegateJob:-1] (vert.x-eventloop-thread-0) Executed successfully with response JobExecutionResponse[message='null', code='200', timestamp=2023-09-25T14:48:50.659996Z[GMT], jobId='0f6a5b02-d46e-45da-9008-f02173d76cad']
# Trigger timeout for event2
2023-09-25 14:49:20,653 jobs-service-bddc7ff9d-hpsrq INFO  [org.kie.kogito.jobs.service.job.DelegateJob:-1] (vert.x-eventloop-thread-0) Executing for context JobDetails[id='297b2578-bef8-4699-b175-1e032e57f87c', correlationId='297b2578-bef8-4699-b175-1e032e57f87c', status=SCHEDULED, lastUpdate=null, retries=0, executionCounter=0, scheduledId='null', recipient=RecipientInstance{recipient=HttpRecipient{url='http://10.110.66.233:80/management/jobs/event-timeout/instances/<id of the workflow>/timers/-1', method='POST', headers={processInstanceId=<id of the workflow>, nodeInstanceId=69e1e801-0071-4250-84a9-79ccdf4834f1, processId=event-timeout, rootProcessId=, rootProcessInstanceId=, Content-Type=application/json}, queryParams={}, payload=org.kie.kogito.jobs.service.api.recipient.http.HttpRecipientJsonPayloadData@3652907c} org.kie.kogito.jobs.service.api.recipient.http.HttpRecipient@e96d7d8d}, trigger=org.kie.kogito.timer.impl.SimpleTimerTrigger@726fefbd, executionTimeout=null, executionTimeoutUnit=null]
2023-09-25 14:49:20,672 jobs-service-bddc7ff9d-hpsrq INFO  [org.kie.kogito.jobs.service.job.DelegateJob:-1] (vert.x-eventloop-thread-0) Executed successfully with response JobExecutionResponse[message='null', code='200', timestamp=2023-09-25T14:49:20.672492Z[GMT], jobId='297b2578-bef8-4699-b175-1e032e57f87c']
...
```

While the workflow is still active (the 2 events were not received and the timeouts did not expire yet), you can see the instance in the DB:
```shell
PGPASSWORD="sonataflow" psql --host 127.0.0.1 -U sonataflow -d sonataflow -p 5432
sonataflow=# select id, process_id, version, process_version from process_instances ;
                  id                  |  process_id   | version | process_version 
--------------------------------------+---------------+---------+-----------------
 150ed623-cf87-4b43-a1fe-11fe22434cfb | event-timeout |       0 | 0.0.1
```
The `payload` column was not queried on purpose.

Once the workflow has terminated, the entry is removed from the DB.
## Delete the deployment
```shell
kustomize build base/ | kubectl apply -f -
```
If not all resources were delete, repeat the last action one more time.

