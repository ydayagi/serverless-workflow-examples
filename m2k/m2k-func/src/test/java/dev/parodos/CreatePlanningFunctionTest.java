package dev.parodos;

import dev.parodos.move2kube.ApiException;
import dev.parodos.service.GitService;
import dev.parodos.service.Move2KubeService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
public class CreatePlanningFunctionTest {

  @InjectMock
  GitService gitServiceMock;

  @InjectMock
  Move2KubeService move2KubeServiceMock;

  @ConfigProperty(name = "plan-created.event.name")
  private String planCreatedEventName;

  private Git git;

  @BeforeEach
  public void setUp() throws GitAPIException, IOException {
    File tmpDir;
    tmpDir = Files.createTempDirectory("gitRepoTest").toFile();
    git = Git.init().setDirectory(tmpDir).call();
  }

  @AfterEach
  public void tearDown() {
    git.getRepository().close();
  }

  @Test
  public void testCreatePlanIsWorking() throws GitAPIException, IOException, ApiException {
    UUID workflowCallerId = UUID.randomUUID();
    when(gitServiceMock.generateRepositoryArchive(anyString(), anyString(), anyString(), any())).thenReturn(git);
    doNothing().when(move2KubeServiceMock).createPlan(anyString(), anyString(), any());
    RestAssured.given().contentType("application/json")
        .header("ce-specversion", "1.0")
        .header("ce-id", UUID.randomUUID().toString())
        .header("ce-type", "create-plan")
        .header("ce-source", "test")
        .body("{\"gitRepo\": \"gitRepo\", " +
            "\"branch\": \"branch\"," +
            " \"token\": \"token\"," +
            " \"workspaceId\": \"workspaceId\"," +
            " \"projectId\": \"projectId\"," +
            " \"workflowCallerId\": \"" + workflowCallerId + "\"" +
            "}")
        .post("/")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .header("ce-type", planCreatedEventName)
        .header("ce-kogitoprocrefid", workflowCallerId.toString())
        .header("ce-source", CreatePlanningFunction.SOURCE)
        .body(containsString("\"error\":null"));

    verify(gitServiceMock, times(1)).generateRepositoryArchive(anyString(), anyString(), anyString(), any());
    verify(move2KubeServiceMock, times(1)).createPlan(anyString(), anyString(), any());
  }

  @Test
  public void testCreatePlanIsFailingWhenGeneratingGitArchive() throws GitAPIException, IOException, ApiException {
    UUID workflowCallerId = UUID.randomUUID();
    when(gitServiceMock.generateRepositoryArchive(anyString(), anyString(), anyString(), any())).thenThrow(new InvalidRemoteException("Error when cloning git"));
    RestAssured.given().contentType("application/json")
        .header("ce-specversion", "1.0")
        .header("ce-id", UUID.randomUUID().toString())
        .header("ce-type", "create-plan")
        .header("ce-source", "test")
        .body("{\"gitRepo\": \"gitRepo\", " +
            "\"branch\": \"branch\"," +
            " \"token\": \"token\"," +
            " \"workspaceId\": \"workspaceId\"," +
            " \"projectId\": \"projectId\"," +
            " \"workflowCallerId\": \"" + workflowCallerId + "\"" +
            "}")
        .post("/")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .header("ce-type", EventGenerator.ERROR_EVENT)
        .header("ce-kogitoprocrefid", workflowCallerId.toString())
        .header("ce-source", CreatePlanningFunction.SOURCE)
        .body(not(containsString("\"error\":null")));

    verify(gitServiceMock, times(1)).generateRepositoryArchive(anyString(), anyString(), anyString(), any());
    verify(move2KubeServiceMock, times(0)).createPlan(anyString(), anyString(), any());
  }

  @Test
  public void testCreatePlanIsFailingWhenCreatingPlan() throws GitAPIException, IOException, ApiException {
    UUID workflowCallerId = UUID.randomUUID();
    when(gitServiceMock.generateRepositoryArchive(anyString(), anyString(), anyString(), any())).thenReturn(git);
    doThrow(new ApiException("Error when interacting with move2kube")).when(move2KubeServiceMock).createPlan(anyString(), anyString(), any());
    RestAssured.given().contentType("application/json")
        .header("ce-specversion", "1.0")
        .header("ce-id", UUID.randomUUID().toString())
        .header("ce-type", "create-plan")
        .header("ce-source", "test")
        .body("{\"gitRepo\": \"gitRepo\", " +
            "\"branch\": \"branch\"," +
            " \"token\": \"token\"," +
            " \"workspaceId\": \"workspaceId\"," +
            " \"projectId\": \"projectId\"," +
            " \"workflowCallerId\": \"" + workflowCallerId + "\"" +
            "}")
        .post("/")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .header("ce-type", EventGenerator.ERROR_EVENT)
        .header("ce-kogitoprocrefid", workflowCallerId.toString())
        .header("ce-source", CreatePlanningFunction.SOURCE)
        .body(not(containsString("\"error\":null")));
    verify(gitServiceMock, times(1)).generateRepositoryArchive(anyString(), anyString(), anyString(), any());
    verify(move2KubeServiceMock, times(1)).createPlan(anyString(), anyString(), any());
  }

  @Test
  public void testCreatePlanMissingInput() throws GitAPIException, IOException, ApiException {
    UUID workflowCallerId = UUID.randomUUID();
    RestAssured.given().contentType("application/json")
        .header("ce-specversion", "1.0")
        .header("ce-id", UUID.randomUUID().toString())
        .header("ce-type", "create-plan")
        .header("ce-source", "test")
        .body("{\"gitRepo\": \"gitRepo\", " +
            "\"branch\": \"branch\"," +
            " \"token\": \"token\"," +
            " \"projectId\": \"projectId\"," +
            " \"workflowCallerId\": \"" + workflowCallerId + "\"" +
            "}")
        .post("/")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .header("ce-type", EventGenerator.ERROR_EVENT)
        .header("ce-kogitoprocrefid", workflowCallerId.toString())
        .header("ce-source", CreatePlanningFunction.SOURCE)
        .body(not(containsString("\"error\":null")));

    verify(gitServiceMock, times(0)).generateRepositoryArchive(anyString(), anyString(), anyString(), any());
    verify(move2KubeServiceMock, times(0)).createPlan(anyString(), anyString(), any());
  }

}
