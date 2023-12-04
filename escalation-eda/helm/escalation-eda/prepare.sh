#!/bin/bash
mkdir -p resources
cp -r ../../escalation-swf/src/main/resources .

# Logic to copy subflow and in case update the SWF.id field
# ...