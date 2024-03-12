# Assessment with knative function

This guide describes the steps required to deploy knative sample app to perform an assessment to your cluster.

## Deploying the app

After the build has completed and the container is pushed to Docker Hub, you can deploy the app into your cluster.

During the creation of a Service, Knative performs the following steps:

- Create a new immutable revision for this version of the app.
- Network programming to create a Route, ingress, Service, and load balancer for your app.
- Automatically scale your pods up and down, including scaling down to zero active pods.

Choose one of the following methods to deploy the app:

### yaml

1. Create a new file named `service.yaml` and copy the following service definition
   into the file:

    ```yaml
    apiVersion: serving.knative.dev/v1
    kind: Service
    metadata:
      name: assessment-with-kn-function
      namespace: default
    spec:
      template:
        spec:
          containers:
            - image: docker.io/{username}/assessment-with-kn-function
              env:
                - name: PORT
                  value: 8080
    ```
    Where `{username}` is your Docker Hub username.

    **Note:** Ensure that the container image value in `service.yaml` matches the container you built in the previous step.

1. Apply the YAML file by running the command:

    ```bash
    kubectl apply -f service.yaml
    ```

### kn

1. With `kn` you can deploy the service with

    ```bash
    kn service create "assessment-with-kn-function" --image=docker.io/{username}/assessment-with-kn-function --env PORT=8080
    ```

    This will wait until your service is deployed and ready, and ultimately it will print the URL through which you can access the service.

## Verification

1. Find the domain URL for your service:

    - For kubectl, run:

    ```bash
    kubectl get ksvc "assessment-with-kn-function"  --output=custom-columns=NAME:.metadata.name,URL:.status.url
    ```

    Example:

    ```bash
    NAME                      URL
    assessment-with-kn-function    http://assessment-with-kn-function.default.1.2.3.4.xip.io
    ```

    - For kn, run:

    ```bash
    kn service describe assessment-with-kn-function -o url
    ```

    Example:

    ```bash
    http://assessment-with-kn-function.default.1.2.3.4.xip.io
    ```

1. Make a request to your app and observe the result. Replace
   the following URL with the URL returned in the previous command.

    Example:

    ```bash
    curl http://assessment-with-kn-function.default.1.2.3.4.sslip.io?repositoryUrl=_YOUR_JAVA_REPOSITORY_
  
    {
        "id": "c9a0ce80-8cd2-49d2-81e1-05606e52c9c9",
        "workflowdata": {
            "workflowOptions": {
                "currentVersion": {
                    "id": "move2kube",
                    "name": "Move2Kube"
                },
                "upgradeOptions": [],
                "migrationOptions": [],
                "newOptions": [],
                "continuationOptions": [],
                "otherOptions": []
            }
        }
    }

    # Even easier with kn:
    curl $(kn service describe assessment-with-kn-function -o url)
    ```

    **Tip:** Add `-v` option to get more detail if the `curl` command fails.

## Deleting the app

To remove the sample app from your cluster, delete the service:

### kubectl
```bash
kubectl delete -f service.yaml
```

### kn
```bash
kn service delete assessment-with-kn-function
```
