# Ansible automation platform job workflow
This workflow launches an Ansible Automation Platform (AAP) job and send out a notification for success or failure upon completion.
The following two (2) inputs are required:
- Job template Id
- Inventory group

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
curl -XPOST -H "Content-Type: application/json" http://localhost:8080/aap-job -d '{"jobTemplateId": _JOB_TEMPLATE_ID_, "inventoryGroup": "_INVENTORY_GROUP_", "limit": "_LIMIT_"}'
```

Response:
```
{
    "id": "832685aa-0df0-4cf9-9e91-820b013efda6",
    "workflowdata": {
        "jobTemplateId": 17,
        "inventoryGroup": "ALL_rhel",
        "limit": "fancy-seal",
        "launchedJob": {
            "id": 29,
            "failed": false,
            "status": "pending",
            "outputUrl": "https://your-app-platform.io/#/jobs/playbook/29/output"
        }
    }
}
```

