# Ansible automation platform job workflow
This workflow launches an Ansible Automation Platform (AAP) job from a given job template id and send out a notification for success or failure upon completion.


## Prerequisite
* A running instance of AAP with admin credentials. 
* A running instance of Backstage notification plugin.

### Requiqued Properties 
List of required properties to run the workflow:
- BACKSTAGE_NOTIFICATIONS_URL
- AAP_URL 
- AAP_USERNAME
- AAP_PASSWORD

## How to run

```bash
mvn clean quarkus:dev
```

Example of POST to trigger the workflow:
```bash
curl -XPOST -H "Content-Type: application/json" http://localhost:8080/app-job -d '{"jobTemplateId": _JOB_TEMPLATE_ID_}'
```

Response:
```
{
  "workflowdata": {
    "launchedJob": {
      "id": 31,
      "failed": false,
      "status": "pending",
      "outputUrl": "https://your-app-platform.io/#/jobs/playbook/31/output"
    },
    "jobTemplateId": 7
  }
}
```

Subsequently (about 30s delay), the workflow will repeatedly pull job with its status to check if the job is done with success or failure to send out relevant notification.

For job completion with success, the log outputs look like:
```
[org.kie.kog.ser.wor.dev.DevModeServerlessWorkflowLogger] (executor-thread-2) Triggered node 'getJob' for process 'aap-job' (3aa93369-5914-4211-b7f3-b218de1630e1)
[org.kie.kog.ser.wor.dev.DevModeServerlessWorkflowLogger] (executor-thread-2) Triggered node 'IsJobDone' for process 'aap-job' (3aa93369-5914-4211-b7f3-b218de1630e1)
[org.kie.kog.ser.wor.dev.DevModeServerlessWorkflowLogger] (executor-thread-2) Triggered node 'SendSuccessNotification' for process 'aap-job' (3aa93369-5914-4211-b7f3-b218de1630e1)
```
