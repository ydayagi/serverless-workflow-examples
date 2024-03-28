package com.redhat.rhdh.orchestrator.function;

import org.jboss.logging.Logger;
import io.quarkus.funqy.Funq;
import java.util.ArrayList;

public class AssessmentFunc {
    private static final Logger log = Logger.getLogger(AssessmentFunc.class);

    @Funq("execute")
    public WorkflowOptions execute(FunInput input) {
        WorkflowOptions workflowOptions = new WorkflowOptions();
        if (null != input && 
            null != input.getInputText() && 
            input.getInputText().toLowerCase().contains("dummy")) { // basic check for infrastructure workflow options recommendation
            workflowOptions.setCurrentVersion(new WorkflowOption("dummy-infra-workflow-option", "Dummy infra workflow option"));
            return workflowOptions;
        }
        return workflowOptions;
    }

    public static class FunInput {
        public String inputText;

        public FunInput() {
        }

        public FunInput(String inputText) {
            this.inputText = inputText;
        }

        public String getInputText() {
            return inputText;
        }

        public void setInputText(String inputText) {
            this.inputText = inputText;
        }
    }
}
