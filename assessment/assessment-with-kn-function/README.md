# Assessment workflow with kn function
This assessment workflow uses kn function to evaluate a user's input text in order to provide recommendation of the suitable infrastrcuture workflow options.

**Note**:
This example assumes that the infrastructure workflow options returned upon assessment are available.
The dummy infrastructure workflow options provided into `dummy-infra-workflow-option` is purely illustrative.

## Workflow
![SWF VIZ](https://github.com/parodos-dev/serverless-workflow-examples/blob/main/assessment/assessment-with-kn-function/assessment-with-kn-function.svg)

## How to run

```bash
mvn clean quarkus:dev
```

Example of POST to trigger the flow:
```bash
curl -XPOST -H "Content-Type: application/json" http://localhost:8080/assessment-with-kn-function -d '{"inputText": "_YOUR_DUMMY_TEXT_"}'
```

Response:
```
{
  "workflowdata": {
    "result": {...},
    "preCheck": {...},
    "inputText": "_YOUR_DUMMY_TEXT_",
    "workflowOptions": {
      "newOptions": [],
      "otherOptions": [],
      "currentVersion": {
        "id": "dummy-infra-workflow-option",
        "name": "Dummy infra workflow option"
      },
      "upgradeOptions": [],
      "migrationOptions": [],
      "continuationOptions": []
    }
  }
}
```