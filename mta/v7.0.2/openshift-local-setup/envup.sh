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

# Install MTA Operator
oc apply -f installmta.yaml

oc project openshift-mta
echo "Installing: MTA operator - please be patient, takes a few minutes"
sleep 60
oc delete clusterserviceversion mta-operator.v7.0.2 -n openshift-mta

sleep 120
while true; do
    OPERATOR_INSTALLED=$(oc get clusterserviceversions/mta-operator.v7.0.2 | grep Succeeded)
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

oc apply -f keycloak-route.yaml
oc get route
KEYCLOAK_USER=$(oc get secret credential-mta-rhsso --template={{.data.ADMIN_USERNAME}} | base64 -d)
KEYCLOAK_PWD=$(oc get secret credential-mta-rhsso --template={{.data.ADMIN_PASSWORD}} | base64 -d)
echo "Keycloak Credentials: $KEYCLOAK_USER  $KEYCLOAK_PWD"
echo 'MTA UI Credentials: admin Passw0rd!'
