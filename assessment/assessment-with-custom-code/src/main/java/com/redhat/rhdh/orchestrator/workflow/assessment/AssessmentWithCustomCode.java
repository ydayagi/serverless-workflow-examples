package com.redhat.rhdh.orchestrator.workflow.assessment;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class AssessmentWithCustomCode {
    public WorkflowOptions execute(String inputText) {
        WorkflowOptions workflowOptions = new WorkflowOptions();
        if (inputText.toLowerCase().contains("dummy")) { // basic check for infrastructure workflow options recommendation
            workflowOptions.setCurrentVersion(new WorkflowOption("dummy-infra-workflow-option", "Dummy infra workflow option"));
            return workflowOptions;
        }
        return workflowOptions;
    }
}
