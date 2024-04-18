Setup OpenShift Local Instance with MTA Operator (for use with only development/testing. Not for production use)

## Prerequisites
* Developer machine OS: macos
* Installed [OpenShift Local](https://console.redhat.com/openshift/create/local) being available
* Running commands on developer machine: crc, oc

## Setup OpenShift Local Instance
* Run the following command in a terminal window
```shell
./envup.sh
```

## Setup the MTA Instance
* Get the MTA UI Route and in your browser navigate to the MTA UI. 
```shell
oc project openshift-mta
oc get route
```

* Login to the MTA UI with initial default credentials `admin/Passw0rd!` and change the password to your liking.

* Follow the [Jira Connection Guide](https://access.redhat.com/documentation/en-us/migration_toolkit_for_applications/7.0/html/user_interface_guide/creating-configuring-jira-connection#doc-wrapper) and setup the Jira instance connection for the workflow

* Test the setup and see if you are getting the tracker you just configured
    * To get the Auth Token for the following call use this command.
    ```shell
    curl --location 'https://<YOUR MTA UI URL>/hub/auth/login' \
        --header 'Accept: application/json, text/plain, */*' \
        --header 'Content-Type: application/json' \
        --data '{
            "user": "admin",
            "password": "<YOUR PASSWORD>"
    }'
    ```

    * Be sure the following call returns the Jira Connection you just configured.
    ```shell
    curl --location 'https://<YOUR MTA UI URL>/hub/trackers' \
        --header 'Accept: application/json, text/plain, */*' \
        --header 'Authorization: Bearer <YOUR AUTH TOKEN>' 
    ```

## Appendix

### Create MTA Credential for Jira via command line
```shell
curl --location 'https://<Your MTA URL>/hub/identities' \
--header 'Accept: application/json, text/plain, */*' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer <Your Auth Token>' \
--data-raw '{
    "name": "jira",
    "description": "",
    "kind": "basic-auth",
    "key": "",
    "settings": "",
    "user": "<Your Jira User>",
    "password": "<Your Jira Password>"
}'
```

### Create Tracker for Jira in MTA via command line
```shell
curl --location 'https://<Your MTA URL>/hub/trackers' \
--header 'Accept: application/json, text/plain, */*' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer <Your Auth Token>' \
--data '{
    "name": "jira",
    "url": "https://<your instance name>.atlassian.net/",
    "id": 0,
    "kind": "jira-cloud",
    "message": "",
    "connected": false,
    "identity": {
        "id": 1,
        "name": "jira"
    },
    "insecure": false
}'
```

### OpenShift Local VM Configuration tips
* You may need a good OpenShift Local configuration for the entire setup to work somewhat smoothly.
* You can configure memory, cpu cores and disk sizes by setting various CRC properties. You can see a list of properties by using this command.
```shell
crc config

# E.g. to set the memory to 30GiB use the following
crc config set memory 30000
```

## Known Issue

### CRC vm unexpectedly shut down
* Sometimes multiple concurrent operations on OpenShift Local such as accessing Console UI, CLI causes the CRC vm to shutdown.
* In this case you may benefit by increasing memory, disk and/or cpu core resources and then simply re-run/re-install the environment using the script.
