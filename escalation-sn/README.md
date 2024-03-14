An escalation workflow integrated with ServiceNow using [SonataFlow](https://sonataflow.org/serverlessworkflow/latest/index.html).

## Prerequisite
* An available ServiceNow instance with admin credentials.
* Prerequisite data being available on ServiceNow instance with setup instructions [found here](instance-setup/readme.md)
* Janus-idp notifications service is deployed and functionally running with instructions [found here](https://github.com/janus-idp/backstage-plugins/tree/main/plugins/notifications-backend).

### Specifics about Notifications service
* Add the `manager` user in `notifications-backend/users.yaml` and your file could look something like this
```yaml
apiVersion: backstage.io/v1alpha1
kind: User
metadata:
  name: guest
spec:
  profile:
    displayName: Guest User
  memberOf: []
---
apiVersion: backstage.io/v1alpha1
kind: User
metadata:
  name: manager
spec:
  profile:
    displayName: Manager Approver User
  memberOf: []
```
* Restart the notifications service
```shell
yarn start:backstage
```

* Be sure the create notification call from the command line works successfully.
```shell
curl -X POST http://localhost:7007/api/notifications/notifications -H "Content-Type: application/json" -d '{"title": "My message title", "message": "I have nothing to say", "origin": "my-origin", "targetUsers": ["default/manager"]}' | jq '.'
```
   
* An example response could look like this
```yaml
{
    "messageId": "942b0aa0-79d4-46a7-a973-47573fa19543"
}
```

## Workflow Application configuration
Application properties can be initialized from environment variables before running the application:

| Environment variable  | Description | Mandatory |
|-----------------------|-------------|-----------|
| `SN_SERVER`            | The ServiceNow server URL | ✅ |
| `SERVICENOW_USERNAME`       | The ServiceNow server username | ✅ |
| `SERVICENOW_PASSWORD`      | The ServiceNow server password | ✅ |

## How to run

### Start the workflow application
```bash
mvn clean quarkus:dev
```

### Trigger/start the workflow
* Example of POST to trigger the flow (see input schema in [servicenow-escalation-schema.json](./src/main/resources/servicenow-escalation-schema.json)):
```bash
# This is a request sent to the workflow instance
curl -XPOST -H "Content-Type: application/json" http://localhost:8080/servicenow-escalation -d '{
    "description": "<ServiceNow change request description>",
    "short_description": "<ServiceNow change request short_description>",
    "comments": "<ServiceNow change request comments>",
    "state": "new",
    "assigned_to": "<ServiceNow Approver user sys_id> e.g. 950597b6973002102425b39fe153af41",
    "additional_assignee_list": "<ServiceNow Approver user sys_id> e.g. 950597b6973002102425b39fe153af41",
    "assignment_group": "<ServiceNow Approver group sys_id> e.g. e50597b6973002102425b39fe153afb2"
}' | jq '.'
```
* You should see a response similar to the following
```json
{
  "id": "99203918-3e8c-46a6-ba43-9a025172f8c2",
  "workflowdata": {
    "description": "Requester requesting an item",
    "short_description": "Requester requesting an item in short",
    "comments": "Requester requesting an item in comments",
    "state": "new",
    "assigned_to": "950597b6973002102425b39fe153af41",
    "additional_assignee_list": "950597b6973002102425b39fe153af41",
    "assignment_group": "e50597b6973002102425b39fe153afb2",
    "createdChangeRequest": {
      "result": {
        "sys_id": "6dfa4ff7973002102425b39fe153afed",
        "state": "-5",
        "number": "CHG0030045"
      }
    }
  }
}
```
* Wait for a minute or two before proceeding to the next step, to view notifications created by the workflow, to remind the approver to approve the created change request.
    * In the current implementation this reminder is generated every `30s` by the workflow.  

* After this wait, login to notifications service's postgres database console.

* You will see `reminder` notification(s) created by `Notifications service` as shown in the following example.
```text
                  id                  |                               message                               
--------------------------------------+---------------------------------------------------------------------
 8a3c945d-9009-4188-a28e-17ceee853a99 | Manager, please approve this change request: CHG0030045
```

### End the workflow by updating the change request

* Update the Change Request state on ServiceNow instance to help terminate the workflow
```bash
# This is a request sent to the ServiceNow instance
curl --location --request PUT 'https://<your servicenow instance>/api/now/table/change_request/<sys_id of the created change request above>' \
--header 'Content-Type: application/json' \
--header 'Authorization: Basic <your authorization header value>' \
--data '{
    "state": "-4",
    "approval": "requested"
}'
```

* You will see a `thank you` notification created by `Notifications service` as shown in the following example. 
Note: this may appear after a few seconds, as the workflow needs to wait for completion of the timeout event of `30s`, before this notification is created.
```text
                  id                  |                               message                               
--------------------------------------+---------------------------------------------------------------------
 3e9cd0a6-c4c8-4ea1-973a-dbb063279397 | Manager, thanks a ton for approving this change request: CHG0030045
```

Tips:
* Visit [Workflow Instances](http://localhost:8080/q/dev/org.kie.kogito.kogito-quarkus-serverless-workflow-devui/workflowInstances)
* Visit (Data Index Query Service)[http://localhost:8080/q/graphql-ui/]