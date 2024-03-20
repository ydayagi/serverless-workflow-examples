# Assessment
The assessment common use case is to assess inputs (ie: a link to their project code and/or an application identifying code), and based on a logic determined by the enterprise returns a list of infrastructure workflows.

A workflow of type assessment is then a workflow that performs some checks on user's input(s) and then recommends suitable workflows of type infrastucture (aka workflow options) for the next step.

## Goal
The goal in these examples is to show how an assessment workflow can be implemented:
- using **jq expression**
- using **custom java code**
- using **knative function**

## Flow
An assessment flow usually consists of:
- **Start**
  - get the user's input(s)
- **Assessment**
  - perform the desired check or evaluation against the user's input(s)
  - return suitable infrastructure workflow options
- **PreCheck**
  - validate whether the workflows in the returned assessment options exist
  - if there are non-existed workflows in the options, then remove them from the options and output the remaining valid ones
- **End**

**Note:** The workflow options must be an object with six fields: _currentVersion, upgradeOptions, migrateOptions, newOptions, continuationOptions, otherOptions_.
