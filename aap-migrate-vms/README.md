# AAP (Ansible automation platform) VM migration job workflow
This workflow launches the VM migration AAP job template
The following two (2) inputs are required:
- Job template Id
- MTV migration request

## Workflow diagram
![AAP job workflow diagram](https://github.com/parodos-dev/serverless-workflow-examples/blob/main/aap-vm-migrate/aap-vm-migrate.svg?raw=true)

## Prerequisites
* A running instance of AAP with admin credentials and VM migration template installed.
* A running instance of Backstage notification plugin.

## Workflow application configuration
Application properties can be initialized from environment variables before running the application:

| Environment variable  | Description | Mandatory |
|-----------------------|-------------|-----------|
| `AAP_URL`       | The AAP server URL - protocol and hostname. E.g. https://myhost.org | ✅ |
| `AAP_USERNAME`      | The AAP server password | ✅ |
| `AAP_PASSWORD`      | The AAP server password | ✅ |

## How to run

```bash
mvn clean quarkus:dev
```

Example of POST to trigger the workflow:
```bash
curl -XPOST -H "Content-Type: application/json" http://localhost:8080/aap-job -d '{
    "jobTemplateId": 9,
    "mtvMigrationRequest": {
        "network_map": "flpath-orchestrator-mtv",
        "destination": "host",
        "storage_map": "flpath-orchestrator-mtv",
        "source": "vsphere-provider",
        "vms": [
            {
                "name": "mtv-1",
                "id": "vm-55237"
            }
        ]
    }
}'
```

Response:
```
{
  "id": "4332cad3-3457-4362-b75b-fe15cf637096",
  "workflowdata": {
    "jobTemplateId": 9,
    "mtvMigrationRequest": {
      "network_map": "flpath-orchestrator-mtv",
      "destination": "host",
      "storage_map": "flpath-orchestrator-mtv",
      "source": "vsphere-provider",
      "vms": [
        {
          "name": "mtv-1",
          "id": "vm-55237"
        }
      ]
    },
    "launchedJob": {
      "id": 35,
      "failed": false,
      "status": "pending"
    }
  }
}
```

