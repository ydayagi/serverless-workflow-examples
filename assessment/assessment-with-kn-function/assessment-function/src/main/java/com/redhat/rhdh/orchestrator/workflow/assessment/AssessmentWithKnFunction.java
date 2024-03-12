package com.redhat.rhdh.orchestrator.workflow.assessment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class AssessmentWithKnFunction {

  @RestController
  class AssessmentController {

    @GetMapping("/")
    @ResponseBody
    public WorkflowOptions execute(@RequestParam String repositoryUrl) {
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

  public static void main(String[] args) {
    SpringApplication.run(AssessmentWithKnFunction.class, args);
  }
}
