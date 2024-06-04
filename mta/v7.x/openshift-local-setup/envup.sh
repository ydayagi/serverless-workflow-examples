#!/bin/bash
# Cleanup previous instance
crc stop
crc delete -f
CRC_START_CMD_OUTPUT=$(crc start -o json)

echo "crc start output: $CRC_START_CMD_OUTPUT" 

# Extract variables
ADMIN_USER=$(echo $CRC_START_CMD_OUTPUT | jq ".clusterConfig.adminCredentials.username" | tr -d "\"") 
ADMIN_PWD=$(echo $CRC_START_CMD_OUTPUT | jq ".clusterConfig.adminCredentials.password" | tr -d "\"")
API_URL=$(echo $CRC_START_CMD_OUTPUT | jq ".clusterConfig.url" | tr -d "\"")
WEB_CONSOLE_URL=$(echo $CRC_START_CMD_OUTPUT | jq ".clusterConfig.webConsoleUrl" | tr -d "\"")
echo "ADMIN_USER: $ADMIN_USER, ADMIN_PWD: $ADMIN_PWD, API_URL: $API_URL, WEB_CONSOLE_URL: $WEB_CONSOLE_URL"

# Open Web Console
open $WEB_CONSOLE_URL/k8s/all-namespaces/operators.coreos.com~v1alpha1~ClusterServiceVersion

# Login to the cluster
oc login -s=$API_URL -u=$ADMIN_USER -p=$ADMIN_PWD
oc create ns openshift-mta

if [ -z "$MTA_OPERATOR_VERSION" ]; then
    MTA_OPERATOR_VERSION=v7.0.3 # Note: The resources in installmta.yaml should match this version
    echo "Using the MTA Operator version: $MTA_OPERATOR_VERSION"
fi

MTA_OPERATOR_NAME=mta-operator."$MTA_OPERATOR_VERSION"
echo "Using the MTA Operator: $MTA_OPERATOR_NAME"

# Install MTA Operator
oc apply -f installmta.yaml

oc project openshift-mta
echo "Installing: MTA operator - please be patient, takes a few minutes"
sleep 60
oc delete clusterserviceversion "$MTA_OPERATOR_NAME" -n openshift-mta

sleep 120
while true; do
    OPERATOR_INSTALLED=$(oc get clusterserviceversions/"$MTA_OPERATOR_NAME" | grep Succeeded)
    if [ -n "$OPERATOR_INSTALLED" ]; then
        echo "Installed: MTA operator "
        break
    else
        echo "Installing: MTA operator "
        sleep 30
    fi
done

# Create Tackle
oc apply -f tackle.yaml

# Ensure MTA UI Route is available
echo "Waiting for: MTA UI Route - please be patient, takes a few minutes"
sleep 420
while true; do
    ROUTE=$(oc get route | grep mta-ui)
    if [ -n "$ROUTE" ]; then
        echo "MTA Route Available "
        break
    else
        echo "Waiting for: MTA UI Route"
        sleep 30
    fi
done

oc get route
echo 'MTA UI Credentials: admin Passw0rd!'
