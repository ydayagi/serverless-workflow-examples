package com.redhat.rhdh.orchestrator.workflow.assessment;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class AssessmentWithCustomCode {
    public WorkflowOptions execute(String repositoryUrl) {
        WorkflowOptions workflowOptions = new WorkflowOptions();
        if (repositoryUrl.toLowerCase().contains("java")) { // basic check for workflow options recommendation
            workflowOptions.setCurrentVersion(new WorkflowOption("move2kube", "Move2Kube"));
            workflowOptions.setUpgradeOptions(new ArrayList<>());
            workflowOptions.setMigrationOptions(new ArrayList<>());
            workflowOptions.setNewOptions(new ArrayList<>());
            workflowOptions.setContinuationOptions(new ArrayList<>());
            workflowOptions.setOtherOptions(new ArrayList<>());
            return workflowOptions;
        }
        return workflowOptions;
    }
}
