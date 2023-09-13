# Simple escalation workflow
An escalation workflow integrated with Atlassian JIRA using [SonataFlow](https://sonataflow.org/serverlessworkflow/latest/index.html).

Email service is using [MailTrap Send email API](https://api-docs.mailtrap.io/docs/mailtrap-api-docs/bcf61cdc1547e-send-email-early-access) API

## Prerequisite
* Access to a Jira server (URL, user and [API token](https://support.atlassian.com/atlassian-account/docs/manage-api-tokens-for-your-atlassian-account/))
* Access to an OpenShift cluster with `admin` Role
* An account to [MailTrap](https://mailtrap.io/home) with a [testing Inbox](https://mailtrap.io/inboxes) and an [API token](https://mailtrap.io/api-tokens)

## Escalation flow

    Create a new JIRA ticket ->
    
    Watch Jira ticket status (Wait for 'done' status) ->
    
        if approved before timeout ->
            Create the namespace in configured Red Hat Openshift Cluster
    
        if timeout has reached ->
            Send email to provided email address

**Note**:
The value of the `.jiraIssue.fields.status.statusCategory.key` field is the one to be used to identify when the `done` status is reached, all the other
similar fields are subject to translation to the configured language and cannot be used for a consistent check.

## Hardcoded settings
Some settings are hardcoded in [ticketEscalation.sw.yaml](./src/main/resources/ticketEscalation.sw.yaml), see below details.

Jira project name is hardcoded in:
```yaml
states:
  - name: CreateJiraIssue
...
              project:
                key: "TEST"
...
```

Polling periodicity and escalation timeout are hardcoded in:
```yaml
states:
...
  - name: GetJiraIssue
...
        sleep:
          before: PT6S
...
  - name: TicketDone
    type: switch
    dataConditions:
      - condition: (.jiraIssue.fields.status.statusCategory.key == "done")
        transition:
          nextState: CreateK8sNamespace
      - condition: (.jiraIssue.fields.status.statusCategory.key != "done" and .timer.triggered == false and .timer.elapsedSeconds > 60)
        transition:
          nextState: Escalate
...
```

The sender email and inbox ID are hardcoded in:
```yaml
states:
...
  - name: Escalate
...
      - name: "sendEmail"
...
          arguments:
            inbox_id: 2403453
...
            from:
              email: escalation@gmail.com
```

## How to run
Application properties can be initialized from environment variables before running the application:

```bash
export JIRA_URL=_YOUR_JIRA_URL_
export JIRA_USERNAME=_YOUR_JIRA_USERNAME_
export JIRA_PASSWORD=_YOUR_JIRA_TOKEN_

export MAILTRAPIO_TOKEN=_YOUR_MAINTRAPIO_API_TOKEN_

export OCP_API_SERVER_URL=_YOUR_OPENSHIFT_API_SERVER_URL_
export OCP_API_SERVER_TOKEN=_YOUR_OPENSHIFT_API_TOKEN_
mvn clean quarkus:dev
```

Example of POST to trigger the flow (see input schema in [ocp-onboarding-schema.json](./src/main/resources/ocp-onboarding-schema.json)):
```bash
curl -XPOST -H "Content-Type: application/json" http://localhost:8080/ticket-escalation -d '{"namespace": "_YOUR_NAMESPACE_", "manager": "_YOUR_EMAIL_"}'
```

Tips:
* Visit [Workflow Instances](http://localhost:8080/q/dev/org.kie.kogito.kogito-quarkus-serverless-workflow-devui/workflowInstances)
* Visit (Data Index Query Service)[http://localhost:8080/q/graphql-ui/]