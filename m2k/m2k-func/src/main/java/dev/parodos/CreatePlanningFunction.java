package dev.parodos;

import dev.parodos.move2kube.ApiException;
import dev.parodos.service.FolderCreatorService;
import dev.parodos.service.GitService;
import dev.parodos.service.Move2KubeService;
import io.quarkus.funqy.Funq;
import io.quarkus.funqy.knative.events.CloudEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

@ApplicationScoped
public class CreatePlanningFunction {
  private static final Logger log = LoggerFactory.getLogger(CreatePlanningFunction.class);

  @Inject
  GitService gitService;

  @Inject
  Move2KubeService move2KubeService;

  @Inject
  FolderCreatorService folderCreatorService;



  public static final String SOURCE = "create-plan";


  @Funq("createPlan")
  public CloudEvent<EventGenerator.EventPOJO> createPlan(FunInput input) {
    if (!input.validate()) {
      return EventGenerator.createErrorEvent(input.workflowCallerId, String.format("One or multiple mandatory input field was missing; input: %s", input),
          SOURCE);
    }

    try {
      Path zipFile = Paths.get(folderCreatorService.createPlanFolder(input.gitRepo,
              String.format("%s-%d", input.branch, new Date().getTime())).toString(),
          "/output.zip");

      try {
        gitService.generateRepositoryArchive(input.gitRepo, input.branch, input.token, zipFile).close();
      } catch (GitAPIException | IOException e) {
        return EventGenerator.createErrorEvent(input.workflowCallerId, String.format("Cannot generate archive of repo %s; error: %s", input.gitRepo, e.getMessage()),
            SOURCE);
      }
      try {
        startPlanning(input, zipFile);
      } catch (ApiException e) {
        return EventGenerator.createErrorEvent(input.workflowCallerId, String.format("Cannot create planning in workspace %s for project %s for repo %s; error: %s",
            input.workspaceId, input.projectId, input.gitRepo, e.getResponseBody()), SOURCE);
      }
    } catch (IOException e) {
      log.error("Cannot create temp dir to clone repo {}", input.gitRepo, e);
      return EventGenerator.createErrorEvent(input.workflowCallerId, String.format("Cannot create temp dir to clone repo %s; error: %s", input.gitRepo, e.getMessage()),
          SOURCE);
    }

    return EventGenerator.createPlanCreatedEvent(input.workflowCallerId, SOURCE);
  }


  private void startPlanning(FunInput input, Path zipFile) throws ApiException {
    try {
      move2KubeService.createPlan(input.workspaceId, input.projectId, zipFile);
    } catch (ApiException e) {
      log.error("Cannot start plan to migrate repo {} with file {}", input.gitRepo, zipFile, e);
      throw e;
    }
  }

  public static class FunInput {
    public String gitRepo;
    public String branch;
    public String token;

    public String workspaceId;
    public String projectId;

    public String workflowCallerId;

    public boolean validate() {
      return !((gitRepo == null || gitRepo.isBlank()) ||
          (branch == null || branch.isBlank()) ||
          (workspaceId == null || workspaceId.isBlank()) ||
          (projectId == null || projectId.isBlank()) ||
          (workflowCallerId == null || workflowCallerId.isBlank()));
    }

    @Override
    public String toString() {
      return "FunInput{" +
          "gitRepo='" + gitRepo + '\'' +
          ", branch='" + branch + '\'' +
          ", workspaceId='" + workspaceId + '\'' +
          ", projectId='" + projectId + '\'' +
          ", workflowCallerId='" + workflowCallerId + '\'' +
          '}';
    }
  }

}
